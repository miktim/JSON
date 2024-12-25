/*
 * JsonObject test, MIT (c) 2022-2024 miktim@mail.ru 
 */
//package json; 

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import org.miktim.json.Json;
import org.miktim.json.JsonObject;

public class JsonObjectTest {

    static void log(Object... obj) {
        for (Object item : obj) {
            System.out.print(String.valueOf(item) + " ");
        }
        System.out.println();
    }

//    JsonObjectTest() {
//    }
    static class F extends E {

        public D objFD = new D();
        public String pubFs = "pub Fs";
        protected String proFs = "pro Fs";
        String defFs = "def Fs";
        private String privFs = "priv Fs";
// debug fields
        public transient ArrayList<String> namesReplaced = new ArrayList<>();
        public transient ArrayList<String> namesRevived = new ArrayList<>();
        public transient boolean enableNames = true;
// TODO: objC
        public C objFC = new C();

        F() {
        }

        @Override
        public Object replacer(String name, Object value) {
            if (enableNames) {
                log(name);
            }
            namesReplaced.add(name);
            return value;
        }

        @Override
        public Object reviver(String name, Object value) {
            if (enableNames) {
                log(name);
            }
            namesRevived.add(name);
            return value;
        }
    }

    static class E extends JsonObject {

        public String pubEs = "pub Es";
        protected String proEs = "pro Es";
        String defEs = "def Es";
        private String priEs = "pri Es";
        public transient String transEs = "trans Es";

        E() {
        }
    }

    static class D {

        protected String proDs = "pro Ds";
        public int pubDi = 123;
        String defDs = "def Ds";
        public C[] pubDaC = new C[]{new C(), new C(1, "one"), new C(2, "two")};
        private String priDs = "priv Ds";

        D() {
        }

        D(int i, String s) {
            pubDi = i;
            defDs = s;
        }
    }

    public static class C extends JsonObject {

        int defCi = 0;
        String defCs = "zero";

        public C() {
        }

        public C(int i, String s) {
            defCi = i;
            defCs = s;
        }
    }

    public static class B extends A {

        double defBd = 123.123;
        D defBD = new D();

        B() {
            super();
//            setIgnored(new String[0]); // reset ignored
        }

        @Override
        public Object replacer(String name, Object value) {
//            log(name);
            try {
                if (name.indexOf(':') < 0) { // first call
// ignored field was declared in A
//                log("Ignored: " + Arrays.toString(getIgnored()));
// private a.pri_ad field is invisible here. Unload by getter
// on first call we MUST return Json object
                    return (new Json()).set("set_aD", get_Ad());
                } else if (name.endsWith(":BD")) {
//                    return toJson(value);
                }
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
            return super.replacer(name, value); // TODO target?
        }

        @Override
        public Object reviver(String name, Object value) {
            try {
                if (name.indexOf(':') < 0) { // first call
// load a.priv_ad by setter
                    set_Ad(((Json)value).castMember(get_Ad(), "set_aD"));
                    return IGNORED;
                } else if (name.endsWith(":BD")) {
//                    return fromJson(bD, (Json) value);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return super.reviver(name, value); // TODO target?
        }
    }

    public static class A extends JsonObject {

//        public String pub_aS = "public aS";
//        final String fin_aS = "final aS";
//        private String pri_aS = "private aS";
//        public String pro_aS = "public aS";
        public transient String transAs = "trans As";
        private double priAd = 3.14159;
        int[][] defAai = new int[][]{{1, 2}, {3, 4}};
        HashMap<Integer, C> defAmpC = new HashMap<>();
        public C[] pubAaC = new C[]{new C(), new C(1, "one"), new C(2, "two")};

        A() {
            defAmpC.put(3, new C(1, "one"));
            defAmpC.put(2, new C(2, "two"));
            defAmpC.put(1, new C(3, "three"));
//            setIgnored(new String[]{"pub_aS"});
        }

        public double get_Ad() {
            return priAd;
        }

        public void set_Ad(double ad) {
            this.priAd = ad;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Object replacer(String name, Object value) {
            if (name.endsWith(":defAmpC") && value != null) {
// unload Map
                Json j = new Json();
                for (HashMap.Entry<Integer, C> entry : ((HashMap<Integer, C>) value).entrySet()) {
                    j.set(entry.getKey(), Json.converter.toJson(entry.getValue()));
                }
                return new Json("HashMap", j);
            }
            return value;
        }

        @Override
        public Object reviver(String name, Object value) {
            if (name.endsWith(":defAmpC") && value != null) {
// load Map
                defAmpC.clear();
                Json j = ((Json) value).getJson("HashMap");
                for (String key : j.listNames()) {
                    C c = Json.converter.fromJson(new C(), j.getJson(key));
                    defAmpC.put(new Integer(key), c);
                }
                return IGNORED; // aC already loaded
            }
//
            return value;
        }

    }

    public static void main(String[] args) throws Exception {
        log("\n\rJsonObject test");
        Json j;

        log("\n\r A instance to/from Json:");
        A a = new A();
        j = a.toJson();
        j.set("pubAaC", new C[]{new C(3, "three"), new C(4, "four")});
        j.set("defAai", new int[][]{{5},{6}});
        j.getJson("defAmpC").set("HashMap", new Json());
        a.fromJson(j);
        log(a);
        if (!j.toString().equals(a.toString())) {
            log("Failed! ");
            return;
        }
        F f = new F();
        log("\n\r F instance toJson:");
        j = Json.converter.toJson(f);
        log(j);
//        t.av.enableNames = false;
        log("\n\r F instance fromJson:"); // TODO inherited B fields???
        Json.converter.fromJson(f, j);
        if (f.namesReplaced.size() != f.namesRevived.size()) {
            log("Failed! ");
            return;
        }
        f.enableNames = false;
        log(Json.converter.toJson(f));
        log(" F instance with Json.converter");
        log(Json.converter.toJson(f));
        B b = new B();
        D d = new D(456, "default dS");

        log("\n\r B instance toJson/fromJson :");
// TODO JsonObject returns Object
        String s = ((Json) Json.converter.toJson(b)).toJSON();
        log(s);
        Json.converter.fromJson(b, new Json(s));

        log(" B instance with Json.converter:");
        j = Json.converter.toJson(b);
        log(j);

        log("\n\r D instance with Json.converter:");
        j = Json.converter.toJson(d);
        log(j);
        log(" load updated Json into new D instance");
        j.set("proDs", "updated Ds").set("pubDi", 0);
        d = Json.converter.fromJson(new D(), j);
        log(Json.converter.toJson(d));

        log("\n\r java.lang.reflect.Modifier instance with Json.converter:");
        j = Json.converter.toJson(new Modifier());
        log(j);

    }

}
