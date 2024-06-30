/*
 * JsonObject test, MIT (c) 2022-2024 miktim@mail.ru 
 */
//package json; 

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.miktim.json.Json;
import org.miktim.json.JsonAdapter;
import org.miktim.json.JsonObject;

public class JsonObjectTest {

    public static void log(Object s) {
        System.out.println(String.valueOf(s));
    }

    JsonObjectTest() {
    }

    static class D {
// unload/load run in the context of an instance
// of an object, so private fields are visible
        protected String pro_dS = "protected dS";
        public int pub_di = 123;
        String dS = "";
        private String pri_dS;
        
        D() {
        }

        D(int i, String s) {
            pub_di = i;
            dS = s;
        }
    }

    class C extends JsonObject {
// unload/load run in the context of an instance
// of an object, so private fields are visible

        private int pri_ci = 0;
        private String pri_cS = "";

        C() {
        }

        C(int i, String s) {
            pri_ci = i;
            pri_cS = s;
        }
    }

    class B extends A {
        
        private double pri_bd = 123.123;
        B() {
            setIgnored(new String[0]); // reset ignored
        }

        @Override
        protected Object replacer(String name, Object value) {
            if (isClassName(name)) { // first call
// ignored field was declared in A
                log("Ignored: " + Arrays.toString(getIgnored()));
// private a.pri_ad is invisible here. unload by getter
// on first call we MUST return Json object
                return (new Json()).set("ad", get_ad());
            }
            return super.replacer(name, value);
        }

        @Override
        protected Object reviver(String name, Object value) {
            if (isClassName(name)) {
// load a.priv_ad by setter
                set_ad(castMember("ad", (Json) value, get_ad()));
            }
            return super.reviver(name, value);
        }
    }

    class A extends JsonObject {

        public String pub_aS = "public aS";
        static final String fin_aS = "static final aS";
        private double pri_ad = 3.14159;
        private int[] pri_ai = new int[]{1, 2, 3};
        private String pri_aS = "private aS";
        final HashMap<Integer, C> aC = new HashMap<>();
        protected int pro_ai = 3456;

        A() {
            aC.put(3, new C(1, "one"));
            aC.put(2, new C(2, "two"));
            aC.put(1, new C(3, "three"));
            setIgnored(new String[]{"pub_aS"});
        }

        public double get_ad() {
            return pri_ad;
        }

        public void set_ad(double ad) {
            this.pri_ad = ad;
        }

        @Override
        protected Object replacer(String name, Object value) {
            if (isClassName(name))
                log("Ignored: " + Arrays.toString(getIgnored()));
            if (name.equals("aC") && aC != null) {
// unload Map
                Json aCjson = new Json();
                for (Map.Entry<Integer, C> entry : aC.entrySet()) {
                    try {
                        aCjson.set(entry.getKey(), entry.getValue().toJson());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                return aCjson;
            }
            return value;
        }

        @Override
        protected Object reviver(String name, Object value) {
            if (name.equals("aC") && value != null) {
// load Map
                aC.clear();
                Json aCjson = (Json) value;
                for (String key : aCjson.listNames()) {
                    try {
                        C c = (new C()).fromJson(aCjson.get(key));
                        aC.put(new Integer(key), c);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                return IGNORED; // aC already loaded
            }
            return value;
        }
    }
    A a = new A();
    B b = new B();
    D d = new D(456, "default dS");

    public static void main(String[] args) throws Exception {
        log("\n\rJSONObject test");
        JsonObjectTest t = new JsonObjectTest();

        log("\n\r A instance toJSON/fromJSON:");
        String s = t.a.toJSON();
        log(s);
        t.a.fromJSON(s);
        log(t.a.toJSON());

        log("\n\r B instance toJSON/fromJSON:");
        s = t.b.toJSON();
        log(s);
        t.b.fromJSON(s);

        log("\n\r D instance with default adapter:"); 
        Json j = JsonAdapter.defaultAdapter.toJson(t.d);
        log(j);
        j.set("dS", "updated dS").set("di", 0);
        t.d = JsonAdapter.defaultAdapter.fromJson(new D(), j);
        log(JsonAdapter.defaultAdapter.toJson(t.d));
        
        log("\n\r B instance with default adapter:"); 
        j = JsonAdapter.defaultAdapter.toJson(t.b);
        log(j);
        
        log("\n\r Modifierinstance with default adapter:"); 
        j = JsonAdapter.defaultAdapter.toJson(new Modifier());
        log(j);

    }

}
