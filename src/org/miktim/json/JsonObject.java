/**
 * JsonObject abstract class. MIT (c) 2022-2025 miktim@mail.ru
 *
 * Converts Java object instance accessible fields to/from Json object.
 * Java final and transient fields are ignored.
 *
 * Created: april 2022
 */

package org.miktim.json;

public abstract class JsonObject extends JsonConverter {

    @SuppressWarnings("unchecked")
    public Json toJson() {
//            throws IllegalArgumentException, IllegalAccessException {
        return unload(this, this);
    }

    @SuppressWarnings("unchecked")
    public <T> T fromJson(Json jsonObj) {
//            throws IllegalArgumentException, IllegalAccessException {
        return (T) load(this, this, jsonObj);
    }
    
    @Override
    public String toString() {
        try {
            return this.toJson().toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }
}
