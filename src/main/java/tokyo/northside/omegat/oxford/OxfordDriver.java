package tokyo.northside.omegat.oxford;

import org.omegat.core.dictionaries.DictionaryEntry;
import org.omegat.core.dictionaries.IDictionary;
import org.omegat.util.Language;
import org.omegat.util.Log;
import tokyo.northside.oxfordapi.OxfordDictionaryParser;
import tokyo.northside.oxfordapi.dtd.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OxfordDriver implements IDictionary {

    private static final String endpointUrl = "https://od-api.oxforddictionaries.com/api/v2/";

    private final Language source;
    private final Language target;
    private final Map<String, List<DictionaryEntry>> cache = new HashMap<>();

    public OxfordDriver(final Language source, final Language target) {
        this.source = source;
        this.target = target;
    }

    protected String getEntriesRequestUrl(final String word, final boolean strict) {
        final String strictMatch;
        if (strict) {
            strictMatch = "true";
        } else {
            strictMatch = "false";
        }
        final String wordId = word.toLowerCase();
        String language = source.getLanguageCode();
        return String.format("%sentries/%s/%s?&strictMatch=%s", endpointUrl, language, wordId, strictMatch);
    }

    protected String getTranslationsRequestUrl(final String word, final boolean strictMatch) {
        final String wordId = word.toLowerCase();
        String sourceLang = source.getLanguageCode();
        String targetLang = target.getLanguageCode();
        String targetUrl = String.format("%stranslations/%s/%s/%s?&strictMatch=%s", endpointUrl, sourceLang, targetLang, wordId, strictMatch);
        Log.log("target URL: " + targetUrl);
        return targetUrl;
    }

    private Map<String, Object> getHeaderEntries() {
        Map<String, Object> header = new HashMap<>();
        header.put("Accept", "application/json");
        header.put("app_id", OxfordPreferencesController.getAppId());
        header.put("app_key", OxfordPreferencesController.getAppKey());
        return header;
    }

    protected List<Result> query(final String requestUrl, final String word) {
        Map<String, Object> header = getHeaderEntries();
        String response = null;
        try {
            response = QueryUtil.query(requestUrl, header);
        } catch (IOException e) {
            Log.log(e.getMessage());
        }
        if (response != null) {
            OxfordDictionaryParser parser = new OxfordDictionaryParser(word);
            try {
                parser.parse(response);
                return parser.getResults();
            } catch (IOException e) {
                Log.log(e.getMessage());
            }
        }
        return new ArrayList<>();
    }

    /**
     * Read article's text.
     *
     * @param word The word to look up in the dictionary
     * @return List of entries. May be empty, but cannot be null.
     */
    @Override
    public List<DictionaryEntry> readArticles(String word) {
        if (!cache.containsKey(word)) {
            List<DictionaryEntry> dictionaryEntries = new ArrayList<>();
            List<Result> results = query(getEntriesRequestUrl(word, false), word);
            List<Result> translations = query(getTranslationsRequestUrl(word, false), word);
            for (Result result : results) {
                for (LexicalEntry lexicalEntry : result.getLexicalEntries()) {
                    for (Entry entry : lexicalEntry.getEntries()) {
                        for (Sense sense : entry.getSenses()) {
                            if (sense.getDefinitions() == null) continue;
                            StringBuilder sb = new StringBuilder();
                            for (String text : sense.getDefinitions()) {
                                sb.append(text);
                                sb.append("/");
                            }
                            dictionaryEntries.add(new DictionaryEntry(word, sb.toString()));
                        }
                    }
                }
            }
            for (Result result1 : translations) {
                for (LexicalEntry lexicalEntry1 : result1.getLexicalEntries()) {
                    for (Entry entry1 : lexicalEntry1.getEntries()) {
                        for (Sense sense1 : entry1.getSenses()) {
                            if (sense1.getTranslations() == null) continue;
                            StringBuilder sb = new StringBuilder();
                            for (Translation translation : sense1.getTranslations()) {
                                sb.append("/");
                                sb.append(translation.getText());
                            }
                            dictionaryEntries.add(new DictionaryEntry(word, sb.toString()));
                        }
                    }
                }
            }
            cache.put(word, dictionaryEntries);
        }
        return cache.get(word);
    }

    /**
     * Read article's text. Matching is predictive, so e.g. supplying "term"
     * will return articles for "term", "terminology", "termite", etc. The
     * default implementation simply calls {@link #readArticles(String)} for
     * backwards compatibility.
     *
     * @param word The word to look up in the dictionary
     * @return List of entries. May be empty, but cannot be null.
     */
    @Override
    public List<DictionaryEntry> readArticlesPredictive(String word) throws Exception {
        return readArticles(word);
    }

    /**
     * Dispose IDictionary. Default is no action.
     */
    @Override
    public void close() throws IOException {
        IDictionary.super.close();
    }
}
