/*
 * JSONObject test. MIT (c) 2022 miktim@mail.ru
 */
package json;

import org.miktim.json.JSONObject;
 
public class J extends JSONObject {

    public short js = 10;
    char[] jac = {'a', 'b', 'c'};
    private char jc = 'c';
    protected int ji = 5678;

    public char getJc() {
        return jc;
    }

    public void setJc(char c) {
        jc = c;
    }
    public static void log(Object s) {
        System.out.println(s);
    }

    public static void logName(Object obj, String name) {
        log(isClassName(name) ? name : obj.getClass().getSimpleName() + ":" + name);
    }

    @Override
    protected Object replacer(String name, Object value) {
        logName(this, name);
        return value;
    }

    @Override
    protected Object reviver(String name, Object value) {
        logName(this, name);
        return value;
    }

}
