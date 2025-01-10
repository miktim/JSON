/*
 * JsonObject test, MIT (c) 2022-2025 miktim@mail.ru 
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
    
    public static class BIS extends BufferedInputStream implements JsonConvertible {

        @Override
        public Object replacer(String name, Object value) {
            return value;
        }

        @Override
        public Object reviver(String name, Object value) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        BIS(InputStream in) {
            super(in);
        }
     }
    
    public static class BarBar extends JsonObject {
        Bar defBB = new Bar();
        public BarBar(){
            
        }
    }
    
    public static class Bar extends Foo implements JsonConvertible {

        public String pubB; // visible
        String defB; // visible
        protected String proB; // visible
        private String priB; // visible
        public transient String pubtB; // ignored
        public final String pubfB = "public final"; // ignored
        Foo defBF; // bad choice! MUST be initialized!
        transient boolean fillIn = false;

        public Bar() {
            super();
        }

        @Override
        public Object replacer(String name, Object value) {
            if(fillIn && !(name.indexOf(':') < 0 || name.endsWith(":defBF")))
// fill String fields with it's name
                return name;
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
        jf.set("pub", "public");
        Json.converter.fromJson(foo, jf);
        log(Json.converter.toJson(foo));
        isOk(foo.pub.equals("public"));
        
        log("\n\rBar instance to/from Json:");
        Bar bar = new Bar();
        Json jb = Json.converter.toJson(bar);
        log(jb);
        isOk(jb.listNames().length == 8);
        Json.converter.fromJson(bar, jb);
        
// Foo bar.defBF not initialized. But Foo has public default constructor.        
        bar.fillIn = true; // fill String fields with it's name in replacer
        jb = Json.converter.toJson(bar);
        bar.fillIn = false;
        jb.set("defBF", jf);
        Json.converter.fromJson(bar, jb);
        jb = Json.converter.toJson(bar);
        log(jb);
// !from Bar constructor accessible 5 Foo fields        
        isOk(jb.getJson("defBF").listNames().length == 1); 

        log("\n\rBarBar instance to/from Json:");
        BarBar barBar = new BarBar();
        Json jbb = barBar.toJson();
        log(jbb);
        barBar.defBB = null;
        barBar.fromJson(jbb);
        isOk(barBar.toJson().getJson("defBB").listNames().length == 8);

        log("\n\rTest protected fields with BufferedInputStream:");
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
//        bar = Json.converter.fromJson(bar,null); // bar = null
//        Json.converter.fromJson(null, jb); // NullPointerException
    }
}
