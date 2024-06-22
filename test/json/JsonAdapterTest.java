/*
 * JsonAdapterTest. MIT (c) 2022-2024 miktim@mail.ru
 */

//package json;

import java.math.BigDecimal;
import java.util.Arrays;
import org.miktim.json.Json;
import org.miktim.json.JsonAdapter;

public class JsonAdapterTest {
 
    static void log(Object s) {
        System.out.println(String.valueOf(s));
    }

    public static void main(String[] args) throws Exception {
        log("JSONAdapter test\n\r");

        Json json = new Json(
                "i", 1234567,
                "c", 'c',
                "bl", true,
                "bd", new BigDecimal("3.641592653589793238462643383279"),
                "bi", new BigDecimal("3141592653589793238"),
                "ac2", new char[][]{{'b', 'c'}, {'d', 'e', 'f', 'g'}},
                "ac1", new String[]{"abc", "", "def", null},
                "abl1", new boolean[]{true, false, true, false},
                "af1", new double[]{1.5, 2.1},
                "ai2", new Object[][]{{8, 9.9, 10, 11.6}, {12, 0.0}}
        );

        json = json.normalize();
        log(json);

        byte b = 0;
        short s = 0;
        int i = 0;
        long l = 0L;
//        float f = 0;
        double d = 0;
        Integer iI = 0;
        char c = 0;
        boolean bl = JsonAdapter.cast(null, boolean.class); // false
        String st = "";

        log("\n\rMember \"i\" to byte, short, int, double:");
        b = JsonAdapter.cast(json.get("i"), byte.class);
        log(b);
        s = JsonAdapter.cast(json.get("i"), s);
        log(s);
        i = JsonAdapter.cast(json.get("i"), int.class);
        log(i);
        d = JsonAdapter.cast(json.get("i"), d);
        log(d);
        log("\n\rMember \"bi\" to int, long:");
        i = JsonAdapter.cast(json.get("bi"), 0); // use constant as sample
        log(i);
        l = JsonAdapter.cast(json.get("bi"), l);
        log(l);
        log("\n\rMember \"bd\" to int, String:");
        i = JsonAdapter.cast(json.get("bd"), 0); // use constant as sample
        log(i);
        st = JsonAdapter.cast(json.get("bd"), "");// use constant as sample
        log(st);

        log("\n\rMember \"c\" to char:");
        c = JsonAdapter.cast(json.get("c"), c);
        log(c);

        log("\n\rMember \"bl\" to boolean:");
        log(JsonAdapter.cast(json.get("bl"), bl));

        float[] af1 = new float[0];
        int[][] ai2 = new int[0][0];
        Integer aI1[] = new Integer[]{};
        char[] ac1 = new char[0];
        char[][] ac2 = new char[0][0];
        char[][][] ac3 = new char[][][]{{{}}};
        String[][] ast2 = new String[][]{{}};
        String[] ast1 = new String[0];
        boolean[] abl1 = new boolean[0];
        
        log("\n\rCast arrays:");
        af1 = JsonAdapter.cast(json.getArray("af1"), af1);
        log(Arrays.toString(af1) + " \"af1\" to " + af1.getClass().getSimpleName());
        aI1 = JsonAdapter.cast(json.getArray("af1"), aI1);
        log(Arrays.deepToString(aI1) + " \"af1\" to " + aI1.getClass().getSimpleName());
        ai2 = JsonAdapter.cast(json.getArray("ai2"), int[][].class);
        log(Arrays.deepToString(ai2) + " \"ai2\" to " + ai2.getClass().getSimpleName());
        ast2 = JsonAdapter.cast(json.getArray("ai2"), ast2);
        log(Arrays.deepToString(ast2) + " \"ai2\" to " + ast2.getClass().getSimpleName());
        ac2 = JsonAdapter.cast(json.getArray("ac2"), ac2);
        log(Arrays.deepToString(ac2) + " \"ac2\" to " + ac2.getClass().getSimpleName());
        ac1 = JsonAdapter.cast(json.getArray("ac1"), ac1);
        log(Arrays.toString(ac1) + " \"ac1\" to " + ac1.getClass().getSimpleName());
        ast1 = JsonAdapter.cast(json.getArray("ac1"), ast1); // !!!null cast to "null"
        log(Arrays.deepToString(ast1) + " \"ac1\" to " + ast1.getClass().getSimpleName());
        abl1 = JsonAdapter.cast(json.getArray("abl1"), abl1);
        log(Arrays.toString(abl1) + " \"abl1\" to " + abl1.getClass().getSimpleName());
// casting char[][] to char[][][] throws java.lang.IllegalArgumentException: array element type mismatch       
//        ac3 = JSONAdapter.castTo(json.getArray("ac2"), ac3);
// casting char[][] to char[] throws java.lang.ClassCastException: [Ljava.lang.Object; cannot be cast to java.lang.String
//        ac1 = JSONAdapter.castTo(json.getArray("ac2"), ac1);
// casting to null returns null, casting null to an array returns an empty array
// casting null to primitive returns initial value: boolen = false, char = (char) 0, int = 0 ...
        log("\n\rCasting null or to null:");
        ai2 = JsonAdapter.cast(null, ai2);
        log(Arrays.deepToString(ai2) + " " + ai2.getClass().getSimpleName());
        ai2 = null;
        ai2 = JsonAdapter.cast(json.getArray("ai2"), ai2);
        log(ai2);
        ai2 = JsonAdapter.cast(null, null);
        log(ai2);
// casting null to String returns "null"
        st = JsonAdapter.cast(null, st);
        log(st);
        i = JsonAdapter.cast(null, i);
        log(i);
// restore ai2 = null
        log("\n\rCast to an uninitialized array:");
        ai2 = JsonAdapter.cast(json.getArray("ai2"), int[][].class);
        log(Arrays.deepToString(ai2) + " " + ai2.getClass().getSimpleName());

    }
}
