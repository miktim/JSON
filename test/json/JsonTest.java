/*
 * JsonTest, MIT (c) 2020-2024 miktim@mail.ru
 */
// package json;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Arrays;
import org.miktim.json.JSON;
import org.miktim.json.Json;

public class JsonTest {

    static void log(Object... s) {
        for(int i=0; i<s.length; i++)
            System.out.print(String.valueOf(s[i]) + " ");
        System.out.println();
    }
    public static void main(String[] args) throws Exception {
        String path = (new File(".")).getAbsolutePath();
        if (args.length > 0) {
            path = args[0];
        }

        log("\n\rJson class test");
        log(JSON.toJSON(null));

        log("\n\rTest escape/unescape string:");
        String unescaped = new String(new char[]{0x8, 0x9, 0xA, 0xC, 0xD, 0x22, 0x2F, 0x5C, 0, '-', 0x1F, 0xd834, 0xdd1e});
        String escaped = JSON.toJSON(unescaped);
        log(escaped);
        log(unescaped.equals(JSON.fromJSON(escaped)) ? "OK" : "FAIL");

        log("\n\rTest parse/generate literals:");
        String fmt = "%s -> %s";
        String sl = "\"" + escaped + "\"";
        log(String.format(fmt, sl, JSON.toJSON(JSON.fromJSON(sl))));
        sl = "+3.141592653589793238462643383279";
        log(String.format(fmt, sl, JSON.toJSON(JSON.fromJSON(sl))));
        sl = "-3141592653589793.238462643383279E-15";
        log(String.format(fmt, sl, JSON.toJSON(JSON.fromJSON(sl))));
        sl = "3141592653589793238462643383279e-30";
        log(String.format(fmt, sl, JSON.toJSON(JSON.fromJSON(sl))));
        sl = "3141592653589793238462643383279e-100";
        log(String.format(fmt, sl, JSON.toJSON(JSON.fromJSON(sl))));
        sl = "3141592653589793238";
        log(String.format(fmt, sl, JSON.toJSON(JSON.fromJSON(sl))));
        for (String s : "true,false,null".split(",")) {
            log(String.format(fmt, s, JSON.toJSON(JSON.fromJSON(s))));
        }

// ParseExceptions
//        log(JSON.fromJSON("31415926535897932384")); // number format
//        log(JSON.fromJSON("\"asfas\\uD83\uDD1E\"")); // unparseable u-escaped char
//        log(JSON.fromJSON("\"\uD834\\uDD1\"")); // unparseable u-escaped char
//        log(JSON.fromJSON("123e")); // number format
//        log(JSON.fromJSON("123 e")); // EOT Expected
//        log(JSON.fromJSON("{{}}"); // name expected
//        log(JSON.fromJSON("{\"Latitude\":  37.371991\n\"")); // "}" expected
//        log(JSON.fromJSON("b123")); // unexpected char
//        log(JSON.fromJSON("falsen")); // unknown literal

 //       float[][] f = new float[][]{{1.1f, 2.2f}, {3.3f, 4.4f}};
 //       JSON.castTo(<T> f);

log("\n\rTest JSON object constructors (key pairs, JSON text):");
        Json json = new Json("One", 1, "Two", 2, 3, "Three", null, null,
                "Nested Json", new Json("array",
                        new float[][]{{1.1f, 2.2f}, {3.3f, 4.4f}}));
        log(json);
        String jsonText = json.toString();
        json = new Json(jsonText);
        log(json);
        log("List members: " + JSON.toJSON(json.listNames()));
        log("\"null\" member exists?: " + json.exists("null"));
        log("array[5] exists?: " + json.getJson("Nested Json").exists("array", 5));
        log("array[1] exists?: " + json.getJson("Nested Json").exists("array", 1));
        
        log("\n\rTest member getters:");
        log(json.getNumber("One"));
// java.lang.ClassCastException        
//        log(json.getString("Two"));
        log(json.toJSON("Two"));
        log(json.getString("3"));
        log(json.getJson("Nested Json").getArray("array", 1).getClass());
        log(Arrays.toString(json.getJson("Nested Json").getArray("array", 1)));
        log(json.getJson("Nested Json").getNumber("array", 1, 1));

        log("\n\rTest JSON typecast.");
        json = (new Json())
                .set("String", unescaped)
                .set("empty Json", new Json())
                .set("intArray", new int[][]{{0, 1, 2}, {3, 4, 5, 6}})
                .set(null, null)
                .set("boolean", true)
                .set("double", 3.141592653589793238462643383279)
                .set("float", 3.141592653589793238462643383279f)
                .set("long", 3141592653589793238L)
                .set("int", 314159265)
                .set("byte", (byte) 31)
//                .set("char", 'c')
                .set("BigDecimal", new BigDecimal("3.141592653589793238462643383279")
                );
        log("\n\rJSON object:");
        log(json);
        log("\n\rList members: " + JSON.toJSON(json.listNames()));
//        Json jsonn = json.normalize();
//        log("\n\rJSON object AFTER normalization:");
//        log(jsonn);
//        log("\n\rClasses of members before/after normalization:");
        log("\n\rClasses of members:");
        for (String memberName : json.listNames()) {
            if (json.get(memberName) != null) {
                log("\"" + memberName + "\"\tinstance of: "
                        + json.get(memberName).getClass().getSimpleName()
//                        + " / "
//                        + jsonn.get(memberName).getClass().getSimpleName()
                );
            } else {
                log("\"" + memberName + "\"\tis null");
            }
        }
        
        log("\n\rGenerate JSON with 2 spaces:");
        log(JSON.toJSON(json,2));
        
        log("\n\rTest JSON with Java array:");
        int[][] intArray = new int[][]{{0, 1, 2}, {3, 4, 5, 6}};
        json = new Json("array", intArray);
        log(json);
        log(json.get("array").getClass().getSimpleName());
        log(json.get("array", 1).getClass().getSimpleName());
        log(json.getArray("array", 1).getClass().getSimpleName());
//         
        log(Arrays.toString(json.getArray("array", 1)));
        log(json.get("array", 1, 3).getClass().getSimpleName());
        int i = json.getNumber("array", 1, 3).intValue();
        log(i);
/*
        log("Normalized object:");
        log(json = json.normalize());

// member "array" is instance of Object[Object[], Object[]]        
        log(json.get("array").getClass().getSimpleName());
        log(json.get("array", 1).getClass().getSimpleName());
        log(json.getArray("array", 1).getClass().getSimpleName());
        log(Arrays.toString(json.getArray("array", 1)));
        log(json.get("array", 1, 3).getClass().getSimpleName());
        i = json.getNumber("array", 1, 3).intValue();
        log(i);
        log(JSON.toJSON(json.castMember(int[][].class,"array")));
*/
// TODO use Json.converter against toString
/*
        log("\n\rTest generator with other Java objects:");
        log("  1. HashMap with int[3], Date, String and File entries:");
        HashMap<String,Object> hashMap = new HashMap<String,Object>();
        hashMap.put("int[]",new int[3]);
        hashMap.put("Date",new Date());
        hashMap.put("String", "15\"");
        hashMap.put("File", new File(path, "json.json"));
        String s = JSON.toJSON(hashMap);
        log(s);
        log("  2. ArrayList with int[3], Date, String and File entries:");
        ArrayList<Object> arrayList = new ArrayList<>();
        arrayList.add(new int[3]);
        arrayList.add(new Date());
        arrayList.add("15\"");
        arrayList.add(new File(path, "json.json"));
        s = JSON.toJSON(arrayList);
        log(s);
        Object obj = JSON.fromJSON(s);
        log("is Array?: " + (obj.getClass().isArray()));
*/
        Object obj;
        log("\n\rTest examples:");
// examples from RFC 8259 https://datatracker.ietf.org/doc/rfc8259/?include_text=1
        log(JSON.fromJSON("\"\\uD834\\uDD1E\"")); // G-clef
        log(JSON.fromJSON("3.141592653589793238462643383279"));
        log(JSON.fromJSON("null"));

        log("\n\rTest example 1:");
        String example1 = "\n{\n"
                + "        \"Image\": {\n"
                + "            \"Width\":  800,\n"
                + "            \"Height\": 600,\n"
                + "            \"Title\":  \"View from 15th Floor\",\n"
                + "            \"Thumbnail\": {\n"
                + "                \"Url\":    \"http://www.example.com/image/481989943\",\n"
                + "                \"Height\": 125,\n"
                + "                \"Width\":  100\n"
                + "            },\n"
                + "            \"Animated\" : false,\n"
                + "            \"IDs\": [116, 943, 234, 38793]\n"
                + "          }\n"
                + "      } ";
        json = new Json(example1);
        log(json.get("Image"));
        log(json.getJson("Image").set("Thumbnail", 256)); // replace JSON object with number
        log(json.getJson("Image").remove("Thumbnail")); // remove member

        log("\n\rTest example 2:");
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
        Object[] array = (Object[]) JSON.fromJSON(example2);
        log(JSON.toJSON(array));
        log(JSON.toJSON(array[1]));

// Example from https://docs.oracle.com/en/database/oracle/oracle-database/12.2/adjsn/json-data.html#GUID-FBC22D72-AA64-4B0A-92A2-837B32902E2C        
        log("\n\rTest example 3:");
        try (FileInputStream in = new FileInputStream(new File(path, "Example3.json"))) {
            json = new Json(in);
        }
        log(json);
        log(json.get("AllowPartialShipment"));
//        Json[] jsons = (Json[])json.getArray("LineItems");
        Json[] jsons = json.castMember(Json[].class,"LineItems");
        json = jsons[1];
        log(json.get("Quantity"));
        json = (Json) json.get("Part");
        log(json);
        log(json.get("Description"));
        float f = json.castMember(float.class,"UnitPrice");
        log(f);
        long upcCode = json.castMember(long.class, "UPCCode");
        log(upcCode);
        json.set("Description", "Naked Gun")
                .set("UnitPrice", 59.91)
                .set("UPCCode", 72982619358L);
        log(json);
        log("\n\rTime(ms) for generation/parsing/casting an array of random values (double[1000]):");
        double[] ad = new double[1000];
        for (i = 0; i < ad.length; i++) {
            ad[i] = Math.random() * ad.length;
        }
        long start = System.currentTimeMillis();
        String sa = JSON.toJSON(ad);
        log("Generation: " + (System.currentTimeMillis() - start));

        start = System.currentTimeMillis();
        Object[] an = (Object[]) JSON.fromJSON(sa);
        log("Parsing: " + (System.currentTimeMillis() - start));

        start = System.currentTimeMillis();
        JSON.toJSON(an);
        log("Normalized object generation: " + (System.currentTimeMillis() - start));

        start = System.currentTimeMillis();
        JSON.cast(double[].class, an);
        log("Normalized object casting to double[]: " + (System.currentTimeMillis() - start));

        log("\n\rAverage time(ms) of example 3 parsing/generation:");
        jsonText = new String(Files.readAllBytes(
                FileSystems.getDefault().getPath(path, "Example3.json")));
        
        start = System.currentTimeMillis();
        obj = null;
//        String s;
        for (i = 0; i < 100; i++) {
            obj = JSON.fromJSON(jsonText);
        }
        log("Parsing: " + ((float)(System.currentTimeMillis() - start))/100);

        start = System.currentTimeMillis();
        for (i = 0; i < 100; i++) {
            String s = JSON.toJSON(obj);
        }
        log("Generation " + ((float)(System.currentTimeMillis() - start))/100);

    }
}
