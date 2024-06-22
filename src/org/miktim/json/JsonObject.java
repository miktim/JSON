/*
 * JsonObject abstract class. MIT (c) 2022-2024 miktim@mail.ru
 *
 * Unloads/loads Java object accessible fields to/from JSON object.
 * - Java final, transient fields are ignored;
 *   TODO initialized final object fields?
 * - see JSON set/get/cast rules for Java object fields in the notes for JSON object
 *   and JSONAdapter.
 *
 * Created: april 2022
 */
package org.miktim.json;

import java.io.IOException;
import java.text.ParseException;

public abstract class JsonObject extends AbstractObject{ // extends JsonObjectCommons {

    protected static final Object IGNORED = new Object();

    public Object toJson()
            throws IllegalArgumentException, IllegalAccessException {
        return unload(this, this);
    }

    public String toJSON() throws IllegalArgumentException, IllegalAccessException {
        return Json.toJSON(toJson());
    }

    public <T> T fromJson(Object json)
            throws IllegalArgumentException, IllegalAccessException {
        return (T) load(this, this, json);
    }

    public <T> T fromJSON(String jsonText)
            throws IOException, ParseException, IllegalArgumentException, IllegalAccessException {
        return (T) fromJson(Json.fromJSON(jsonText));
    }

    public static final ObjectAdapter defaultAdapter = new ObjectAdapter();
}
