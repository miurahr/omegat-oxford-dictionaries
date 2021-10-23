package tokyo.northside.oxfordapi

import org.apache.commons.io.IOUtils
import org.junit.Test
import tokyo.northside.oxfordapi.dtd.Result

import static org.junit.Assert.*

class OxfordDictionaryEntriesTest {
    @Test
    void testParse1() {
        InputStream resource = ODParser.class.getClassLoader().getResourceAsStream("oxfordapi/entry_result1.json")
        String json = IOUtils.toString(resource, "UTF-8")
        ODParser parser = new ODParser("ace")
        parser.parse(json)
        List<Result> result = parser.getResults()
        Result entry = result.get(0)
        assertEquals("ace", entry.getId())

    }

}