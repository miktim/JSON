/*
 * Example4, MIT (c) 2024 miktim@mail.ru
 */
// package json;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import org.miktim.json.JSON;
import org.miktim.json.Json;
import org.miktim.json.JsonObject;

public class Example4 {

    public static class Person extends JsonObject {

        int personId = 0;
        String firstName = "";
        String lastName = "";
        boolean married = false;
// Json implements HashMap<String, Object>        
        Json phones = new Json(); 
        HashSet<Integer> friends = new HashSet<>(); // personId's
// default constructor
        public Person() {
        }
// HashSet is not native class
        @Override
        @SuppressWarnings("unchecked")
        public Object replacer(String name, Object value) {
            if (name.endsWith(":friends")) {
// unload friends Set as array                 
                return ((HashSet<Integer>)value).toArray(new Integer[0]);
            }
            return value;
        }

        @Override
        public Object reviver(String name, Object value) {
            if (name.endsWith(":friends")) {
// load friends Set, first create collection from array                
                   List<Integer> list = Arrays.asList(JSON.cast(Integer[].class, value));
                   return new HashSet<>(list);
            }
            return value;
        }
    }
    public static class Persons extends Json {
        public Persons() {
        }
        public Person remove(Object personId) {
            Person person = remove(String.valueOf(personId));
            if (person != null) {
                for(Object entry : values()){
                    ((Person)entry).friends.remove((int)personId);
                }
            }
            return person;
        }
    }

    public static void main(String[] args) throws
            IllegalArgumentException, //IllegalAccessException,
            IOException, ParseException, IllegalAccessException {
// load persons from File
        FileInputStream fis = new FileInputStream("./Example4.json");
        Json persons = new Json(fis);
        String s = persons.toJSON();
        System.out.println(s);
// TODO: !cast not use Json.converter for convertible Objs     
        Person person = persons.castMember(Person.class, "12345");
        System.out.println(JSON.toJSON(person, 2));
    };
}
