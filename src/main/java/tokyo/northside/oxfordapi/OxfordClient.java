package tokyo.northside.oxfordapi;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.omegat.core.dictionaries.DictionaryEntry;
import tokyo.northside.oxfordapi.dtd.LexicalEntry;
import tokyo.northside.oxfordapi.dtd.Result;

import java.io.IOException;
import java.util.*;

public final class OxfordClient {

    static final HttpClientResponseHandler<String> responseHandler = response -> {
        final int status = response.getCode();
        if (status >= HttpStatus.SC_SUCCESS && status < HttpStatus.SC_REDIRECTION) {
            try (HttpEntity entity = response.getEntity()) {
                if (entity != null) {
                    return EntityUtils.toString(entity);
                } else {
                    return null;
                }
            } catch (final ParseException ex) {
                throw new ClientProtocolException(ex);
            }
        } else {
            throw new ClientProtocolException(String.format("Unexpected response status: %d", status));
        }
    };
    private final String endpointUrl;

    private final String appId;
    private final String appKey;

    public OxfordClient(final String appId, final String appKey, final String baseUrl) {
        this.appId = appId;
        this.appKey = appKey;
        endpointUrl = baseUrl;
    }

    public List<DictionaryEntry> getTranslations(final String word, String source, String target) throws OxfordClientException {
        List<DictionaryEntry> dictionaryEntries = new ArrayList<>();
        for (Result result : query(getTranslationsRequestUrl(word, source, target))) {
            for (LexicalEntry lexicalEntry : result.getLexicalEntries()) {
                dictionaryEntries.add(HTMLFormatter.formatTranslations(lexicalEntry));
            }
        }
        return dictionaryEntries;
    }

    public List<DictionaryEntry> getDefinitions(final String word, final String language, final boolean strict) throws OxfordClientException {
        List<DictionaryEntry> dictionaryEntries = new ArrayList<>();
        for (Result result: query(getEntriesRequestUrl(word, language, strict))) {
            for (LexicalEntry lexicalEntry : result.getLexicalEntries()) {
                dictionaryEntries.add(HTMLFormatter.formatDefinitions(lexicalEntry));
            }
        }
        return dictionaryEntries;
    }

    private List<Result> query(final String requestUrl) throws OxfordClientException {
        Map<String, Object> header = getHeaderEntries();
        String response;
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(requestUrl);
            header.forEach(httpGet::addHeader);
            response = httpclient.execute(httpGet, responseHandler);
        } catch (IOException e) {
            throw new OxfordClientException(e.getMessage());
        }
        if (response != null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readTree(response);
                node = node.get("results");
                return mapper.readValue(node.traverse(), new TypeReference<List<Result>>() {});
            } catch (IOException e) {
                throw new OxfordClientException(e.getMessage());
            }
        }
        return Collections.emptyList();
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

    public static class OxfordClientException extends Exception {
        /**
         * Constructs a new exception with the specified detail message.  The
         * cause is not initialized, and may subsequently be initialized by
         * a call to {@link #initCause}.
         *
         * @param message the detail message. The detail message is saved for
         *                later retrieval by the {@link #getMessage()} method.
         */
        public OxfordClientException(String message) {
            super(message);
        }
    }
}
