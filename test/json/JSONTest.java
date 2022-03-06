/*
 * JSONTest, MIT (c) 2020-2022 miktim@mail.ru
 */
import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.util.ArrayList;
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

        log("JSON package test");

        log("\n\rTest constructor:");
        log(new JSON("One", 1, "Two", 2, 3, "Three"));
        
        log("\n\rTest escape/unescape string:");
        String unescaped = new String(new char[]{0x8, 0x9, 0xA, 0xC, 0xD, 0x22, 0x2F, 0x5C, 0, '-', 0x1F, 0xd834, 0xdd1e});
        String escaped = JSON.escapeString(unescaped);
        log(escaped);
        logIsOK(unescaped.equals(JSON.unescapeString(escaped)));

        log("\n\rTest create/stringify JSON object:");
        JSON json = (new JSON())
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
        log("List members: " + JSON.stringify(json.list()));
        for (String memberName : json.list()) {
            if (json.get(memberName) != null) {
                log("\"" + memberName + "\" is instance of: "
                        + json.get(memberName).getClass().getSimpleName());
            } else {
                log("\"" + memberName + "\" is " + json.get(memberName));
            }
        }

        log("\n\rTest stringify/parse JSON:");
        log(json);
        log("List members: " + JSON.stringify(json.list()));
        json = (JSON) JSON.parse(json.toString());
        log(json);
        log("List members: " + JSON.stringify(json.list()));
        for (String memberName : json.list()) {
            if (json.get(memberName) != null) {
                log("\"" + memberName + "\" is instance of: "
                        + json.get(memberName).getClass().getSimpleName());
            } else {
                log("\"" + memberName + "\" is " + json.get(memberName));
            }
        }

        log("\n\rTest nullnamed/nonexistent member:");
        log("Remove null/nonexistent member returns: "
                + json.remove(null) + "/" + json.remove("nonexistent"));
        log("Get null/nonexistent member returns: "
                + json.get(null) + "/" + json.get("nonexistent"));
        log("Exists null/nonexistent member returns: "
                + json.exists(null) + "/" + json.exists("nonexistent"));

        log("\n\rTest JSON clone then remove \"BigDecimal\":");
        JSON cloned = (JSON) json.clone();
        cloned.remove("BigDecimal");
        log(json.list());
        log(cloned.list());

        log("\n\rTest JSON with Java arrays:");
        int[][] intArray = new int[][]{{0, 1, 2}, {3, 4, 5, 6}};
        log(JSON.stringify(intArray));
        cloned.set("Array", intArray);
        log(JSON.stringify(cloned.get("Array")));
        intArray[0][1] = 6;
        log(JSON.stringify(cloned.get("Array")));
        Object[] array = intArray;
        array[1] = new int[]{7, 8, 9};
        log(JSON.stringify(array));
        log(JSON.stringify(cloned.get("Array")));

        log("\n\rTest generator with other Java objects (ArrayList with Date, File entries):");
        ArrayList<Object> arrayList = new ArrayList<>();
        arrayList.add(new Date());
        arrayList.add(new File(path, "json.json"));
        log(JSON.stringify(arrayList));

// examples from RFC 8259 https://datatracker.ietf.org/doc/rfc8259/?include_text=1
        log("\n\rTest examples from RFC 8259:");
        log(JSON.parse("\"\\uD834\\uDD1E\"")); // G-clef
        log(JSON.parse("3.141592653589793238462643383279"));
        log(JSON.parse("null"));
        log(JSON.stringify(JSON.parse("\t[1, null, 3.2, {}, \"some text\" ] ")));

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
        Object object = JSON.parse(example1);
        log(((JSON) object).toString());
        log(((JSON) object).get("Image"));
        log(((JSON) (((JSON) object).get("Image"))).set("Thumbnail", 256));
        log(((JSON) (((JSON) object).get("Image"))).remove("Thumbnail"));
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
        object = JSON.parse(example2);
        log(JSON.stringify(object));
        log(JSON.stringify(((Object[]) object)[1]));

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
