package tokyo.northside.oxfordapi

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.commons.io.IOUtils
import org.junit.Test
import tokyo.northside.oxfordapi.dtd.Result

import static org.junit.Assert.*

class OxfordDictionaryEntriesTest {
    @Test
    void testParse1() {
        InputStream resource = ODParser.class.getClassLoader().getResourceAsStream("oxfordapi/entry_result1.json")
        String json = IOUtils.toString(resource, "UTF-8")
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(json);
        node = node.get("results");
        def results = mapper.readValue(node.traverse(), new TypeReference<List<Result>>() {});
        Result result = results.get(0)
        assertEquals("ace", result.getId())

    }

}