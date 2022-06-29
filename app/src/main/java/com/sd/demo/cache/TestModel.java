package com.sd.demo.cache;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/8/29.
 */

public class TestModel implements Serializable {
    static final long serialVersionUID = 0;

    private int valueInt = 10;
    private long valueLong = 20;

    private float valueFloat = 0.123f;
    private double valueDouble = 0.345678d;

    private boolean valueBoolean = true;

    private String valueString = "hello";
    private Map<String, String> mapString = new HashMap<>();

    public TestModel() {
        for (int i = 0; i < 1000; i++) {
            mapString.put(String.valueOf(i), String.valueOf(i + i));
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("\r\n");
        sb.append("valueInt:").append(valueInt).append("\r\n");
        sb.append("valueLong:").append(valueLong).append("\r\n");
        sb.append("valueFloat:").append(valueFloat).append("\r\n");
        sb.append("valueDouble:").append(valueDouble).append("\r\n");
        sb.append("valueBoolean:").append(valueBoolean).append("\r\n");
        sb.append("valueString:").append(valueString).append("\r\n");
        return sb.toString();
    }
}
