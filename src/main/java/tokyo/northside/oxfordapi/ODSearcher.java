package tokyo.northside.oxfordapi;

import org.omegat.core.dictionaries.DictionaryEntry;
import tokyo.northside.oxfordapi.dtd.LexicalEntry;
import tokyo.northside.oxfordapi.dtd.Result;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ODSearcher {

    private static final String endpointUrl = "https://od-api.oxforddictionaries.com/api/v2/";

    private String appId;
    private String appKey;

    public ODSearcher(final String appId, final String appKey) {
        this.appId = appId;
        this.appKey = appKey;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public List<DictionaryEntry> getTranslations(final String word, String source, String target) throws IOException {
        List<DictionaryEntry> dictionaryEntries = new ArrayList<>();
        for (Result result : query(getTranslationsRequestUrl(word, source, target), word)) {
            for (LexicalEntry lexicalEntry : result.getLexicalEntries()) {
                dictionaryEntries.add(HTMLFormatter.formatTranslations(lexicalEntry));
            }
        }
        return dictionaryEntries;
    }

    public List<DictionaryEntry> getDefinitions(final String word, final String language, final boolean strict) throws IOException {
        List<DictionaryEntry> dictionaryEntries = new ArrayList<>();
        for (Result result: query(getEntriesRequestUrl(word, language, strict), word)) {
            for (LexicalEntry lexicalEntry : result.getLexicalEntries()) {
                dictionaryEntries.add(HTMLFormatter.formatDefinitions(lexicalEntry));
            }
        }
        return dictionaryEntries;
    }

    private List<Result> query(final String requestUrl, final String word) throws IOException {
        Map<String, Object> header = getHeaderEntries();
        String response = QueryUtil.query(requestUrl, header);
        if (response != null) {
            ODParser parser = new ODParser(word);
            try {
                parser.parse(response);
                return parser.getResults();
            } catch (IOException ignored) {
            }
        }
        return new ArrayList<>();
    }

    private String getEntriesRequestUrl(final String word, final String language, final boolean strict) {
        final String strictMatch;
        if (strict) {
            strictMatch = "true";
        } else {
            strictMatch = "false";
        }
        final String wordId = word.toLowerCase();
        String lang;
        if (language.equals("en")) {
            lang = "en-gb";
        } else {
            lang = language;
        }
        return String.format("%sentries/%s/%s?strictMatch=%s", endpointUrl, lang, wordId, strictMatch);
    }

    private String getTranslationsRequestUrl(final String word, final String sourceLang, final String targetLang) {
        final String wordId = word.toLowerCase();
        String source;
        String target;
        if (sourceLang.equals("en")) {
            source = "en-gb";
        } else {
            source = sourceLang;
        }
        if (targetLang.equals("en")) {
            target = "en-gb";
        } else {
            target = targetLang;
        }
        return String.format("%stranslations/%s/%s?q=%s", endpointUrl, source, target, wordId);
    }

    private Map<String, Object> getHeaderEntries() {
        Map<String, Object> header = new HashMap<>();
        header.put("Accept", "application/json");
        header.put("app_id", appId);
        header.put("app_key", appKey);
        return header;
    }
}
