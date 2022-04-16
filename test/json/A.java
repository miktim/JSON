/*
 * JSONObject test. MIT (c) 2022 miktim@mail.ru
 */
package json;

public class A extends J {

    private double[] aad = {1.1, 2.2, 3.3};
    int ai = 6789;
    protected float af = 12.345f;
    public String aS = "aS value";

    @Override
    protected Object replacer(String name, Object value) {
        logName(this , name);
        return value;
    }

    @Override
    protected Object reviver(String name, Object value) {
        logName(this , name);
        return value;
    }

}
