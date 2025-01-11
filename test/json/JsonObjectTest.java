/*
 * JsonObject test, MIT (c) 2022-2025 miktim@mail.ru 
 * Visibility and usability (uninitialized fields) test
 */
//package json; 

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import org.miktim.json.JSON;
import org.miktim.json.Json;
import org.miktim.json.JsonConvertible;
import org.miktim.json.JsonObject;

public class JsonObjectTest {

    static void log(Object... obj) {
        for (Object item : obj) {
            System.out.print(String.valueOf(item) + " ");
        }
        System.out.println();
    }

    static void isOk(boolean res) {
        if (res) {
            log("Ok");
        } else {
            log("Failed!");
            System.exit(1);
        }
    }

// visibility of BufferedInputStream protected fields
    public static class BIS extends BufferedInputStream implements JsonConvertible {

        @Override
        public Object replacer(String name, Object value) {
            return value;
        }

        @Override
        public Object reviver(String name, Object value) {
            // unused
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        BIS(InputStream in) {
            super(in);
        }
    }
// Bar class field    

    public static class BarBar extends JsonObject {

        Bar defBB = new Bar();
        transient int testNo = 0;
        
        public BarBar() {

        }
        
        @Override
        public Object replacer(String name, Object value) {
            if(testNo == 1) return IGNORE; // ignore unloading at all
            return value;
        }
        @Override
        public Object reviver(String name, Object value) {
            if(testNo == 2) return IGNORE; // ignore loading at all
            return value;
        }
    }
// Foo inherited fields and Foo class field    

    public static class Bar extends Foo implements JsonConvertible {

        public String pubB; // visible
        String defB; // visible
        protected String proB; // visible
        private String priB; // visible
        public transient String pubtB; // ignored
        public final String pubfB = "public final"; // ignored
        Foo defBF; // bad choice! MUST be initialized!
// 0 - nothing to do, 1 - fill in names, 2 IGNORE
        transient int testNo = 0;

        public Bar() {
            super();
        }

        @Override
        public Object replacer(String name, Object value) {
            if (testNo > 0 && !(name.indexOf(':') < 0 || name.endsWith(":defBF"))) {
                if (testNo == 1) {
// fill String fields with it's name
                    return name;
                }
                if (testNo == 2) {
// IGNORE String fields
                    return IGNORE;
                }
            }
            return value;
        }

        @Override
        public Object reviver(String name, Object value) {
            return value;
        }
    }

    public static class Foo {

        public String pub; // visible
        String def = "default"; // invisible in the converter
        protected String pro = "protected"; // invisible in the converter
        private String pri = "private"; // invisible in the converter
        public transient String pubt = "public transient"; // ignored
        public final String pubf = "public final"; // ignored

        public Foo() {
        }
    }

    public static void main(String[] args) throws Exception {
        log("\n\rJsonObject test");

        log("\n\rFoo instance to/from Json:");
        Foo foo = new Foo();
        Json jf = Json.converter.toJson(foo);
        log(jf);
        Json.converter.fromJson(foo, jf);
        jf.set("pub", "public"); // fill Json field and load
        Json.converter.fromJson(foo, jf);
        log(Json.converter.toJson(foo));
        isOk(jf.listNames().length == 1 && foo.pub.equals("public"));

        log("\n\rBar instance to/from Json:");
        Bar bar = new Bar();
        Json jb = Json.converter.toJson(bar);
        log(jb);
        isOk(jb.listNames().length == 8);
// Foo bar.defBF not initialized. But Foo has public default constructor.        
        Json.converter.fromJson(bar, jb);

        log("Bar: replacer fills fields with class:name");
        bar.testNo = 1; 
        jb = Json.converter.toJson(bar);
        bar.testNo = 0;
        jb.set("defBF", jf);
        Json.converter.fromJson(bar, jb);
        jb = Json.converter.toJson(bar);
        log(jb);
// from Bar constructor accessible 5 Foo fields        
        isOk(jb.getJson("defBF").listNames().length == 1);

        log("Bar: replacer IGNOREs String fields");
        bar.testNo = 2; // ignore String fields
        Json jb1 = Json.converter.toJson(bar);
        log(jb1);
        isOk(jb1.listNames().length == 1);

        log("\n\rBarBar instance to/from Json:");
        BarBar barBar = new BarBar();
        Json jbb = barBar.toJson();
        log(jbb);
        barBar.defBB = null;
        jbb.set("defBB", jb);
        barBar.fromJson(jbb);
        isOk(barBar.defBB.defBF != null);

        log("BarBar: IGNORE unloading");
        barBar.testNo = 1;
        Json jbb1 = barBar.toJson();
        log(jbb1);
        isOk(jbb1.listNames().length == 0);

        log("BarBar: IGNORE loading");
        barBar.testNo = 2;
        barBar.defBB = null;
        barBar.fromJson(jbb);
        log(barBar.toJson());
        isOk(barBar.defBB == null);
        barBar.testNo = 0;

        log("\n\rVisibility BufferedInputStream protected fields:");
        FileInputStream fis = new FileInputStream("./Example3.json");
        BIS bIS = new BIS(fis);
        Json jbis = Json.converter.toJson(bIS);
        String[] jbisNames = jbis.listNames();
        log(JSON.toJSON(jbisNames));
        isOk(jbisNames.length == 6);
        BufferedInputStream bis = new BufferedInputStream(fis);
        jbis = Json.converter.toJson(bis);
        log(jbis);
        isOk(jbis.listNames().length == 0);
        fis.close();

//        Json.converter.toJson(null); // NullPointerException
//        Json.converter.fromJson(bar,null); // NullPointerException
//        Json.converter.fromJson(null, jb); // NullPointerException
    }
}
