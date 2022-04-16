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

    public static String mS = "mS value";
    public A mA = new A(); // A extends J
    protected B mB = new B(); // B extends A
    J mJ = new J(); // J extends JSONObject
    static final String mSf = "Final fields ignored";
    private int mi = 123;
    private String mSp = "mSp value";

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
        }
        return value;
    }

    @Override
    protected Object reviever(String name, Object value) {
        logName(this, name);
        return value;
    }

    class B extends A {

        @Override
        protected Object replacer(String name, Object value) {
            logName(this, name); // logName(), log() inherited from J
            if (isClassName(name)) {
// unload inherited
                ((JSON) value).set("aS", aS).set("jc", getJc());
                log("Unloaded inherited B.aS, B.jc: " + aS + ", " + getJc());
            }
            return value;
        }

        @Override
        protected Object reviever(String name, Object value) {
            logName(this, name);
            if (isClassName(name)) {
// load inherited
                aS = castMember("aS", (JSON) value, aS);
                setJc(castMember("jc", (JSON) value, getJc()));
                log("Loaded inherited B.aS, B.jc: " + aS + ", " + getJc());
            }
            return value;
        }
    }

    public static void main(String[] args)
            throws IllegalArgumentException, IllegalAccessException, InstantiationException, InvocationTargetException, Exception {
        log("\r\nJSONObject test\n\r");
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

        json.getJSON("mB") // replace inherited
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
