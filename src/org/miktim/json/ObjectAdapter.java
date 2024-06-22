/*
 * ObjectAdapter class. MIT (c) 2024 miktim@mail.ru
 * Object Adapters constructor
 */
package org.miktim.json;

import java.io.IOException;
import java.text.ParseException;

public class ObjectAdapter extends AbstractObject { //
    
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
