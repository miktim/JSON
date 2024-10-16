### Java 7+/Android JSON parser/generator, MIT (c) 2020-2024 miktim@mail.ru

**Release notes:**  
\- Java SE 7+/Android RFC 8259 compliant package  
    (see: https://datatracker.ietf.org/doc/rfc8259/?include_text=1 );  
\- no external dependencies;  
\-  "JSON" means text in JSON format. JSON text exchanged between systems MUST be encoded using UTF-8 (default charset);  
\-  "Json" means the Java representation of a JSON object.
  
**package org.miktim.json;**


### Class JSON.
The class contains static methods for parsing/generating text in JSON format.  
\- JSON parser converts JSON text to Java objects:  
  Json object, String, Number, Boolean, null, Object[ ] array of listed types;  
\- when the names within an object are not unique, parser stores the last value only;  
\- in addition to listed types, the generator converts Java Lists to JSON arrays and Java Maps to JSON objects. The null key is converted to a "null" member name.
Other Java objects are converted to string representation.
  
<p style="background-color: #B0C4DE;">
&emsp;<b>Methods:</b>
</p>  

**static Object fromJSON(String jsonText) throws IOException, ParseException**  
Parse JSON text  

**static Object fromJSON(InputStream in, String charsetName) throws IOException, ParseException**  
Parse text in JSON format from a stream with the specified encoding 

**static String toJSON(Object obj) throws IOException;**  
Generate JSON text as single line  

**static String toJSON(Object obj, int space) throws IOException**  
Generate the text in JSON format with the specified number of spaces in the indentation  

**static &lt;T\>T toJSON(T obj, OutputStream out, int space, String charsetName) throws IOException**  
Generate the text in JSON format into a stream with the specified indentation and encoding  
  
Methods for converting objects supported by JSON to a Java primitive or an array of Java primitives.  
\- sample must be initialized;  
\- casting numbers may involve rounding or truncation;  
\- casting null to a Java primitive returns corresponding initial value;  
\- casting null to an array returns an empty array;  
\- casting null to String returns "null";  
\- casting null to other Java objects returns null;  
\- casting to null returns null.  
  

**static &lt;T\> T cast(Object obj, T sample) throws ClassCastException**  
Cast Java object by sample  

**static &lt;T\> T cast(Object obj, Class &lt;T\> cls) throws ClassCastException**  
Cast Java object by class  


### Class Json extends HashMap &lt;String, Object\>
This class is a Java representation of a JSON object.
Json member types:  
Json object, String, Number, Boolean, null, Object[ ] array of listed types.
   
Put, set, get notes:  
\- Json object setters accept any Java object, all Java primitives and primitive arrays;  
\- RFC 8259 does not recommend using Java BigDecimal and BigInteger as Json member values;  
\- AVOID RECURSION!;  
\- put, set methods cast Java primitives to the corresponding objects.  
      Java objects and arrays are stored "as is" (as reference). 
      For example: float -> Float, int[ ][ ] -> int[ ][ ], String[ ] -> String[ ]  
\- after JSON text parsing or normalization, they are stored as:  
Double, Object[ ]{Object[ ]{Long,...}, Object[ ]{Long,...}}, Object[ ] {String,...};  
\- getters return null if the member does not exist.

<p style="background-color: #B0C4DE;">
&emsp;<b>Constructors:</b>
</p>  

**Json(Object... members) throws IndexOutOfBoundsException**  
Members is a name,value pairs  

**Json(String jsonText) throws IOException, ParseException**  
Create Json object from String  

**Json(InputStream inStream) throws IOException, ParseException**  
Create Json object from UTF-8 encoded stream.

<p style="background-color: #B0C4DE;">
&emsp;<b>Methods:</b>
</p>  

**String[ ] listNames()**  
Returns list of member names 

**boolean exists(String memberName)**  
Alias of the inherited containsKey()  

**Object put(String memberName, Object value);**  
inherited  

**Json set(Object memberName, Object value);**  
Create or replace member. Returns this.   

**Object get(String memberName)**  
inherited  

**Object remove(String memberName);**  
inherited  
  
**Object get(String memberName, int... indices) throws IndexOutOfBoundsException**  
Returns null, value or array element  

**Json getJson(String memberName, int... indices) throws ClassCastException, IndexOutOfBoundsException**  
Cast member or array element to Json object  
  
**String getString(String memberName, int... indices)     throws ClassCastException, IndexOutOfBoundsException**
Cast member or array element to String  

**Number getNumber(String memberName, int... indices)         throws ClassCastException, IndexOutOfBoundsException**  
Cast member or array element to Number
  
**Boolean getBoolean(String memberName, int... indices)        throws ClassCastException, IndexOutOfBoundsException**  
Cast member or array element to Boolean  

**Object[ ] getArray(String memberName, int... indices)        throws ClassCastException, IndexOutOfBoundsException**  
Cast member or array element to Object array  

**&lt;T\> T castMember(T sample, String memberName, int... indices) throws ClassCastException, IndexOutOfBoundsException**  
Casting Json member value or array element by sample. See notes for a JSON.cast methods    

**&lt;T\> T castMember(Class &lt;T\> cls, String memberName, int... indices) throws ClassCastException, IndexOutOfBoundsException**  
Casting Json member value or array element by Class. See notes for a JSON.cast methods  

**Json normalize() throws IOException, ParseException**  
Returns normalized Json object  

**String toString()**  
overridden. Returns JSON text as single line  

**String toJSON()**  
Stringify Json object as single line  

**String toJSON(String memberName, int... indices)**  
Stringify member value or array element as single line  

**Json toJSON(OutputStream outStream) throws IOException**  
OutStream is UTF-8 encoded single line JSON text. Returns this     
  
  
### Abstract Class JsonObject
Java object extender. Unload/load fields of a Java object to/from a Json object.  
\- visibility of object fields as from the object constructor;  
\- Java transient fields are ignored;  
\- Java final fields are unloaded, but not initialized at load;  
\- the accessible fields of the object MUST be initialized;  
\- see Json set/get/cast rules for Java object fields in the notes for JSON object;  
\- arrays of custom objects and collections MUST be managed using replacer/reviewer;  
\- it is recommended to create a default constructor  
  
<p style="background-color: #B0C4DE;">
&emsp;<b>Constants:</b>
</p>  
  
**protected static final transient Object IGNORED**  
Returned from the replacer/reviver methods to skip the field  

<p style="background-color: #B0C4DE;">
&emsp;<b>Methods:</b>
</p>  

**Json toJson() throws IllegalArgumentException, IllegalAccessException;**  
Returns a Json object from this object  

**&lt;T\> T fromJson(Json jsonObj) throws IllegalArgumentException, IllegalAccessException;**  
Loads Json to this object. Returns this object.  

**Json toJson(Object targetObj) throws IllegalArgumentException, IllegalAccessException;**  
Returns a Json object from the target object  

**&lt;T\> T fromJson(T targetObj, Json json) throws IllegalArgumentException, IllegalAccessException;**  
 Loads Json to target object. Returns target object.  
 
**protected Object replacer(String name, Object value);**  
Applies on unloading:  
\- name is object field class and name delimitet with dot (.), value is object field value;  
\- first call with the target object class name and the target object as the value;  
\- returns Json-supported object or IGNORED  

**protected Object reviver(String name, Object value);**  
Applies on loading:  
\- name is object field class and name delimited with dot (.), value is Json-supported object;  
\- first call with the target object class name and the Json object as value;  
\- returns a value that is compatible with the object field or IGNORED  

**protected &lt;T> T getTarget( );**  
Get target object. Accessible from replacer/reviver

**String toString( );**  
Overridden. Generate JSON text as a single line

**static boolean isClassName(String name);**  

**protected &lt;T\> T castMember(String memberName, Json jsonObj, T sample);**  
Returns the sample if Json member does not exists or is null

 
**See usage here:**  
  ./test/json/JsonTest.java  
  ./test/json/JsonCastTest.java  
  ./test/json/JsonObjectTest.java  
