/*
 * JSONTest, MIT (c) 2020 miktim@mail.ru
 */
import java.math.BigDecimal;
import org.samples.java.JSON;

public class JSONTest {

    public static void main(String[] args) throws Exception {
        JSON json = new JSON();
        json.set("Escaped", new String(new char[]{0x8, 0x9, 0xA, 0xC, 0xD, 0x22, 0x2F, 0x5C, 0, '-', 0x1F, 0xd834, 0xdd1e}))
                .set("EmptyJSON", new JSON())
                .set("EmptyArray", new Object[0])
                .set("Null", null)
                .set("False", (Boolean) false)
                .set("Double", 3.141592653589793238462643383279)
                .set("BigDecimal", new BigDecimal("3.141592653589793238462643383279"))
                .set("MaxLong", Long.MAX_VALUE)
                .set("MinInt", Integer.MIN_VALUE);
        System.out.println(json);
        System.out.println(json.list());
        System.out.println(json.get("MinInt").getClass().getSimpleName()
                + " is Number? "
                + (json.get("MinInt") instanceof Number));
//        System.out.println(JSON.parse("\uD834\uDD1E"));
        System.out.println((JSON) JSON.parse(json.stringify()));
        System.out.println(JSON.escapeString((String) json.get("Escaped")));
        System.out.println(json.clone());
        System.out.println(((Number) json.get("Double")).longValue());
// examples from RFC 8259        
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
        System.out.println(((JSON) object).stringify());
        System.out.println(((JSON) object).get("Image"));
        System.out.println(((JSON) (((JSON) object).get("Image"))).set("Thumbnail", (Number) 256));
        System.out.println(((JSON) (((JSON) object).get("Image"))).remove("Thumbnail"));
        System.out.println(((JSON) object).clone().stringify());
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
        System.out.println(JSON.stringify(((Object[]) object)[1]));
    }

}
