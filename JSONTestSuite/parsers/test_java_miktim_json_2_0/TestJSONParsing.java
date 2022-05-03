// javac -cp ".:json-2.0.0.jar" TestJSONParsing.java && java -classpath ".:json-2.0.0.jar" TestJSONParsing x.json

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import org.miktim.json.JSON;


public class TestJSONParsing {
    
    public static boolean isValidJSON(String s) {
        try {
            Object obj = JSON.parse(s);
//            System.out.println(obj);
//            System.out.println(obj != null ? obj.getClass().getSimpleName() : null);
            return true;
        } catch (ParseException | IOException e) {
//            System.out.println(e);
            return false;
        }
    }

    public static void main(String[] args) {
        
        if(args.length == 0) {
            System.out.println("Usage: java TestJSONParsing file.json");            
            System.exit(2);
        }
        
        try {
            String s = new String(Files.readAllBytes(Paths.get(args[0])));
            if(isValidJSON(s)) {
                System.out.println("valid");
                System.exit(0);            
            }
            System.out.println("invalid");
            System.exit(1);
        } catch (IOException e) {
            System.out.println("not found");
            System.exit(2);
        }   
    }
}