package tokyo.northside.omegat.oxford;

import org.apache.hc.client5.http.ClientProtocolException;
import org.omegat.core.dictionaries.DictionaryEntry;
import org.omegat.core.dictionaries.IDictionary;
import org.omegat.util.Language;
import tokyo.northside.omegat.preferences.OxfordPreferencesController;
import tokyo.northside.oxfordapi.ODSearcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OxfordDriver implements IDictionary {

    private final Language source;
    private final Language target;
    private ODSearcher searcher;
    private final Map<String, List<DictionaryEntry>> cache = new HashMap<>();

    public OxfordDriver(final Language source, final Language target) {
        this.source = source;
        this.target = target;
        searcher = new ODSearcher(OxfordPreferencesController.getAppId(),
                OxfordPreferencesController.getAppKey());
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
            if (OxfordPreferencesController.isCredentialChanged()) {
                searcher.setAppId(OxfordPreferencesController.getAppId());
                searcher.setAppKey(OxfordPreferencesController.getAppKey());
            }
            List<DictionaryEntry> dictionaryEntries = new ArrayList<>();
            if (OxfordPreferencesController.isMonolingual()) {
                String language = source.getLanguageCode();
                try {
                    dictionaryEntries.addAll(searcher.getDefinitions(word, language, strict));
                } catch (ClientProtocolException cpe) {
                    // when got connection/query error, return without any content.
                    return Collections.emptyList();
                } catch (IOException e) {
                    e.printStackTrace();
                    return Collections.emptyList();
                }
            }
            if (OxfordPreferencesController.isBilingual()) {
                String sourceLang = source.getLanguageCode();
                String targetLang = target.getLanguageCode();
                try {
                    dictionaryEntries.addAll(searcher.getTranslations(word, sourceLang, targetLang));
                } catch (IOException ignored) {
                }
                if (dictionaryEntries.isEmpty()) {
                    return Collections.emptyList();
                }
            }
            cache.put(word, dictionaryEntries);
        }
        return cache.get(word);
    }

    @Override
    public void close() {
    }
}
