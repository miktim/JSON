/*
 * JSONAdapterTest. MIT (c) 2022 miktim@mail.ru
 */

//package json;

import java.math.BigDecimal;
import java.util.Arrays;
import org.miktim.json.JSON;
import org.miktim.json.JSONAdapter;

public class JSONAdapterTest {

    static void log(Object s) {
        System.out.println(String.valueOf(s));
    }

    public static void main(String[] args) throws Exception {
        log("JSONAdapter test\n\r");

        JSON json = new JSON(
                "i", 12345,
                "c", 'c',
                "bl", true,
                "bd", new BigDecimal("3.141592653589793238462643383279"),
                "bi", new BigDecimal("3141592653589793238462643383279"),
                "ac2", new char[][]{{'b', 'c'}, {'d', 'e', 'f', 'g'}},
                "ac1", new String[]{"abc", "", "def", null},
                "abl1", new boolean[]{true, false, true, false},
                "ai1", new int[]{1, 2},
                "ai2", new int[][]{{8, 9, 10, 11}, {12, 13}}
        );

        json = json.normalize();
        log(json);

        byte b = 0;
        short s = 0;
        int i = 0;
//        long l = 0;
//        float f = 0;
        double d = 0;
        Integer iI = 0;
        char c = 0;
        boolean bl = JSONAdapter.castTo(null, boolean.class);
        String st = "";

        log("\n\rMember \"i\" to byte, short, int, double:");
        b = JSONAdapter.castTo(json.get("i"), byte.class);
        log(b);
        s = JSONAdapter.castTo(json.get("i"), s);
        log(s);
        i = JSONAdapter.castTo(json.get("i"), i);
        log(i);
        d = JSONAdapter.castTo(json.get("i"), d);
        log(d);
        log("\n\rMember \"bi\" to int, Integer. Member \"bd\" to int, String:");
        i = JSONAdapter.castTo(json.get("bi"), 0); // use constant as sample
        log(i);
        iI = JSONAdapter.castTo(json.get("bi"), iI);
        log(iI);
        i = JSONAdapter.castTo(json.get("bd"), 0); // use constant as sample
        log(i);
        st = JSONAdapter.castTo(json.get("bd"), "");// use constant as sample
        log(st);

        log("\n\rMember \"c\" to char:");
        c = JSONAdapter.castTo(json.get("c"), c);
        log(c);

        log("\n\rMember \"bl\" to boolean:");
        log("Before: " + bl);
        bl = JSONAdapter.castTo(json.get("bl"), bl);
        log("After: " + bl);

        int[] ai1 = new int[0];
        int[][] ai2 = new int[0][0];
        Integer aI1[] = new Integer[]{};
        char[] ac1 = new char[0];
        char[][] ac2 = new char[0][0];
        char[][][] ac3 = new char[][][]{{{}}};
        String[][] ast2 = new String[][]{{}};
        String[] ast1 = new String[0];
        boolean[] abl1 = new boolean[0];
        
        log("\n\rCast arrays:");
        ai1 = JSONAdapter.castTo(json.getArray("ai1"), ai1);
        log(Arrays.toString(ai1) + " " + ai1.getClass().getSimpleName());
        aI1 = JSONAdapter.castTo(json.getArray("ai1"), aI1);
        log(Arrays.deepToString(aI1) + " " + aI1.getClass().getSimpleName());
        ai2 = JSONAdapter.castTo(json.getArray("ai2"), int[][].class);
        log(Arrays.deepToString(ai2) + " " + ai2.getClass().getSimpleName());
        ac2 = JSONAdapter.castTo(json.getArray("ac2"), ac2);
        log(Arrays.deepToString(ac2) + " " + ac2.getClass().getSimpleName());
        ac1 = JSONAdapter.castTo(json.getArray("ac1"), ac1);
        log(Arrays.toString(ac1) + " " + ac1.getClass().getSimpleName());
        ast2 = JSONAdapter.castTo(json.getArray("ai2"), ast2);
        log(Arrays.deepToString(ast2) + " " + ast2.getClass().getSimpleName());
        ast1 = JSONAdapter.castTo(json.getArray("ac1"), ast1); // !!!null cast to "null"
        log(Arrays.deepToString(ast1) + " " + ast1.getClass().getSimpleName());
        abl1 = JSONAdapter.castTo(json.getArray("abl1"), abl1);
        log(Arrays.toString(abl1) + " " + abl1.getClass().getSimpleName());

// casting char[][] to char[][][] throws java.lang.IllegalArgumentException: array element type mismatch       
//        ac3 = JSONAdapter.castTo(json.getArray("ac2"), ac3);
// casting char[][] to char[] throws java.lang.ClassCastException: [Ljava.lang.Object; cannot be cast to java.lang.String
//        ac1 = JSONAdapter.castTo(json.getArray("ac2"), ac1);
// casting to null returns null, casting null to an array returns an empty array
// casting null to primitive returns initial value: boolen = false, char = (char) 0, int = 0 ...
        log("\n\rCasting null or to null:");
        ai2 = JSONAdapter.castTo(null, ai2);
        log(Arrays.deepToString(ai2) + " " + ai2.getClass().getSimpleName());
        ai2 = null;
        ai2 = JSONAdapter.castTo(json.getArray("ai2"), ai2);
        log(ai2);
        ai2 = JSONAdapter.castTo(null, null);
        log(ai2);
// casting null to String returns "null"
        st = JSONAdapter.castTo(null, st);
        log(st);
        i = JSONAdapter.castTo(null, i);
        log(i);
// restore ai2 = null
        log("Cast to null array:");
        ai2 = JSONAdapter.castTo(json.getArray("ai2"), int[][].class);
        log(Arrays.deepToString(ai2) + " " + ai2.getClass().getSimpleName());

    }
}
