/**
 * JsonConvertible interface, MIT (c) 2024 miktim@mail.ru
 * 
 * Provides conversion of the accessible fields of the Java object instance
 * to package native and vice versa.
 */
package org.miktim.json;

public interface JsonConvertible {

    static final Object IGNORED = JsonConverter.IGNORED;

    Object replacer(String name, Object value);

    Object reviver(String name, Object value);

}
