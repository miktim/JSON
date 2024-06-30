/*
 * JsonObject abstract class. MIT (c) 2022-2024 miktim@mail.ru
 + Java object extender
 *
 * Unloads/loads Java object accessible fields to/from JSON object.
 * - Java final, transient fields are ignored;
 *   TODO initialized final object fields?
 * - see the notes on the Json methods set/get/cast.
 *
 * Created: april 2022
 */
package org.miktim.json;

import java.io.IOException;
import java.text.ParseException;

public abstract class JsonObject extends ObjectConverter{ // extends JsonObjectCommons {

    public final Json toJson()
            throws IllegalArgumentException, IllegalAccessException {
        return unload(this, this);
    }

    public final String toJSON() 
            throws IllegalArgumentException, IllegalAccessException, IOException {
        return JSON.toJSON(toJson());
    }

    @SuppressWarnings("unchecked")
    public final <T> T fromJson(Object json)
            throws IllegalArgumentException, IllegalAccessException {
        return (T) load(this, this, json);
    }

    @SuppressWarnings("unchecked")
    public final <T> T fromJSON(String jsonText)
            throws IOException, ParseException, IllegalArgumentException, IllegalAccessException {
        return (T) fromJson(JSON.fromJSON(jsonText));
    }

}
