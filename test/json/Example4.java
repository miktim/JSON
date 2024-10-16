/*
 * 
 */
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.miktim.json.JSON;
import org.miktim.json.Json;
import org.miktim.json.JsonObject;

// package json;

public class Example4 {

    public static class Person extends JsonObject {

        int personId = 0;
        String firstName = "";
        String lastName = "";
        boolean married = false;
        HashMap<String, String> phones = new HashMap<>();

// default constructor
        public Person() {
        }
    }

    public static class PersonInfo extends Person {

        Person[] friends = new Person[0];

        public PersonInfo() {
            super();
        }

        @Override
        protected Object replacer(String name, Object value) {
            if (name.endsWith(".phones")) {
// unload phones Map
                Json json = new Json();
                for (Map.Entry<String, String> entry : phones.entrySet()) {
                    json.set(entry.getKey(), entry.getValue());
                }
                return json;
            } else if (name.endsWith(".friends")) {
// unload friends array                
                ArrayList<Json> list = new ArrayList<>();
                try {
                    for (Person p : (Person[]) value) {
                        list.add(p.toJson());
                    }
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    System.err.println(ex.getMessage());
                }
                return list.toArray(new Json[0]);
            }
            return value;
        }

        @Override
        protected Object reviver(String name, Object value) {
            if (name.endsWith(".phones")) {
// load phones Map
                phones.clear(); // fill existing Map
                Json json = (Json) value;
                for (String key : json.listNames()) {
                    phones.put(key, json.getString(key));
                }
                return IGNORED; // phones already loaded
            } else if (name.endsWith(".friends")) {
// load friends array                
                ArrayList<Person> list = new ArrayList<>();
                try {
                    for (Json json : JSON.cast(value, Json[].class)) {
                        list.add(fromJson(new Person(), json));
                    }
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    System.err.println(ex.getMessage());
                }
                friends = list.toArray(friends);//new Person[0]);
                return IGNORED;
            }
            return value;
        }
    }

    public static void main(String[] params) throws
            IllegalArgumentException, IllegalAccessException,
            IOException, ParseException, Exception {
// load person from File
        FileInputStream fis = new FileInputStream("./Example4.json");
        PersonInfo personInfo = (new PersonInfo()).fromJson(new Json(fis));
        System.out.println(JSON.toJSON(personInfo.toJson().normalize(), 2));
    }
}
