/*
 * JSONObject test, MIT (c) 2022 miktim@mail.ru 
 */
//package json; 

import java.lang.reflect.InvocationTargetException;
import json.A;
import json.J;
import org.miktim.json.JSON;
import org.miktim.json.JSONObject;

public class JSONObjectTest extends JSONObject {

    C[] mC = {new C(), new C()};
    public static String mS = "mS value";
    static final String mSf = "Final fields ignored";
    private double md = 3.14159;
    private int mi = 123;
    private String mSp = "mSp value";

    public A mA = new A(); // A extends J
    protected B mB = new B(); // B extends A
    J mJ = new J(); // J extends JSONObject

    public static void log(Object s) {
        System.out.println(s);
    }

    public static void logName(Object obj, String name) {
        log(isClassName(name) ? name : obj.getClass().getSimpleName() + ":" + name);
    }

    JSONObjectTest() {
        setIgnored(new String[]{"mSp"}); // ignore field
    }

    @Override
    protected Object replacer(String name, Object value) {
        logName(this, name);
        if (name.equals("mi")) {
            return IGNORED; // another way to ignore the field
        } else if (name.equals("mS")) {
            return "mS value replaced";
        } else if (name.equals("mC")) {
            Object[] mmC = new Object[mC.length];
            for (int i = 0; i < mC.length; i++) {
                try {
                    mmC[i] = mC[i].toJSON();
                } catch (IllegalAccessException | IllegalArgumentException ex) {
                    log(ex);
                }
            }
            value = mmC;
        }
        return value;
    }

    @Override
    protected Object reviver(String name, Object value) {
//        logName(this, name);
        if (name.equals("mC")) {
            Object[] jmC = (Object[]) value;
            this.mC = new C[jmC.length];
            for (int i = 0; i < jmC.length; i++) {
                try {
                    (this.mC[i] = new C()).fromJSON(jmC[i]);
                } catch (IllegalAccessException | IllegalArgumentException ex) {
                    log(ex);
                }
            }
            return IGNORED;
        }
        return value;
    }

    class B extends A {

        @Override
        protected Object replacer(String name, Object value) {
            logName(this, name); // logName(), log() inherited from J
            if (isClassName(name)) {
// unload by getter
                ((JSON) value).set("jc", getJc());
                log("Unloaded J.jc by getter: " + getJc());
            }
            super.replacer(name, value);
            return value;
        }

        @Override
        protected Object reviver(String name, Object value) {
            logName(this, name);
            if (isClassName(name)) {
// load by setter
                setJc(castMember("jc", (JSON) value, getJc()));
                log("Loaded J.jc by setter: " + getJc());
            }
            return value;
        }
    }

    class C extends JSONObject {

        public String mS = "mS value";
        static final String mSf = "Final fields ignored";
        private double md = 3.14159;
        private int mi = 123;
        private String mSp = "mSp value";

        C() {
        }
    }

    public static void main(String[] args)
            throws IllegalArgumentException, IllegalAccessException, InstantiationException, InvocationTargetException, Exception {
        log("\n\rJSONObject test\n\r");
        JSONObjectTest t = new JSONObjectTest();

        log("toJSON:\n\r");
        JSON json = (JSON) t.toJSON();
        log(json);

        log("\n\rChange JSON:");
        json.set("mS", "mS value changed")
                .set("mi", 0) // mi ignored
                .set("qwerty", "unknown members ignored");

        json.getJSON("mA")
                .set("aad", new int[]{1, 2})
                .set("ai", 0)
                .set("af", 0);

        json.getJSON("mB") // change inherited
                .set("aS", "inherited aS changed")
                .set("jc", "d");

        log(json);

        log("\n\rNormalize JSON.");
        json = json.normalize();

        log("\n\rfromJSON:\n\r");
        t.fromJSON(json);

        log("\n\rtoJSON again:\n\r");
        log(t.toJSON());
    }

}
