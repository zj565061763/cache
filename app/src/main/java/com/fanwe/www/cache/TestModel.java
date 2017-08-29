package com.fanwe.www.cache;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/8/29.
 */

public class TestModel implements Serializable
{
    static final long serialVersionUID = 0;

    private int valueInt = 10;
    private long valueLong = 20;

    private float valueFloat = 0.123f;
    private double valueDouble = 0.345678d;

    private boolean valueBoolean = true;

    private String valueString = "hello";


    public int getValueInt()
    {
        return valueInt;
    }

    public void setValueInt(int valueInt)
    {
        this.valueInt = valueInt;
    }

    public long getValueLong()
    {
        return valueLong;
    }

    public void setValueLong(long valueLong)
    {
        this.valueLong = valueLong;
    }

    public float getValueFloat()
    {
        return valueFloat;
    }

    public void setValueFloat(float valueFloat)
    {
        this.valueFloat = valueFloat;
    }

    public double getValueDouble()
    {
        return valueDouble;
    }

    public void setValueDouble(double valueDouble)
    {
        this.valueDouble = valueDouble;
    }

    public boolean isValueBoolean()
    {
        return valueBoolean;
    }

    public void setValueBoolean(boolean valueBoolean)
    {
        this.valueBoolean = valueBoolean;
    }

    public String getValueString()
    {
        return valueString;
    }

    public void setValueString(String valueString)
    {
        this.valueString = valueString;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder("\r\n");
        sb.append("int:").append(valueInt).append("\r\n");
        sb.append("long:").append(valueLong).append("\r\n");
        sb.append("float:").append(valueFloat).append("\r\n");
        sb.append("double:").append(valueDouble).append("\r\n");
        sb.append("boolean:").append(valueBoolean).append("\r\n");
        sb.append("string:").append(valueString).append("\r\n");
        return sb.toString();
    }
}
