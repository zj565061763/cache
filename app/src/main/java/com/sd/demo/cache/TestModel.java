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
        StringBuilder sb = new StringBuilder(" | ");
        sb.append("valueInt:").append(valueInt).append(" | ");
        sb.append("valueLong:").append(valueLong).append(" | ");
        sb.append("valueFloat:").append(valueFloat).append(" | ");
        sb.append("valueDouble:").append(valueDouble).append(" | ");
        sb.append("valueBoolean:").append(valueBoolean).append(" | ");
        sb.append("valueString:").append(valueString).append(" | ");
        return sb.toString();
    }
}
