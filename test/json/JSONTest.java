/*
 * JSONTest, MIT (c) 2020-2022 miktim@mail.ru
 */
import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import org.miktim.JSON;

public class JSONTest {

    static void log(Object s) {
        System.out.println(s);
    }

    static void logIsOK(boolean res) {
        log((res ? "OK" : "FAILED"));
    }

    public static void main(String[] args) throws Exception {
        String path = (new File(".")).getAbsolutePath();
        if (args.length > 0) {
            path = args[0];
        }

        log("JSON class test");

        log("\n\rTest constructor:");
        JSON json = new JSON("One", 1, "Two", 2, 3, "Three",
                "Nested", new JSON("Array",
                        new float[][]{{1.1f, 2.2f}, {3.3f, 4.4f}}));
        log(json);
        log(json.getNumber("One").floatValue()); // Number(Integer)
// java.lang.ClassCastException        
//        log(json.getString("Two"));
        log(json.stringify("Two"));
        log(json.getString("3"));
        log(json.getJSON("Nested").getNumber("Array", 1, 1).intValue());

        log("\n\rTest escape/unescape string:");
        String unescaped = new String(new char[]{0x8, 0x9, 0xA, 0xC, 0xD, 0x22, 0x2F, 0x5C, 0, '-', 0x1F, 0xd834, 0xdd1e});
        String escaped = JSON.escapeString(unescaped);
        log(escaped);
        logIsOK(unescaped.equals(JSON.unescapeString(escaped)));

        log("\n\rTest JSON typecast.\r\nBefore normalization:");
        json = (new JSON())
                .set("Unescaped", unescaped)
                .set("EmptyJSON", new JSON())
                .set("intArray", new int[][]{{0, 1, 2}, {3, 4, 5, 6}})
                .set(null, null)
                .set("True", true)
                .set("False", false)
                .set("Char", 'c')
                .set("Double", 3.141592653589793238462643383279)
                .set("BigDecimal", new BigDecimal("3.141592653589793238462643383279"))
                .set("Long", 1415926535897932384L)
                .set("Int", 14159265)
                .set("Byte", (byte) 0xFF);
        log(json);

        log("List members: " + json.listNames());
        for (String memberName : json.listNames()) {
            if (json.get(memberName) != null) {
                log("\"" + memberName + "\" is instance of: "
                        + json.get(memberName).getClass().getSimpleName());
            } else {
                log("\"" + memberName + "\" is null");
            }
        }

        log("\n\rNormalized:");
        json = json.normalize();
        log(json);
        log("List members: " + json.listNames());
        for (String memberName : json.listNames()) {
            if (json.get(memberName) != null) {
                log("\"" + memberName + "\" is instance of: "
                        + json.get(memberName).getClass().getSimpleName());
            } else {
                log("\"" + memberName + "\" is null");
            }
        }

        log("\n\rTest JSON with Java arrays:");
        int[][] intArray = new int[][]{{0, 1, 2}, {3, 4, 5, 6}};
        json = new JSON("Array", intArray);
        log(json.get("Array").getClass().getSimpleName());
// array is instance of int[][]        
// java.lang.ClassCastException 
//        log(json.getArray("Array", 1).getClass().getSimpleName());
        log(json.getNumber("Array", 1, 1).floatValue());

        log(json = json.normalize());
        log(json.get("Array").getClass().getSimpleName());
// array is instance of Object[]        
        log(json.getNumber("Array", 1, 1).floatValue());
        Object[] array;
        array = json.getArray("Array");
        log(array.length);
        array = json.getArray("Array",1);
        log(array.length);
// cast array. doesn't make much sense
        Number[] na = Arrays.copyOf(array, array.length, Number[].class); 
        log(JSON.stringify(na));

        log("\n\rTest generator with other Java objects (ArrayList with Array, Date and File entries):");
        ArrayList<Object> arrayList = new ArrayList<>();
        arrayList.add(new int[2]);
        arrayList.add(new Date());
        arrayList.add(new File(path, "json.json"));
        String s = JSON.stringify(arrayList);
        log(s);
        Object o = JSON.parse(s);
        log("is Array?: " + o.getClass().isArray());

// examples from RFC 8259 https://datatracker.ietf.org/doc/rfc8259/?include_text=1
        log("\n\rTest examples from RFC 8259:");
        log(JSON.parse("\"\\uD834\\uDD1E\"")); // G-clef
        log(JSON.parse("3.141592653589793238462643383279"));
        log(JSON.parse("null"));

        String example1 = "{\n"
                + "        \"Image\": {\n"
                + "            \"Width\":  800,\n"
                + "            \"Height\": 600,\n"
                + "            \"Title\":  \"View from 15th Floor\",\n"
                + "            \"Thumbnail\": {\n"
                //??? in accordance with RFC, the solidus (/) MUST be escaped                
                + "                \"Url\":    \"http://www.example.com/image/481989943\",\n"
                + "                \"Height\": 125,\n"
                + "                \"Width\":  100\n"
                + "            },\n"
                + "            \"Animated\" : false,\n"
                + "            \"IDs\": [116, 943, 234, 38793]\n"
                + "          }\n"
                + "      } ";
        json = (JSON) JSON.parse(example1);
        log(json.get("Image"));
        log(json.getJSON("Image").set("Thumbnail", 256)); // replace JSON object with number
        log(json.getJSON("Image").remove("Thumbnail")); // remove member
        String example2 = "[\n"
                + "        {\n"
                + "           \"precision\": \"zip\",\n"
                + "           \"Latitude\":  37.7668,\n"
                + "           \"Longitude\": -122.3959,\n"
                + "           \"Address\":   \"\",\n"
                + "           \"City\":      \"SAN FRANCISCO\",\n"
                + "           \"State\":     \"CA\",\n"
                + "           \"Zip\":       \"94107\",\n"
                + "           \"Country\":   \"US\"\n"
                + "        },\n"
                + "        {\n"
                + "           \"precision\": \"zip\",\n"
                + "           \"Latitude\":  37.371991,\n"
                + "           \"Longitude\": -122.026020,\n"
                + "           \"Address\":   \"\",\n"
                + "           \"City\":      \"SUNNYVALE\",\n"
                + "           \"State\":     \"CA\",\n"
                + "           \"Zip\":       \"94085\",\n"
                + "           \"Country\":   \"US\"\n"
                + "        }\n"
                + "      ]";
        array = (Object[]) JSON.parse(example2);
        log(JSON.stringify(array));
        log(JSON.stringify(array[1]));

// Example from https://docs.oracle.com/en/database/oracle/oracle-database/12.2/adjsn/json-data.html#GUID-FBC22D72-AA64-4B0A-92A2-837B32902E2C        
        log("\n\rTest one more example:");
        json = (JSON) JSON.parse(new FileReader(new File(path, "json.json")));
        log(json.get("AllowPartialShipment"));
        array = (Object[]) json.get("LineItems");
        json = (JSON) array[1];
        log(json.get("Quantity"));
        json = (JSON) json.get("Part");
        log(json.get("Description"));
        json.set("Description", "Naked Gun");
        log(JSON.stringify(array));

// ParseExceptions        
//        log(JSON.parse("\"asfas\\uD83\uDD1E\"")); // unparseable u-escaped char
//        log(JSON.parse("\"\uD834\\uDD1\"")); // unparseable u-escaped char
//        log(JSON.parse("123e")); // unparseable number
//        log(JSON.parse("123 e")); // EOT Expected
//        log(JSON.parse("{{}}"); // name expected
//        log(JSON.parse("{\"Latitude\":  37.371991\n\"")); // "}" expected
//        log(JSON.parse("b123")); // unexpected char
//        log(JSON.parse("falsen")); // unknown literal
    }
}
