/*
 * Copyright (C) 2017 zhengjun, fanwe (http://www.fanwe.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fanwe.library.cache;


import java.io.File;
import java.io.Serializable;

public class SDDisk extends ASDDisk
{
    private static final String DEFAULT_FILE_DIR = "files";
    private static final String DEFAULT_CACHE_DIR = "cache";

    private static final String INT = "int_";
    private static final String LONG = "long_";
    private static final String FLOAT = "float_";
    private static final String DOUBLE = "double_";
    private static final String BOOLEAN = "boolean_";
    private static final String STRING = "string_";
    private static final String OBJECT = "object_";
    private static final String SERIALIZABLE = "serializable_";

    private StringHandler mIntHandler;
    private StringHandler mLongHandler;
    private StringHandler mFloatHandler;
    private StringHandler mDoubleHandler;
    private StringHandler mBooleanHandler;
    private StringHandler mStringHandler;
    private SerializableHandler mSerializableHandler;

    protected SDDisk(File directory)
    {
        super(directory);
        mIntHandler = new StringHandler(directory, INT);
        mLongHandler = new StringHandler(directory, LONG);
        mFloatHandler = new StringHandler(directory, FLOAT);
        mDoubleHandler = new StringHandler(directory, DOUBLE);
        mBooleanHandler = new StringHandler(directory, BOOLEAN);
        mStringHandler = new StringHandler(directory, STRING);
        mSerializableHandler = new SerializableHandler(directory);
    }

    public static SDDisk open()
    {
        return open(DEFAULT_FILE_DIR);
    }

    public static SDDisk open(String dirName)
    {
        return openDir(getFileDir(dirName));
    }

    public static SDDisk openCache()
    {
        return open(DEFAULT_CACHE_DIR);
    }

    public static SDDisk openCache(String dirName)
    {
        return openDir(getCacheDir(dirName));
    }

    public static SDDisk openDir(File directory)
    {
        return new SDDisk(directory);
    }

    @Override
    public boolean hasInt(String key)
    {
        return mIntHandler.hasObject(key);
    }

    @Override
    public boolean removeInt(String key)
    {
        return mIntHandler.removeObject(key);
    }

    @Override
    public boolean putInt(String key, int value)
    {
        return mIntHandler.putObject(key, String.valueOf(value));
    }

    @Override
    public int getInt(String key, int defValue)
    {
        String content = mIntHandler.getObject(key);
        if (content != null)
        {
            return Integer.valueOf(content);
        } else
        {
            return defValue;
        }
    }

    @Override
    public boolean hasLong(String key)
    {
        return mLongHandler.hasObject(key);
    }

    @Override
    public boolean removeLong(String key)
    {
        return mLongHandler.removeObject(key);
    }

    @Override
    public boolean putLong(String key, long value)
    {
        return mLongHandler.putObject(key, String.valueOf(value));
    }

    @Override
    public long getLong(String key, long defValue)
    {
        String content = mLongHandler.getObject(key);
        if (content != null)
        {
            return Long.valueOf(content);
        } else
        {
            return defValue;
        }
    }

    @Override
    public boolean hasFloat(String key)
    {
        return mFloatHandler.hasObject(key);
    }

    @Override
    public boolean removeFloat(String key)
    {
        return mFloatHandler.removeObject(key);
    }

    @Override
    public boolean putFloat(String key, float value)
    {
        return mFloatHandler.putObject(key, String.valueOf(value));
    }

    @Override
    public float getFloat(String key, float defValue)
    {
        String content = mFloatHandler.getObject(key);
        if (content != null)
        {
            return Float.valueOf(content);
        } else
        {
            return defValue;
        }
    }

    @Override
    public boolean hasDouble(String key)
    {
        return mDoubleHandler.hasObject(key);
    }

    @Override
    public boolean removeDouble(String key)
    {
        return mDoubleHandler.removeObject(key);
    }

    @Override
    public boolean putDouble(String key, double value)
    {
        return mDoubleHandler.putObject(key, String.valueOf(value));
    }

    @Override
    public double getDouble(String key, double defValue)
    {
        String content = mDoubleHandler.getObject(key);
        if (content != null)
        {
            return Double.valueOf(content);
        } else
        {
            return defValue;
        }
    }

    @Override
    public boolean hasBoolean(String key)
    {
        return mBooleanHandler.hasObject(key);
    }

    @Override
    public boolean removeBoolean(String key)
    {
        return mBooleanHandler.removeObject(key);
    }

    @Override
    public boolean putBoolean(String key, boolean value)
    {
        return mBooleanHandler.putObject(key, String.valueOf(value));
    }

    @Override
    public boolean getBoolean(String key, boolean defValue)
    {
        String content = mBooleanHandler.getObject(key);
        if (content != null)
        {
            return Boolean.valueOf(content);
        } else
        {
            return defValue;
        }
    }

    @Override
    public boolean hasString(String key)
    {
        return mStringHandler.hasObject(key);
    }

    @Override
    public boolean removeString(String key)
    {
        return mStringHandler.removeObject(key);
    }

    @Override
    public boolean putString(String key, String data)
    {
        return putString(key, data, false);
    }

    @Override
    public boolean putString(String key, String data, boolean encrypt)
    {
        mStringHandler.setEncrypt(encrypt);
        return mStringHandler.putObject(key, data);
    }

    @Override
    public String getString(String key)
    {
        return mStringHandler.getObject(key);
    }

    @Override
    public boolean hasSerializable(Class clazz)
    {
        return mSerializableHandler.hasObject(clazz.getName());
    }

    @Override
    public <T extends Serializable> boolean putSerializable(T object)
    {
        return mSerializableHandler.putObject(object.getClass().getName(), object);
    }

    @Override
    public <T extends Serializable> T getSerializable(Class<T> clazz)
    {
        return (T) mSerializableHandler.getObject(clazz.getName());
    }

    @Override
    public boolean removeSerializable(Class clazz)
    {
        return mSerializableHandler.removeObject(clazz.getName());
    }

    @Override
    public boolean hasObject(Class clazz)
    {
        return false;
    }

    @Override
    public boolean removeObject(Class clazz)
    {
        return false;
    }

    @Override
    public boolean putObject(Object object)
    {
        return false;
    }

    @Override
    public boolean putObject(Object object, boolean encrypt)
    {
        return false;
    }

    @Override
    public <T> T getObject(Class<T> clazz)
    {
        return null;
    }
}
