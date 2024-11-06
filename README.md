### Java 7+/Android JSON Serializer/Deserializer, MIT (c) 2020-2024 @miktim  
  
#### Release notes:  
  
Java SE 7+/Android RFC 8259 compliant package
(see: [https://datatracker.ietf.org/doc/rfc8259/?include_text=1](https://datatracker.ietf.org/doc/rfc8259/?include_text=1) ).  
No external dependencies.  

<a name="native"></a>**Native** serializable objects are: [Json object](#Json) ( Java representation of a JSON object ), String, Number, Boolean, null, Object[ ] - an array of the listed types. Java numeric and boolean primitives and their arrays also supported.  
Instances of existing Java classes are typically serialized into an empty JSON object.  
To serialize existent Java object, use the [Nativizer](#Nativizer) interface and [register instance](#ClasRegistry) for class. Newly created classes can extends [Nativefier](#Nativefier) class. The abstract Nativefier class applies a JavaScript-like approach, using a replacer/reviver to convert the fields of an object instance to the native type.  
The class [JSON](#JSON) is responsible for serializing objects to JSON text and vice versa.
  
#### package org.miktim.json  
  
  
<a name="JSON"></a> 
#### class JSON

This class contains static serialization and conversion methods.  
JSON text exchanged between systems that are not part of a closed ecosystem MUST be encoded using UTF-8 (default charset).  

<p style="background-color: #B0C4DE;">
&emsp;<b>Methods:</b>
</p>  

**static Object fromJSON ( String jsonText ) throws IOException, ParseException**  
Parse JSON text to a native object  

**static Object fromJSON ( InputStream in, String charsetName ) throws IOException, ParseException**  
Parse JSON text from an input stream with the specified encoding

**static String toJSON ( Object obj ) throws IOException;**  
Serializes a Java object as a single-line text in JSON format  

**static String toJSON ( Object obj, int space ) throws IOException**  
Serializes a Java object as JSON text with the specified number of spaces in the indentation  

**static &lt;T\>T toJSON ( T obj, OutputStream out, int space, String charsetName ) throws IOException**  
Serializes a Java object as JSON text into a stream with the specified indentation and encoding. Returns obj.  
  

Methods for converting or copying Java objects.  Notes:  
\- sample must be initialized;  
\- arrays must have the same dimension;  
\- casting numbers may involve rounding or truncation;  
\- casting a null object returns empty array or initial value;  
\- casting to null returns null.  
  

**static &lt;T\> T cast ( T sample, Object obj ) throws ClassCastException**  
Casting a Java Object to the sample Class  

**static &lt;T\> T cast ( Class &lt;T\> cls, Object obj ) throws ClassCastException**  
Casting a Java object to a Class  
<p style="background-color: #B0C4DE;">
&emsp;<b>Example:</b>
</p>  
  

```java
/*
 * Parse and cast arrays
 */

// cast by class. Numbers truncated to integers
int ints = JSON.cast(int[].class,
  JSON.fromJSON("[1.2, 3.4, 5.6]"));
// generate JSON with two spaces in the indentation
System.out.println(JSON.toJSON(ints, 2));
/* console output:
[
  1,
  3,
  5
]
*/
// cast by sample
double[][] dbls = new double[0][0];
dbls = JSON.cast(dbls, new int[][]{{1, 2, 3},{7, 8, 9}});
System.out.println(Arrays.deepToString(dbls));
/* console output:
[[1.0, 2.0, 3.0], [7.0, 8.0, 9.0]]
*/
```
#### JSON.serializer
  
<a name="Json"></a>
#### public class Json extends HashMap &lt;String, Object\>
This class is a Java representation of a JSON object.
Json member native types:  
Json object, String, Number, Boolean, null, Object[ ] array of listed types.
   
Put, set, get notes:  
\- Json object setters accept any Java object, all Java primitives and primitive arrays;  
\- RFC 8259 does not recommend using Java BigDecimal and BigInteger as Json member values;  
\- AVOID RECURSION!;  
\- put, set methods cast Java primitives to the corresponding objects:  
**float** as **Number**, **boolean** as **Boolean**  
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
Returns a list of the names of the members of this Json object  

**boolean exists ( String memberName, int... indices )**  
Returns true if there is a member or an element of the member array  

**Object put ( String memberName, Object value );**  
inherited  

**Json set ( String memberName, Object value );**  
Create or replace member. Returns this.   

**Object get ( String memberName )**  
inherited  

**Object remove ( String memberName );**  
inherited  
  
**Object get ( String memberName, int... indices ) throws IndexOutOfBoundsException**  
Returns null, the value of an member, or an array element  

**Json getJson ( String memberName, int... indices ) throws ClassCastException, IndexOutOfBoundsException**  
Returns a nested Json object 
  
**String getString ( String memberName, int... indices )     throws ClassCastException, IndexOutOfBoundsException**
Casts member or array element to String  

**Number getNumber ( String memberName, int... indices )         throws ClassCastException, IndexOutOfBoundsException**  
Casts member or array element to Number
  
**Boolean getBoolean ( String memberName, int... indices )        throws ClassCastException, IndexOutOfBoundsException**  
Casts member or array element to Boolean  

**Object[ ] getArray ( String memberName, int... indices )        throws ClassCastException, IndexOutOfBoundsException**  
Casts member to array of Objects  

**&lt;T\> T castMember ( T sample, String memberName, int... indices ) throws ClassCastException, IndexOutOfBoundsException**  
Converts the value of a Json member or an array element to the sample class. See notes for a JSON.cast methods    

**&lt;T\> T castMember ( Class &lt;T\> cls, String memberName, int... indices ) throws ClassCastException, IndexOutOfBoundsException**  
Converts the value of a Json member or an array element to the specified class. See notes for a JSON.cast methods  
<!--
**Json normalize() throws IOException, ParseException**  
Returns normalized Json object  
-->
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
// "phones" is nested Json object
  .set("phones", 
    new Json("home", "123-4567","work","890-1234"));
System.out.println(JSON.toJSON(j, 2));
/* console output
{
  "personId": 1234, 
  "firstName": "John", 
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
<a name="Native"></a>
#### interface Nativizer  
  
<p style="background-color: #B0C4DE;">
&emsp;<b>Methods:</b>
</p>  

**&lt;T\> T fromNative(Object nativeObj)**  
Returns target object.  

**&lt;T\> T toNative()**  
Returns [native](#native) object.   
  
#### JSON Class Registry methods  
  
**Nativizer JSON.registerClass(Class cls, Nativizer serializer)**  
**Nativizer JSON.getRegistered(Class cls)**  
**Class[] JSON.listRegistered()**
**Nativizer JSON.unregisterClass(Class cls)**  
  
<p style="background-color: #B0C4DE;">
&emsp;<b>Example:</b>
</p>  

```java
/*
 * Create and register serializer for Java File class
 * An instance of a File will be represented by its path
 */
static class FileClass implements Nativizer {
  FileClass();
  @Override
  <T> T toNative(Object target) {
    return (T) (new Json()).set("filePath", ((File)target).getPath());
  }
  @Override
  <T> T fromNative(Object native) {
  	return (T) new File(((Json)native).getString("filePath"));
  }
}

public static void main(String args) {
  File file = new File("./example.json");
  System.out.println(JSON.toJSON(file)); // before registering a Java class
  JSON.registerClass(File.class, new FileClass());
  System.out.println(JSON.toJSON(file)); // after
/* console output:
{ }
{ "filePath": ".\/example.json" }
*/	
}
```  
  
<a name="Nativefier"></a>
#### abstract class Nativefier implements Nativizer
Java object extender. Unload/load fields of a Java object to/from a [native](#native) objects. Notes:  
\- visibility of object fields as from the object constructor;  
\- Java transient and final fields are ignored;  
\- it is highly recommended to initialize accessible fields and create a default constructor;  
\- non-native fields MUST be managed using replacer/reviver.  
  
<p style="background-color: #B0C4DE;">
&emsp;<b>Constants:</b>
</p>  
  
**protected static final transient Object IGNORED**  
Returned from the replacer/reviver methods to skip the field  

<p style="background-color: #B0C4DE;">
&emsp;<b>Methods:</b>
</p>  

**Json toNative ( ) throws IllegalArgumentException, IllegalAccessException;**  
Returns a Json object from this object  

**&lt;T\> T fromNative ( Object nativeObj ) throws IllegalArgumentException, IllegalAccessException;**  
Loads Json to this object. Returns this object.  

**Json toNative ( Object targetObj ) throws IllegalArgumentException, IllegalAccessException;**  
Returns a Json object from the target object  

**&lt;T\> T fromNative ( T targetObj, Object nativeObj ) throws IllegalArgumentException, IllegalAccessException;**  
 Loads Json to target object. Returns target object.  
 
**protected Object replacer ( String name, Object value );**  
Applies on unloading:  
\- name is object field class and name delimitet with dot (.), value is object field value;  
\- first call with the target object class name and the target object as the value;  
\- returns [native](#native) object or IGNORED  

**protected Object reviver ( String name, Object value );**  
Applies on loading:  
\- name is object field class and name delimited with dot (.), value is Json-supported object;  
\- first call with the target object class name and the Json object as value;  
\- returns a value that is compatible with the object field or IGNORED  

**static boolean isClassName ( String name );**  
Returns true if name is class name.  

<!--
**protected &lt;T> T getTarget ( );**  
Get target object. Accessible from replacer/reviver
-->  

**String toString ( );**  
Overridden. Generate JSON text as a single line  
  
<!--
**protected &lt;T\> T castMember( T sample, String memberName, Json jsonObj );**  
Returns the sample if Json member does not exists or is null  
-->  

<p style="background-color: #B0C4DE;">
&emsp;<b>Example:</b>
</p>  
  
```java
/*
 * Create Nativefier instance
 */
public static class Person extends Nativefier {
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
  throws IllegalArgumentException, IllegalAccessException,
         IOException, ParseException {

// create and fill Person object
  Person person = new Person();
  person.personId = 12345;
  person.firstName = "John";
  person.lastName = "Doe";
  person.phones.put("home", "123-4567");
  person.phones.put("work", "789-0123");
// unload person to string
  String s = person.toNative().toJSON();
  System.out.println(s);
/* console output:
{"personId": 12345, "firstName": "John", "lastName": "Doe", "married": false, "phones": {"work": "789-0123", "home": "123-4567"}}
*/
// load person from string
  person = new Person();
  person.fromNative(new Json(s));
  System.out.println(person.phones.getString("home"));
));
/* console output:
123-4567
*/
}
```  
  
**See usage here:**  
  ./test/json/JsonTest.java  
  ./test/json/JsonCastTest.java  
  ./test/json/NativefierTest.java  
