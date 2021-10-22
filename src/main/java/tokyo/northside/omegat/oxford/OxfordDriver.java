package tokyo.northside.omegat.oxford;

import org.apache.hc.client5.http.ClientProtocolException;
import org.omegat.core.dictionaries.DictionaryEntry;
import org.omegat.core.dictionaries.IDictionary;
import org.omegat.util.Language;
import org.omegat.util.Log;
import tokyo.northside.oxfordapi.OxfordDictionaryParser;
import tokyo.northside.oxfordapi.dtd.*;

import java.io.IOException;
import java.util.*;

public class OxfordDriver implements IDictionary {

    private static final String endpointUrl = "https://od-api.oxforddictionaries.com/api/v2/";

    private final Language source;
    private final Language target;
    private final Map<String, List<DictionaryEntry>> cache = new HashMap<>();

    public OxfordDriver(final Language source, final Language target) {
        this.source = source;
        this.target = target;
    }

    /**
     * Read article's text.
     *
     * @param word The word to look up in the dictionary
     * @return List of entries. May be empty, but cannot be null.
     */
    @Override
    public List<DictionaryEntry> readArticles(String word) {
        return queryArticle(word, true);
    }

    /**
     * Read article's text. Matching is predictive, so e.g. supplying "term"
     * will return articles for "term", "terminology", "termite", etc.
     *
     * @param word The word to look up in the dictionary
     * @return List of entries. May be empty, but cannot be null.
     */
    @Override
    public List<DictionaryEntry> readArticlesPredictive(String word) {
        return queryArticle(word, false);
    }

    private List<DictionaryEntry> queryArticle(final String word, final boolean strict) {
        if (!cache.containsKey(word)) {
            List<DictionaryEntry> dictionaryEntries = new ArrayList<>();
            if (OxfordPreferencesController.isMonolingual()) {
                try {
                    dictionaryEntries.addAll(getDefinitions(word, strict));
                } catch (ClientProtocolException cpe) {
                    // when got connection/query error, return without any content.
                    return Collections.emptyList();
                }
            }
            if (OxfordPreferencesController.isBilingual()) {
                try {
                    dictionaryEntries.addAll(getTranslations(word));
                } catch (ClientProtocolException ignored) {
                }
                if (dictionaryEntries.isEmpty()) {
                    return Collections.emptyList();
                }
            }
            cache.put(word, dictionaryEntries);
        }
        return cache.get(word);
    }

    protected List<DictionaryEntry> getTranslations(final String word) throws ClientProtocolException {
        List<DictionaryEntry> dictionaryEntries = new ArrayList<>();
        List<Result> translations;
        translations = queryTranslation(word);
        for (Result result : translations) {
            for (LexicalEntry lexicalEntry : result.getLexicalEntries()) {
                String title = lexicalEntry.getText();
                StringBuilder sb = new StringBuilder("<ol>");
                for (Entry entry : lexicalEntry.getEntries()) {
                    for (Sense sense : entry.getSenses()) {
                        if (sense.getTranslations() == null) continue;
                        for (Translation translation : sense.getTranslations()) {
                            sb.append("<li>").append(translation.getText()).append("</li>");
                        }
                    }
                }
                sb.append("</ol>");
                dictionaryEntries.add(new DictionaryEntry(title, sb.toString()));
            }
        }
        return dictionaryEntries;
    }

    protected List<DictionaryEntry> getDefinitions(final String word, final boolean strict) throws ClientProtocolException {
        List<DictionaryEntry> dictionaryEntries = new ArrayList<>();
        List<Result> results;
        results = queryWord(word, strict);
        for (Result result : results) {
            for (LexicalEntry lexicalEntry : result.getLexicalEntries()) {
                String title = lexicalEntry.getText();
                StringBuilder sb = new StringBuilder();
                for (Entry entry : lexicalEntry.getEntries()) {
                    List<String> et = entry.getEtymologies();
                    if (et != null) {
                        sb.append("<span>");
                        for (String etymology : et) {
                            sb.append(etymology);
                        }
                        sb.append("</span>");
                    }
                    List<Pronunciation> pronunciations = entry.getPronunciations();
                    if (pronunciations != null) {
                        sb.append("<span>");
                        for (Pronunciation pron: pronunciations) {
                            if (pron.getAudioFile() != null) {
                                sb.append("<a href=\"").append(pron.getAudioFile()).append("\">");
                            }
                            sb.append("[").append(pron.getPhoneticSpelling()).append("]");
                            if (pron.getAudioFile() != null) {
                                sb.append("</a>");
                            }
                        }
                        sb.append("</span>");
                    }
                    sb.append("<ol>");
                    for (Sense sense : entry.getSenses()) {
                        if (sense.getDefinitions() == null) continue;
                        for (String text : sense.getDefinitions()) {
                            sb.append("<li>").append(text).append("</li>");
                        }

                        List<Example> examples = sense.getExamples();
                        if (examples != null) {
                            sb.append("<ul>");
                            for (Example ex : examples) {
                                sb.append("<li>").append(ex.getText()).append("</li>");
                            }
                            sb.append("</ul>");
                        }
                    }
                    sb.append("</ol>");
                }
                dictionaryEntries.add(new DictionaryEntry(title, sb.toString()));
            }
        }
        return dictionaryEntries;
    }

    protected List<Result> queryTranslation(final String word) throws ClientProtocolException {
        return query(getTranslationsRequestUrl(word), word);
    }

    protected List<Result> queryWord(final String word, final boolean strict) throws ClientProtocolException {
        return query(getEntriesRequestUrl(word, strict), word);
    }

    protected List<Result> query(final String requestUrl, final String word) throws ClientProtocolException {
        Map<String, Object> header = getHeaderEntries();
        String response = null;
        try {
            response = QueryUtil.query(requestUrl, header);
        } catch (ClientProtocolException cpe) {
            throw cpe;
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

    protected String getEntriesRequestUrl(final String word, final boolean strict) {
        final String strictMatch;
        if (strict) {
            strictMatch = "true";
        } else {
            strictMatch = "false";
        }
        final String wordId = word.toLowerCase();
        String language = source.getLanguageCode();
        if (language.equals("en")) {
            language = "en-gb";
        }
        return String.format("%sentries/%s/%s?strictMatch=%s", endpointUrl, language, wordId, strictMatch);
    }

    protected String getTranslationsRequestUrl(final String word) {
        final String wordId = word.toLowerCase();
        String sourceLang = source.getLanguageCode();
        String targetLang = target.getLanguageCode();
        if (sourceLang.equals("en")) {
            sourceLang = "en-gb";
        }
        if (targetLang.equals("en")) {
            targetLang = "en-gb";
        }
        return String.format("%stranslations/%s/%s?q=%s", endpointUrl, sourceLang, targetLang, wordId);
    }

    private Map<String, Object> getHeaderEntries() {
        Map<String, Object> header = new HashMap<>();
        header.put("Accept", "application/json");
        header.put("app_id", OxfordPreferencesController.getAppId());
        header.put("app_key", OxfordPreferencesController.getAppKey());
        return header;
    }

    @Override
    public void close() {
    }
}
