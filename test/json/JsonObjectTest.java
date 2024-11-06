/*
 * JsonObject test, MIT (c) 2022-2024 miktim@mail.ru 
 */
//package json; 

import java.lang.reflect.Modifier;
import java.util.HashMap;
import org.miktim.json.Json;
import org.miktim.json.JsonObject;

public class JsonObjectTest {

    public static void log(Object s) {
        System.out.println(String.valueOf(s));
    }

    JsonObjectTest() {
    }

    public static class Av extends Bv {

        public D objD = new D();
        public String pubAv = "public Av";
        protected String proAv = "protected Av";
        String defAv = "default Av";
        private String privAv = "private Av";
        public transient boolean enableNames = true;
// TODO: objC
        public C objC = new C();

        Av() {
        }

        @Override
        protected Object replacer(String name, Object value) {
            if (enableNames) {
                log(name);
            }
            try {
// TODO: pri_dS
                if (name.endsWith(".objD")) {
//                    return toJson(value);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                return IGNORED;
            }
            return value;
        }

        @Override
        protected Object reviver(String name, Object value) {
            if (enableNames) {
                log(name);
            }
            try {
                if (name.endsWith(".objD")) {
//                    fromJson(objD, (Json)value);
//                    return IGNORED;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                return IGNORED;
            }
            return value;
        }
    }

    public static class Bv extends JsonObject {

        public String pubBs = "public Bv";
        protected String proBs = "protected Bv";
        String defBs = "default Bv";
        private String priBs = "private Bv";
        public transient String transBs = "transient Bv";

        Bv() {
        }
    }

    public static class D {

        protected String proDs = "protected dS";
        public int pubDi = 123;
        String defDs = "";
        public C pubDC = new C();
        private String priDs = "private dS";

        D() {
        }

        D(int i, String s) {
            pubDi = i;
            defDs = s;
        }
    }

    public static class C extends JsonObject {
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

    public static class B extends A {

        double bd = 123.123;
        D bD = new D();

        B() {
            super();
//            setIgnored(new String[0]); // reset ignored
        }

        @Override
        protected Object replacer(String name, Object value) {
//            log(name);
            try {
                if (isClassName(name)) { // first call
// ignored field was declared in A
//                log("Ignored: " + Arrays.toString(getIgnored()));
// private a.pri_ad field is invisible here. Unload by getter
// on first call we MUST return Json object
                    return (new Json()).set("set_aD", get_aD());
                } else if (name.endsWith(".bD")) {
//                    return toJson(value);
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
                    set_aD(castMember(get_aD(), "set_aD", (Json) value));
                    return IGNORED;
                } else if (name.endsWith(".bD")) {
//                    return fromJson(bD, (Json) value);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return super.reviver(name, value);
        }
    }

    public static class A extends JsonObject {

//        public String pub_aS = "public aS";
//        final String fin_aS = "final aS";
//        private String pri_aS = "private aS";
//        protected String pro_aS = "protected aS";
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

        public double get_aD() {
            return pri_ad;
        }

        public void set_aD(double ad) {
            this.pri_ad = ad;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected Object replacer(String name, Object value) {
            if (name.endsWith(".maC") && value != null) {
// unload Map
                Json j = new Json();
                try {
                    for (HashMap.Entry<Integer, C> entry : ((HashMap<Integer, C>) value).entrySet()) {
                        j.set(entry.getKey(), toJson(entry.getValue()));
                    }
                } catch (IllegalArgumentException | ReflectiveOperationException | IndexOutOfBoundsException ex) {
                    ex.printStackTrace();
//                    return IGNORED;
                }
//                log(j);
                return j;
            }
            return value;
        }

        @Override
        protected Object reviver(String name, Object value) {
            try {
                if (name.endsWith(".maC") && value != null) {
// load Map
                    maC.clear();
                    Json j = (Json) value;
                    for (String key : j.listNames()) {
                        C c = fromJson(new C(), j.getJson(key));
                        maC.put(new Integer(key), c);
                    }
                    return IGNORED; // aC already loaded
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                return IGNORED;
            }
            return value;
        }

    }
    A a = new A();
    B b = new B();
    D d = new D(456, "default dS");
    Av av = new Av();


public static void main(String[] args) throws Exception {
        log("\n\rJsonObject test");
        JsonObjectTest t = new JsonObjectTest();
        Json j;
        
        log("\n\r Av instance toJson:");
        j = t.av.toJson();
        log(j);
        j.getJson("objD").set("proDs", "updatedDs");
//        t.av.enableNames = false;
        log("\n\r Av instance fromJson:");
        t.av.fromJson(j);
        t.av.enableNames = false;
        log(t.av.toJson());
        log(" Av instance with Json.converter");
        log(Json.converter.toJson(t.av));

        log("\n\r B instance toJson/fromJson :");
// TODO JsonObject returns Object
        String s = ((Json)t.b.toJson()).toJSON();
        log(s);
        t.b.fromJson(new Json(s));

        log(" B instance with Json.converter:");
        j = Json.converter.toJson(t.b);
        log(j);
        
        log("\n\r D instance with Json.converter:");
        j = Json.converter.toJson(t.d);
        log(j);
        log(" load updated Json into new D instance");
        j.set("proDs", "updated dS").set("pubDi", 0);
        t.d = Json.converter.fromJson(new D(), j);
        log(Json.converter.toJson(t.d));

        log("\n\r java.lang.reflect.Modifier instance with Json.converter:");
        j = Json.converter.toJson(new Modifier());
        log(j);

    }

}
