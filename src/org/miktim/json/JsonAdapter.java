/*
 * JsonAdapter class. MIT (c) 2024 miktim@mail.ru
 * Java object to Json converter
 *
 * Unloads/loads Java object accessible fields to/from Json object.
 * - Java final, transient fields are ignored;
 *   TODO initialized final object fields?
 * - see the notes for Json set/get/cast methods.
 */
package org.miktim.json;

import java.io.IOException;
import java.text.ParseException;

public class JsonAdapter extends ObjectConverter { //

    public static transient JsonAdapter defaultAdapter = new JsonAdapter();

    public final <T> T fromJSON(T target, String jsonText)
            throws IllegalArgumentException, IllegalAccessException, IOException, ParseException {
        return (T) load(this, target, new Json(jsonText));
    }

    public final <T> T fromJson(T target, Json json)
            throws IllegalArgumentException, IllegalAccessException {
        return (T) load(this, target, json);
    }

    public final Json toJson(Object target)
            throws IllegalArgumentException, IllegalAccessException {
        return (Json) unload(this, target);
    }
   
}
