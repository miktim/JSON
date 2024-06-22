/*
 * JsonObject test, MIT (c) 2022-2024 miktim@mail.ru 
 */
//package json; 

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.miktim.json.Json;
import org.miktim.json.JsonObject;

public class JsonObjectTest extends JsonObject {

    public static void log(Object s) {
        System.out.println(String.valueOf(s));
    }

    JsonObjectTest() {
    }

    static class D {
// unload/load run in the context of an instance
// of an object, so private fields are visible

        private int di = 123;
        private String dS = "";

        D() {
        }

        D(int i, String s) {
            di = i;
            dS = s;
        }
    }

    class C extends JsonObject {
// unload/load run in the context of an instance
// of an object, so private fields are visible

        private int ci = 0;
        private String cS = "";

        C() {
        }

        C(int i, String s) {
            ci = i;
            cS = s;
        }
    }

    class B extends A {

        private double bd = 123.123;

        @Override
        protected Object replacer(String name, Object value) {
            if (isClassName(name)) {
// ignored field was declared in A
                log("Ignored: " + Arrays.toString(getIgnored()));
// private a.ad is invisible here. unload by getter
                return (new Json()).set("ad", get_ad());
            }
            return super.replacer(name, value);
        }

        @Override
        protected Object reviver(String name, Object value) {
            if (isClassName(name)) {
// load by setter
                set_ad(castMember("ad", (Json) value, get_ad()));
            }
            return super.reviver(name, value);
        }
    }

    class A extends JsonObject {

        public String aS = "public aS";
        static final String ASF = "static final ASF";
        private double ad = 3.14159;
        private int[] ai = new int[]{1, 2, 3};
        private String aSp = "private aSp";
        HashMap<Integer, C> aC = new HashMap<>();
        protected int aip = 3456;

        A() {
            aC.put(3, new C(1, "one"));
            aC.put(2, new C(2, "two"));
            aC.put(1, new C(3, "three"));
            setIgnored(new String[]{"aS"});
        }

        public double get_ad() {
            return ad;
        }

        public void set_ad(double ad) {
            this.ad = ad;
        }

        @Override
        protected Object replacer(String name, Object value) {
//            if (isClassName(name))
//                log("Ignored: " + Arrays.toString(getIgnored()));
            if (name.equals("aC") && aC != null) {
// unload map
                Json aCjson = new Json();
                for (Map.Entry<Integer, C> entry : aC.entrySet()) {
                    try {
                        aCjson.set(entry.getKey().toString(), entry.getValue().toJson());
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
// load map
                aC.clear();
                Json aCjson = (Json) value;
                for (String key : aCjson.listNames()) {
                    try {
                        C c = (C) (new C()).fromJson(aCjson.get(key));
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

        log("\n\r A toJSON/fromJSON:");
        String s = t.a.toJSON();
        log(s);
        t.a.fromJSON(s);
        log(t.a.toJSON());

        log("\n\r B toJSON/fromJSON:");
        s = t.b.toJSON();
        log(s);
        t.b.fromJSON(s);

        log("\n\r D Test adapter:"); 
//        ObjectAdapter adapter = new ObjectAdapter();
        Json j = (Json) JsonObject.defaultAdapter.toJson(t.d);
        log(j);
        j.set("dS", "updated dS").set("di", 0);
        t.d = JsonObject.defaultAdapter.fromJson(new D(), j);
        log(JsonObject.defaultAdapter.toJson(t.d));
    }

}
