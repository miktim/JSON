/*
 * JsonCastTest. MIT (c) 2022-2024 miktim@mail.ru
 */

//package json;
import java.math.BigDecimal;
import java.util.Arrays;
import org.miktim.json.JSON;
import org.miktim.json.Json;

public class JsonCastTest {
    public static class Foo {
        public int i = 0;
        public String s = "zero";
        public Foo() {};
        public Foo(int i, String s) {
            this.i = i;
            this.s = s;
        }
    }

    static void log(Object obj) {
        System.out.println(String.valueOf(obj));
    }

    public static void main(String[] args) throws Exception {
        log("JsonCast test\n\r");

// TODO chars, Json arrays        
        Json json = new Json(
                "i", 1234567,
                "bl", true,
                "bd", new BigDecimal("3.641592653589793238462643383279"),
                "bi", new BigDecimal("3141592653589793238"),
                "ast1", new String[]{"abc", "", "true", "def", null},
                "abl1", new boolean[]{true, false, true, false},
                "ad1", new double[]{1.5, 2.1},
                "e1", new Object[]{},
                "ao2", new Object[][]{{8, 9.9, 10, 11.6}, {12, 0.0}},
                "aFoo", new Foo[]{new Foo(1,"one"), new Foo(2,"two"), new Foo(3, "three")}
        );

        log(JSON.toJSON(json,-1));

        byte b = 0;
        short s = 0;
        int i = 0;
        long l = 0L;
//        float f = 0;
        double d = 0;
        Integer iI = 0;
//        char c = 0;
        boolean bl = false;
        bl = JSON.cast(boolean.class, null); // false
        String st = "";

        log("\n\rMember \"i\" to byte, short, int, double:");
        b = JSON.cast(byte.class, json.get("i"));
        log(b);
        s = JSON.cast(s, json.get("i"));
        log(s);
        i = JSON.cast(int.class, json.get("i"));
        log(i);
        d = JSON.cast(d, json.get("i"));
        log(d);
        log("\n\rMember \"bi\" to int, long:");
        i = JSON.cast(0, json.get("bi")); // use constant as sample
        log(i);
        l = JSON.cast(l, json.get("bi"));
        log(l);
        log("\n\rMember \"bd\" to int, String:");
        i = JSON.cast(0, json.get("bd")); // use constant as sample
        log(i);
        st = JSON.cast("", json.get("bd"));// use constant as sample
        log(st);

//        log("\n\rMember \"c\" to char:");
//        c = JSON.cast(json.get("c"), c);
//        log(c);
        log("\n\rMember \"bl\" to boolean:");
        log(JSON.cast(json.get("bl"), bl));

        float[] af1 = new float[0];
        int[][] ai2 = new int[0][0];
        Integer aI1[] = new Integer[]{};
        char[] ac1 = new char[0];
        char[][] ac2 = new char[0][0];
        char[][][] ac3 = new char[][][]{{{}}};
        String[][] ast2 = new String[][]{{},{}};
        String[] ast1 = new String[0];
        boolean[] abl1 = new boolean[0];
        Foo[] aFoo = new Foo[0];
        Json[] aJson = new Json[0];
        log("\n\rCast arrays:");
//        log(JSON.toJSON(aFoo));
        aFoo = JSON.cast(Foo[].class, json.getArray("aFoo"));
        log(JSON.toJSON(aFoo) + " \"aFoo\" to " + aFoo.getClass().getSimpleName());
        aJson = JSON.cast(Json[].class, json.getArray("aFoo"));
        log(JSON.toJSON(aFoo) + " \"aFoo\" to " + aJson.getClass().getSimpleName());
        af1 = JSON.cast(af1, json.getArray("ad1"));
        log(JSON.toJSON(af1) + " \"af1\" to " + af1.getClass().getSimpleName());
        aI1 = JSON.cast(aI1, json.getArray("ad1"));
        log(JSON.toJSON(aI1) + " \"af1\" to " + aI1.getClass().getSimpleName());

        ai2 = JSON.cast(int[][].class, json.getArray("ao2"));
        log(JSON.toJSON(ai2) + " \"ao2\" to " + ai2.getClass().getSimpleName());
        Object o = json.getArray("ao2");
        ast2 = JSON.cast(ast2, json.getArray("ao2"));
        log(JSON.toJSON(ast2) + " \"ai2\" to " + ast2.getClass().getSimpleName());
// NullPointerException: "ac2" member does not exist
//        ac1 = JSON.cast( ac1, json.getArray("ast1"));
//        log(Arrays.deepToString(ac1) + " \"ast1\" to " + ac1.getClass().getSimpleName());
// TODO ClassCastException
        ac1 = JSON.cast(ac1, json.getArray("ast1"));
        log(Arrays.toString(ac1) + " \"ast1\" to " + ac1.getClass().getSimpleName());

        ast1 = JSON.cast(ast1, json.getArray("ast1")); // !!!null cast to "null"
        log(JSON.toJSON(ast1) + " \"ast1\" to " + ast1.getClass().getSimpleName());
        abl1 = JSON.cast(abl1, json.getArray("abl1"));
        log(JSON.toJSON(abl1) + " \"abl1\" to " + abl1.getClass().getSimpleName());
        abl1 = JSON.cast(abl1, json.getArray("ast1"));
        log(JSON.toJSON(abl1) + " \"ast1\" to " + abl1.getClass().getSimpleName());
// casting to null returns null, casting null to an array returns an empty array
// casting null to primitive returns initial value: boolen = false, char = (char) 0, int = 0 ...
        log("\n\rCasting null or to null:");
//        ai2 = JSON.cast(ai2, null);
//        log(Arrays.deepToString(ai2) + " " + ai2.getClass().getSimpleName());
        ai2 = null;
        ai2 = JSON.cast(ai2, json.getArray("ao2"));
        log(ai2);
        ai2 = JSON.cast(null, null);
        log(ai2);
// casting null to String returns "null"
        st = JSON.cast(st, null);
        log(st);
        i = JSON.cast(i, null);
        log(i);
        log((JSON.cast(int[].class, null)).getClass().getSimpleName());
// restore ai2 = null
        log("\n\rCast to an uninitialized array:");
        int[][] ai = JSON.cast(int[][].class, json.getArray("ao2"));
        log(Arrays.deepToString(ai) + " " + ai.getClass().getSimpleName());
    }
}
