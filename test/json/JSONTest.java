/*
 * JSONTest, MIT (c) 2020-2022 miktim@mail.ru
 */
// package json;

import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import json.A;
import org.miktim.json.JSON;
import org.miktim.json.JSONAdapter;

public class JSONTest {

    static void log(Object s) {
        System.out.println(s);
    }

    public static void main(String[] args) throws Exception {
        String path = (new File(".")).getAbsolutePath();
        if (args.length > 0) {
            path = args[0];
        }

        log("\n\rJSON class test");
        JSON.generate(null);

        log("\n\rTest escape/unescape string:");
        String unescaped = new String(new char[]{0x8, 0x9, 0xA, 0xC, 0xD, 0x22, 0x2F, 0x5C, 0, '-', 0x1F, 0xd834, 0xdd1e});
        String escaped = JSON.escapeString(unescaped);
        log(escaped);
        log(unescaped.equals(JSON.unescapeString(escaped)) ? "OK" : "FAIL");

// ParseExceptions        
//        log(JSON.parse("\"asfas\\uD83\uDD1E\"")); // unparseable u-escaped char
//        log(JSON.parse("\"\uD834\\uDD1\"")); // unparseable u-escaped char
//        log(JSON.parse("123e")); // unparseable number
//        log(JSON.parse("123 e")); // EOT Expected
//        log(JSON.parse("{{}}"); // name expected
//        log(JSON.parse("{\"Latitude\":  37.371991\n\"")); // "}" expected
//        log(JSON.parse("b123")); // unexpected char
//        log(JSON.parse("falsen")); // unknown literal
        log("\n\rTest constructors:");
        JSON json = new JSON("One", 1, "Two", 2, 3, "Three", null, null,
                "Nested", new JSON("array",
                        new float[][]{{1.1f, 2.2f}, {3.3f, 4.4f}}));
        log(json);
        String jsonText = json.toString();
        json = new JSON(jsonText);
        log(json);

        log("\n\rTest getting members:");
        log(json.getNumber("One"));
// java.lang.ClassCastException        
//        log(json.getString("Two"));
        log(json.generate("Two"));
        log(json.getString("3"));
        log(Arrays.toString(json.getJSON("Nested").getArray("array", 1)));
        log(json.getJSON("Nested").getNumber("array", 1, 1).intValue());

        log("\n\rTest JSON typecast.");
        json = (new JSON())
                .set("String", unescaped)
                .set("emptyJSON", new JSON())
                .set("intArray", new int[][]{{0, 1, 2}, {3, 4, 5, 6}})
                .set(null, null)
                .set("boolean", true)
                .set("double", 3.141592653589793238462643383279)
                .set("float", 3.141592653589793238462643383279f)
                .set("long", 3141592653589793238L)
                .set("int", 314159265)
                .set("byte", (byte) 31)
                .set("char", 'c')
                .set("BigDecimal", new BigDecimal("3.141592653589793238462643383279"))
                .set("Object", new A());
        log("\n\rList members: " + json.listNames());
        log("\n\rJSON object BEFORE normalization:");
        log(json);
        JSON jsonn = json.normalize();
        log("\n\rJSON object AFTER normalization:");
        log(jsonn);
        log("\n\rClasses of members before/after normalization:");
        for (String memberName : json.listNames()) {
            if (json.get(memberName) != null) {
                log("\"" + memberName + "\"    instance of: "
                        + json.get(memberName).getClass().getSimpleName()
                        + " / "
                        + jsonn.get(memberName).getClass().getSimpleName()
                );
            } else {
                log("\"" + memberName + "\" is null");
            }
        }

        log("\n\rTest JSON with Java array:");
        int[][] intArray = new int[][]{{0, 1, 2}, {3, 4, 5, 6}};
        json = new JSON("array", intArray);
        log(json);
// member "array" is instance of int[][]        
        log(json.get("array").getClass().getSimpleName());
        log(json.get("array", 1).getClass().getSimpleName());
        log(json.getArray("array", 1).getClass().getSimpleName());
        log(Arrays.toString(json.getArray("array", 1)));
        log(json.get("array", 1, 3).getClass().getSimpleName());
        int i = json.getNumber("array", 1, 3).intValue();
        log(i);

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

        log("\n\rTest generator with other Java objects:");
        log("  HashMap with int[3], Date and File entries:");
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("int[]",new int[3]);
        hashMap.put("Date",new Date());
        hashMap.put("File", new File(path, "json.json"));
        String s = JSON.generate(hashMap);
        log(s);
        log("  ArrayList with int[3], Date and File entries:");
        ArrayList<Object> arrayList = new ArrayList<>();
        arrayList.add(new int[3]);
        arrayList.add(new Date());
        arrayList.add(new File(path, "json.json"));
        s = JSON.generate(arrayList);
        log(s);
        Object obj = JSON.parse(s);
        log("is Array?: " + (obj instanceof Object[]));//obj.getClass().isArray());

        log("\n\rTest examples:");
// examples from RFC 8259 https://datatracker.ietf.org/doc/rfc8259/?include_text=1
        log(JSON.parse("\"\\uD834\\uDD1E\"")); // G-clef
        log(JSON.parse("3.141592653589793238462643383279"));
        log(JSON.parse("null"));

        log("\n\rTest example 1:");
        String example1 = "\n{\n"
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
        json = new JSON(example1);
        log(json.get("Image"));
        log(json.getJSON("Image").set("Thumbnail", 256)); // replace JSON object with number
        log(json.getJSON("Image").remove("Thumbnail")); // remove member

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
        Object[] array = (Object[]) JSON.parse(example2);
        log(JSON.generate(array));
        log(JSON.generate(array[1]));

// Example from https://docs.oracle.com/en/database/oracle/oracle-database/12.2/adjsn/json-data.html#GUID-FBC22D72-AA64-4B0A-92A2-837B32902E2C        
        log("\n\rTest example 3:");
        try (FileReader reader = new FileReader(new File(path, "json.json"))) {
            json = new JSON(reader);
        }
        log(json);
        log(json.get("AllowPartialShipment"));
        JSON[] jsons = json.cast("LineItems", JSON[].class);
        json = jsons[1];
        log(json.get("Quantity"));
        json = (JSON) json.get("Part");
        log(json);
        log(json.get("Description"));
        float f = json.cast("UnitPrice", float.class);
        log(f);
        long upcCode = json.cast("UPCCode", long.class);
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
        String sa = JSON.generate(ad);
        log("Generation: " + (System.currentTimeMillis() - start));

        start = System.currentTimeMillis();
        Object[] an = (Object[]) JSON.parse(sa);
        log("Parsing: " + (System.currentTimeMillis() - start));

        start = System.currentTimeMillis();
        JSON.generate(an);
        log("Normalized object generation: " + (System.currentTimeMillis() - start));

        start = System.currentTimeMillis();
        JSONAdapter.cast(an, double[].class);
        log("Normalized object casting to double[]: " + (System.currentTimeMillis() - start));

        log("\n\rAverage time(ms) of example 3 parsing/generation:");
        jsonText = new String(Files.readAllBytes(
                FileSystems.getDefault().getPath(path, "json.json")));
        
        start = System.currentTimeMillis();
        for (i = 0; i < 100; i++) {
            obj = JSON.parse(s);
        }
        log("Parsing: " + ((float)(System.currentTimeMillis() - start))/100);

        start = System.currentTimeMillis();
        for (i = 0; i < 100; i++) {
            s = JSON.generate(obj);
        }
        log("Generation " + ((float)(System.currentTimeMillis() - start))/100);

    }
}
