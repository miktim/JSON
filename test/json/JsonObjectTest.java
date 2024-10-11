/*
 * JsonObject test, MIT (c) 2022-2024 miktim@mail.ru 
 */
//package json; 

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import org.miktim.json.Json;
import org.miktim.json.JsonConverter;
import org.miktim.json.JsonObject;

public class JsonObjectTest {

    public static void log(Object s) {
        System.out.println(String.valueOf(s));
    }

    JsonObjectTest() {
    }

    static class D {

        protected String pro_dS = "protected dS";
        public int pub_di = 123;
        String dS = "";
        public C pub_dC = new C();
        private String pri_dS;

        D() {
        }

        D(int i, String s) {
            pub_di = i;
            dS = s;
        }
    }

    static class C extends JsonObject {
// unload/load run in the context of an instance
// of an object, so private fields are visible

        int ci = 0;
        String cS = "some string";

        C() {
        }

        C(int i, String s) {
            ci = i;
            cS = s;
        }
    }

    class B extends A {

        double bd = 123.123;
        D bD = new D();

        B() {
//            setIgnored(new String[0]); // reset ignored
        }

        @Override
        protected Object replacer(String name, Object value) {
            log(name);
            try {
                if (isClassName(name)) { // first call
// ignored field was declared in A
//                log("Ignored: " + Arrays.toString(getIgnored()));
// private a.pri_ad field is invisible here. Unload by getter
// on first call we MUST return Json object
                    return (new Json()).set("ad", get_ad());
                } else if (name.endsWith(".bD")) {
                    return toJson(value);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return super.replacer(name, value);
        }

        @Override
        protected Object reviver(String name, Object value) {
            try {
                if (isClassName(name)) {
// load a.priv_ad by setter
                    set_ad(castMember("ad", (Json) value, get_ad()));
                } else if (name.endsWith(".bD")) {
                    return fromJson(bD, (Json) value);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return super.reviver(name, value);
        }
    }

    class A extends JsonObject {

        public String pub_aS = "public aS";
        final String fin_aS = "final aS";
        private String pri_aS = "private aS";
        protected String pro_aS = "protected aS";
        public transient String tra_aS = "transient aS";
        private double pri_ad = 3.14159;
        int[][] ai = new int[][]{{1, 2}, {3, 4}};
        final HashMap<Integer, C> maC = new HashMap<>();

        A() {
            maC.put(3, new C(1, "one"));
            maC.put(2, new C(2, "two"));
            maC.put(1, new C(3, "three"));
//            setIgnored(new String[]{"pub_aS"});
        }

        public double get_ad() {
            return pri_ad;
        }

        public void set_ad(double ad) {
            this.pri_ad = ad;
        }

        @Override
        protected Object replacer(String name, Object value) {
//            try {
//                log(name);
            if (isClassName(name)) {
//                log("Ignored: " + Arrays.toString(getIgnored()));
            } else if (name.endsWith(".maC") && maC != null) {
// unload Map
                Json aCjson = new Json();
                for (Map.Entry<Integer, C> entry : maC.entrySet()) {
                    aCjson.set(entry.getKey(), entry.getValue().toJson());
                }
                return aCjson;
            }
//            } catch (IllegalArgumentException | IndexOutOfBoundsException ex) {
//                ex.printStackTrace();
//            }
            return super.replacer(name, value);
        }

        @Override
        protected Object reviver(String name, Object value) {
            try {
                if (name.endsWith(".maC") && value != null) {
// load Map
                    maC.clear();
                    Json aCjson = (Json) value;
                    for (String key : aCjson.listNames()) {
                        C c = fromJson(new C(), aCjson.getJson(key));
                        maC.put(new Integer(key), c);
                    }
                    return IGNORED; // aC already loaded
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return super.reviver(name, value);
        }

    }
    A a = new A();
    B b = new B();
    D d = new D(456, "default dS");

    public static void main(String[] args) throws Exception {
        log("\n\rJsonObject test");
        JsonObjectTest t = new JsonObjectTest();

        log("\n\r A instance toJson/fromJson:");
        String s = t.a.toJson().toJSON();
        log(s);
        t.a.fromJson(new Json(s));
        log(t.a.toJson());

        log("\n\r B instance toJson/fromJson :");
        s = t.b.toJson().toJSON();
        log(s);
        t.b.fromJson(new Json(s));
        
        JsonConverter converter = new JsonConverter();
        log("\n\r D instance with default converter:");
        Json j = converter.toJson(t.d);
        log(j);
        log(" load updated Json into new D instance");
        j.set("pro_dS", "updated dS").set("pub_di", 0);
        t.d = converter.fromJson(new D(), j);
        log(converter.toJson(t.d));

        log("\n\r B instance with default converter:");
        j = converter.toJson(t.b);
        log(j);

        log("\n\r java.lang.reflect.Modifier instance with default converter:");
        j = converter.toJson(new Modifier());
        log(j);

    }

}
