## Java 7+/Android JSON parser/generator, MIT (c) 2020-2025 @miktim

### Release notes:  

Java SE 7+/Android RFC 8259 compliant package
(see: [https://datatracker.ietf.org/doc/rfc8259/?include_text=1](https://datatracker.ietf.org/doc/rfc8259/?include_text=1) ).  
\- no external dependencies;  
\- JSON literals and arrays of mixed type are allowed.  

The jar ./dist/java-json-... file was generated with debugging info using JDK 8 for target JRE 7.  

Further in the text:  
\- “JSON” means text in JSON format;  
\- “Json” means the Java representation of a JSON object.  

  
### package org.miktim.json  

<p style="background-color: #B0C4DE;">
&emsp;<b>Overview:</b>
</p>  

The class [JSON](#JSON) contains static methods for parsing/generating text in JSON format. Parser converts JSON to <a id="native"></a> **package native** objects: **[Json](#JsonClass)** ( the Java representation of a JSON object )**, String, Number, Boolean, null, Object[ ]** - an array of the listed types. The JSON generator accepts any Java object, all Java primitives and their arrays.   

Instances of Java classes can be converted to Json objects (usually empty) using [Json.converter](#Converter).  

The [JsonObject](#JsonObject) abstract class and [JsonConvertible](#JsonConvertible) interface use the JavaScript-like replacer/reviver tool to convert object instance to or from Json object.  

<a id="JSON"></a> 
### Class JSON

This class contains static parsing/generating methods.  
Parser converts JSON to package native objects. When the names within an JSON object are not unique, the parser stores last value only.  
The JSON generator accept any Java object, all Java primitives and their arrays. JSON text exchanged between systems MUST be encoded using UTF-8 (default charset).  

<p style="background-color: #B0C4DE;">
&emsp;<b>Methods:</b>
</p>  

**static Object fromJSON ( String jsonText ) throws IOException, ParseException**  
Parse JSON text to a native objects.  

**static Object fromJSON ( InputStream in, String charsetName ) throws IOException, ParseException**  
Parse JSON text from an input stream with the specified encoding

**static String toJSON ( Object obj ) throws IOException;**  
Serializes a Java object instance as a single-line text in JSON format  

**static String toJSON ( Object obj, int space ) throws IOException**  
Generate a Java object as JSON text with the specified number of spaces in the line indentation.  

**static &lt;T\>T toJSON ( T obj, OutputStream out, int space ) throws IOException**  
Serializes a Java object as JSON text into a UTF-8 stream with the specified indentation. Returns obj.  


**static &lt;T\>T toJSON ( T obj, OutputStream out, int space, String charsetName ) throws IOException**  
Serializes a Java object as JSON text into a stream with the specified indentation and encoding. Returns obj.  
  
<a id="cast"></a>
Methods for converting or copying Java objects.  Notes:  
\- the sample object must be initialized;  
\- arrays must have the same dimension;  
\- casting numbers may involve rounding or truncation;  
\- casting null to primitive returns empty array or initial value;  
\- casting to null returns null.  
  

**static &lt;T\> T cast ( T sample, Object obj ) throws ClassCastException**  
Casting a Java Object to the sample Class  

**static &lt;T\> T cast ( Class &lt;T\> cls, Object obj ) throws ClassCastException**  
Casting a Java object to the cls Class  

<p style="background-color: #B0C4DE;">
&emsp;<b>Example:</b>
</p>  

```java
/*
 * Parse and cast
 */
// JSON string literal 
Object obj = JSON.fromJSON("\"This is also JSON!\"");
System.out.println(obj instanceof String);
/* console output:
true
*/
obj = JSON.fromJSON("[1.2, 3.4, 5]"); // obj is instance of Object[]
// cast array by class. Numbers truncated to integers
int[] ints = JSON.cast(int[].class, obj);
// generate JSON with two spaces in the indentation
System.out.println(JSON.toJSON(ints, 2));
/* console output:
[
  1,
  3,
  5
]
*/
// cast array by sample
double[][] dbls = new double[0][0];
dbls = JSON.cast(dbls, new int[][]{{1, 2, 3},{7, 8}});
System.out.println(JSON.toJSON(dbls));
/* console output:
[[1.0, 2.0, 3.0], [7.0, 8.0]]
*/
```
  
  
<a id="JsonClass"></a>
### Class Json extends HashMap &lt;String, Object\>

This class is a Java representation of a JSON object.
Json member types: **Json, String, Number, Boolean, null, Object[ ]** - an array of the listed types.  
   
Put, set notes:  
\- Json object setters accept any Java object, all Java primitives and their arrays;  
\- RFC 8259 does not recommend using Java BigDecimal and BigInteger as JSON values;  
\- AVOID RECURSION ! ;  
\- the put, set methods cast Java primitives into corresponding objects: 
**Number**, **Boolean** or **String** for chars;  
\- Java arrays are stored as:  
**int[ ][ ]** as **Object[ ] { Object[ ] { Number, ... }, Object[ ] { Number, ... } }**, **String[ ]** as **Object[ ] { String, ... }**  

<p style="background-color: #B0C4DE;">
&emsp;<b>Constructors:</b>
</p>

**Json ( )**  
Default constructor.  

**Json ( Object... members ) throws IndexOutOfBoundsException**  
Members is a name,value pairs  

**Json ( String jsonText ) throws IOException, ParseException**  
Create Json object from String  

**Json ( InputStream inStream ) throws IOException, ParseException**  
Create Json object from UTF-8 encoded stream.  

<p style="background-color: #B0C4DE;">
&emsp;<b>Example:</b>
</p>  
  
```java
/*
 * Creating Json object
 */
// ... from name/value pairs
Json j = new Json("number", 1, "string", "qwerty", "boolean", true);
System.out.println(j.toJSON());
/* console output: 
{"number": 1, "string": "qwerty", "boolean": true}
*/
// ... from String
j = new Json("{ \"number\": 1, \"string\": \"qwerty\", \"boolean\": true }"); 
```  
<p style="background-color: #B0C4DE;">
&emsp;<b>Methods:</b>
</p>  

**String[ ] listNames ( )**  
Returns a list of member names of this Json object.  

**boolean exists ( String memberName, int... indices )**  
Returns true if there is a member or an element of the member array  

**Object put ( String memberName, Object value );**  
Overriden. Create or replace Json member.  

**Json set ( String memberName, Object value );**  
Create or replace Json member. Returns this.   

**Object get( String memberName );**  
Inherited. Get Json member value or null.

**Object remove ( String memberName );**  
Inherited  
  
**Getters returns null if the Json memeber does not exist.**  
  
**Object get ( String memberName, int... indices ) throws IndexOutOfBoundsException**  
Returns null, the value of the Json member, or an array element  

**Json getJson ( String memberName, int... indices ) throws ClassCastException, IndexOutOfBoundsException**  
Returns a nested Json object. 
  
**String getString ( String memberName, int... indices ) throws ClassCastException, IndexOutOfBoundsException**
Casts member or array element to String  

**Number getNumber ( String memberName, int... indices ) throws ClassCastException, IndexOutOfBoundsException**  
Casts member or array element to Number
  
**Boolean getBoolean ( String memberName, int... indices ) throws ClassCastException, IndexOutOfBoundsException**  
Casts member or array element to Boolean  

**Object[ ] getArray ( String memberName, int... indices ) throws ClassCastException, IndexOutOfBoundsException**  
Casts member to array of Objects  

**&lt;T\> T castMember ( T sample, String memberName, int... indices ) throws ClassCastException, IndexOutOfBoundsException**  
Converts the value of a Json member or an array element to the sample class. See notes for a [JSON.cast](#cast) methods.  

**&lt;T\> T castMember ( Class &lt;T\> cls, String memberName, int... indices ) throws ClassCastException, IndexOutOfBoundsException**  
Converts the value of a Json member or an array element to the specified class. See notes for a [JSON.cast](#cast) methods.  

**String toString ( )**  
Overridden. Returns JSON text as single line  

**String toJSON ( )**  
Stringify Json object as single line  

**String toJSON ( String memberName, int... indices )**  
Stringify member value or array element as single line  

**Json toJSON ( OutputStream outStream ) throws IOException**  
Output UTF-8 encoded single line JSON text to outStream. Returns this.
  
<p style="background-color: #B0C4DE;">
&emsp;<b>Example:</b>
</p>  
  
```java
/*
 * Creating a Json object and extracting members
 */
Json j = (new Json())
  .set("personId", 1234)
  .set("firstName","John")
  .set("lastName","Doe")
// "phones" is nested Json object
  .set("phones", 
    new Json("home", "123-4567","work","890-1234"));
System.out.println(JSON.toJSON(j, 2));
/* console output
{
  "personId": 1234, 
  "firstName": "John",
  "lastName": "Doe", 
  "phones": {
    "home": "123-4567", 
    "work": "890-1234"
  }
}
*/
// retrieve personId
int personId = j.getNumber("personId").intValue();
// retrieve first name
String firstName = j.getString("firstName");
// retrieve home phone from nested Json object
String homePhone = j.getJson("phones").getString("home");
System.out.printf("%d %s %s\n\r", personId, firstName, homePhone);
/* console output:
1234 John 123-4567
*/
```
<a id="Converter"></a>
### Static Json.converter  
Used to convert existing instances of Java objects to/from a Json object.  
Only the visible fields are converted. The converter ignores the final and transient fields.

<p style="background-color: #B0C4DE;">
&emsp;<b>Methods:</b>
</p>  

**Json toJson ( Object targetObj ) throws ClassCastException**  
Returns a Json object from the target object  

**&lt;T\> T fromJson ( T targetObj, Json jsonObj ) throws ClassCastException**  
Loads Json to target object. Returns target object.  

<p style="background-color: #B0C4DE;">
&emsp;<b>Example:</b>
</p>  
  
```java
/*
 * Fields visibility
 */
  public static class Foo {
    public String pub = "public"; // visible
    String def = "default"; // invisible in the converter
    protected String pro = "protected"; // invisible in the converter
    private String pri = "private"; // invisible in the converter
    public transient String pubt = "public transient"; // ignored
    public final String pubf = "public final"; // ignored

    public Foo() {}; // default constructor
  }
  
  public static void main(String[] args) throws Exception {
    Foo foo = new Foo();  
    System.out.println(Json.converter.toJson(foo));
/* console output:
{"pub": "public"}
*/
  }
```  

<a id="JsonConvertible"></a>
### Interface JsonConvertible  
The JsonConvertible interface provides JavaScript-like methods for converting Java object into or from [Json](#JsonClass) object. Notes:  
\- visibility of object fields as from the object constructor (including the privates);  
\- Java transient and final fields are ignored;  
\- it is strongly recommended to initialize the convertible fields;  
\- requires a public default constructor.  

<p style="background-color: #B0C4DE;">
&emsp;<b>Constants:</b>
</p>  

**static final Object IGNORE**  
Returned from the replacer/reviver methods to disable conversion by default.  

<p style="background-color: #B0C4DE;">
&emsp;<b>Methods:</b>
</p>  

**Object replacer ( String name, Object value );**  
Applies on unloading to Json object:  
\- name is object field class and name delimited with colon ( : ), value is object field value;  
\- the first call with the class name of this object and this object as the value;  
\- returns Json-supported object or IGNORE.  

**Object reviver ( String name, Object value );**  
Applies on loading from Json object:  
\- name is object field class and name delimited with colon ( : ), value is Json-supported object;  
\- the first call with the class name of this object and the Json object as the value;  
\- returns a convertible value or IGNORE.  

<p style="background-color: #B0C4DE;">
&emsp;<b>Example:</b>
</p>  
  
```java
/*
 * Creating a Json convertible HashMap
 */
public class NamesOfNumbers extends HashMap<Double, String>
    implements JsonConvertible {

  int space = 2; // spaces in indentation
 
  public NamesOfNumbers() {
    super();
  }

// unload fields to Json object
  @Override
  public Object replacer(String name, Object value) {
    if (name.indexOf(':') < 0) { // first call without field name
// convert HashMap to Json object
      Json j = new Json();
      for (Double key : this.keySet().toArray(new Double[0])) {
        j.put(key.toString(), this.get(key));
      }
// return newly created Json object
      return new Json("HashMap", j);
    }
    return value; // unload fields "by default"
  }

// load fields from Json object
  @Override
  public Object reviver(String name, Object value) {
    if (name.indexOf(':') < 0) { // first call without field name
// fill HashMap from the Json object 
      Json j = ((Json) value).getJson("HashMap");
      this.clear(); // erase HashMap
      for (String key : j.listNames()) {
        this.put(new Double(key), j.getString(key));
      }
    }
    return value; // load "by default"
  }

// serialize this to String with spaces in the indentation 
  public String toJSON() throws IOException {
    return JSON.toJSON(Json.converter.toJson(this), this.space);
  }
// deserialize this from String
  public void fromJSON(String s) throws IOException, ParseException {
    Json.converter.fromJson(this, new Json(s));
  }
    
  public static void main(String[] args)
      throws IOException, ParseException {
    NamesOfNumbers names = new NamesOfNumbers();
    names.put(0.0, "zero");
    names.put(1.0, "one");
    names.put(1.5, "one and five tenths");
    String s = names.toJSON();
    System.out.println(s);
    names.clear(); // erase map
    names.fromJSON(s);
    System.out.println(names.get(1.5));
  }
/* console output:
{
  "HashMap": {
    "0.0": "zero", 
    "1.0": "one", 
    "1.5": "one and five tenths"
  }, 
  "space": 2
}
one and five tenths
*/
}

```

  
<a id="JsonObject"></a>
### Abstract class JsonObject implements JsonConvertible
Java object extender. Unload/load a Java object instance to/from a [Json](#JsonClass) object. Notes:  
\- visibility of object fields as from the object constructor (including the privates);  
\- Java transient and final fields are ignored;  
\- it is strongly recommended to initialize the convertible fields;  
\- requires a public default constructor.  
  
<p style="background-color: #B0C4DE;">
&emsp;<b>Constants:</b>
</p>  
  
**static final Object IGNORE**  
Returned from the replacer/reviver methods to disable conversion by defailt.  

<p style="background-color: #B0C4DE;">
&emsp;<b>Methods:</b>
</p>  

**Object replacer ( String name, Object value );**  
**Object reviver ( String name, Object value );**  
See [JsonConvertible](#JsonConvertible) interface.

**Json toJson ( );**  
Returns a Json object from this object  

**&lt;T\> T fromJson ( Json jsonObj );**  
Loads Json to this object. Returns this object.  

**String toString ( );**  
Overridden. Generate JSON text as a single line  
  
<p style="background-color: #B0C4DE;">
&emsp;<b>Example:</b>
</p>  
  
```java
/*
 * Declare JsonObject 
 */
  public static class Person extends JsonObject {
    int personId = 0;
    String firstName = "";
    String lastName = "";
    boolean married = false;
// use Json class (HashMap<String, Object>) for phones
    Json phones = new Json();

// default constructor
    public Person() {
    }
  }

  public static void main(String[] args)
    throws IOException, ParseException {

// create and fill Person instance
    Person person = new Person();
    person.personId = 12345;
    person.firstName = "John";
    person.lastName = "Doe";
    person.phones.put("home", "123-4567");
    person.phones.put("work", "789-0123");
// unload person to JSON string
    String s = person.toJson().toJSON();
    System.out.println(s);
// load person from JSON string
    person = new Person();
    person.fromJson(new Json(s));
// get a home phone number
    System.out.println(person.phones.getString("home"));
/* console output:
{"personId": 12345, "firstName": "John", "lastName": "Doe", "married": false, "phones": {"work": "789-0123", "home": "123-4567"}}
123-4567
*/
  }
```  
  
**See also:**  
[https://www.baeldung.com/java-json](https://www.baeldung.com/java-json)  
[https://www.json.org/json-en.html](https://www.json.org/json-en.html)  
