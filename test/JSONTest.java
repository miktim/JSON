/*
 * JSONTest, MIT (c) 2020 miktim@mail.ru
 */
import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.util.Date;
import org.miktim.json.JSON;

public class JSONTest {

    static void out(Object s) {
        System.out.println(s);
    }

    static void outIsOK(boolean res) {
        out((res ? "OK" : "FAILED"));
    }

    public static void main(String[] args) throws Exception {
        String path = (new File(".")).getAbsolutePath();
        if (args.length > 0) {
            path = args[0];
        }

        out("JSON package test");
        out("\n\rTest escape/unescape string:");
        String unescaped = new String(new char[]{0x8, 0x9, 0xA, 0xC, 0xD, 0x22, 0x2F, 0x5C, 0, '-', 0x1F, 0xd834, 0xdd1e});
        String escaped = JSON.escapeString(unescaped);
        out(escaped);
        outIsOK(unescaped.equals(JSON.unescapeString(escaped)));

        out("\n\rTest create/stringify JSON object:");
        JSON json = new JSON();
        json.set("Unescaped", unescaped)
                .set("EmptyJSON", new JSON())
                .set("Array", new int[]{0, 1, 2})
                .set("Null", null)
                .set("True", true)
                .set("False", false)
                .set("Char", 'c')
                .set("Double", 3.141592653589793238462643383279)
                .set("BigDecimal", new BigDecimal("3.141592653589793238462643383279"))
                .set("Long", 1415926535897932384L)
                .set("Int", 14159265)
                .set("Byte", (byte) 0xFF);
        json.exists("Unescaped");
        json.set("Unescaped", unescaped);
        out(json);
        out("List properties: " + json.list());
        for (String propName : json.list()) {
            if (json.get(propName) != null) {
                out("\"" + propName + "\" is instance of: "
                        + json.get(propName).getClass().getSimpleName());
            } else {
                out("\"" + propName + "\" is " + json.get(propName));
            }
        }

        out("\n\rTest stringify/parse JSON:");
        out(json);
        json = (JSON) JSON.parse(json.toString());
        out(json);
        out("\"Char\" is instance of: " + json.get("Char").getClass().getSimpleName());
        out("\"Byte\" is instance of: " + json.get("Byte").getClass().getSimpleName());

        out("\n\rTest nullnamed/nonexistent property:");
        out("Remove null/nonexistent property returns: "
                + json.remove(null) + "/" + json.remove("nonexistent"));
        out("Get null/nonexistent property returns: "
                + json.get(null) + "/" + json.get("nonexistent"));
        out("Exists null/nonexistent property returns: "
                + json.exists(null) + "/" + json.exists("nonexistent"));

        out("\n\rTest JSON clone then remove \"BigDecimal\":");
        JSON cloned = json.clone();
        cloned.remove("BigDecimal");
        out(json.list());
        out(cloned.list());

        out("\n\rTest JSON with Java arrays:");
        int[][] intArray = new int[][]{{0, 1, 2}, {3, 4, 5}};
        cloned.set("Array", intArray);
        out(JSON.stringify(cloned.get("Array")));
        intArray[0][1] = 6;
        out(JSON.stringify(cloned.get("Array")));
        Object[] array = intArray;
        array[1] = new int[]{7,8,9};
        out(JSON.stringify(array));
        out(JSON.stringify(cloned.get("Array")));

// examples from RFC 8259 https://datatracker.ietf.org/doc/rfc8259/?include_text=1
        out("\n\rTest examples from RFC 8259:");
        out(JSON.parse("\"\\uD834\\uDD1E\"")); // G-clef
        out(JSON.parse("3.141592653589793238462643383279"));
        out(JSON.parse("null"));
        out(JSON.stringify(JSON.parse("\t[1, null, 3.2, {}, \"some text\" ] ")));

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
        out(((JSON) object).toString());
        out(((JSON) object).get("Image"));
        out(((JSON) (((JSON) object).get("Image"))).set("Thumbnail", (Number) 256));
        out(((JSON) (((JSON) object).get("Image"))).remove("Thumbnail"));
        out(((JSON) object).clone().toString());
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
        out(JSON.stringify(object));
        out(JSON.stringify(((Object[]) object)[1]));

// Example from https://docs.oracle.com/en/database/oracle/oracle-database/12.2/adjsn/json-data.html#GUID-FBC22D72-AA64-4B0A-92A2-837B32902E2C        
        out("\n\rTest one more example:");
        json = (JSON) JSON.parse(new FileReader(new File(path, "json.json")));
        out(json.get("AllowPartialShipment"));
        array = (Object[]) json.get("LineItems");
        json = (JSON) array[1];
        out(json.get("Quantity"));
        json = (JSON) json.get("Part");
        out(json.get("Description"));
        json.set("Description", "Naked Gun");
        out(JSON.stringify(array));
        array[0] = 123;
        array[1] = null;
        out(JSON.stringify(array));

// ParseExceptions        
//        out(JSON.parse("\"asfas\\uD83\uDD1E\"")); // unparseable u-escaped char
//        out(JSON.parse("\"\uD834\\uDD1\"")); // unparseable u-escaped char
//        out(JSON.parse("123e")); // unparseable number
//        out(JSON.parse("123 e")); // EOT Expected
//        out(JSON.parse("{\"Latitude\":  37.371991\n\"")); // "}" expected
//        out(JSON.parse("b123")); // unexpected char
//        out(JSON.parse("falsen")); // unknown literal
// NullPointerException
//        json.set(null,123); // null property name
// IllegalArgumentExceptions
//        json.set("File", new File(path,"json.json")); // unsupported object
//        array[1] = new Date();
//        JSON.stringify(array); // unsupported object in array
//        json.set("unsupported", array) // unsupported object in array
//        JSON.array(array); // unsupported object in array
//        JSON.array(new JSON()); // argument is not an array
    }
}
