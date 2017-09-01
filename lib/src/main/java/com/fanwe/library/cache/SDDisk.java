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

    private StringHandler mStringHandler;
    private SerializableHandler mSerializableHandler;

    protected SDDisk(File directory)
    {
        super(directory);
        mStringHandler = new StringHandler(directory);

        mSerializableHandler = new SerializableHandler(directory);
        mSerializableHandler.setKeyPrefix(SERIALIZABLE);
    }

    /**
     * 打开"Android/data/包名/files/files"目录
     *
     * @return
     */
    public static SDDisk open()
    {
        return open(DEFAULT_FILE_DIR);
    }

    /**
     * 打开"Android/data/包名/files/dirName"目录
     *
     * @param dirName
     * @return
     */
    public static SDDisk open(String dirName)
    {
        return openDir(getFileDir(dirName));
    }

    /**
     * 打开"Android/data/包名/cache/cache"目录
     *
     * @return
     */
    public static SDDisk openCache()
    {
        return open(DEFAULT_CACHE_DIR);
    }

    /**
     * 打开"Android/data/包名/cache/dirName"目录
     *
     * @param dirName
     * @return
     */
    public static SDDisk openCache(String dirName)
    {
        return openDir(getCacheDir(dirName));
    }

    /**
     * 打开指定的目录
     *
     * @param directory
     * @return
     */
    public static SDDisk openDir(File directory)
    {
        return new SDDisk(directory);
    }

    @Override
    public SDDisk setObjectConverter(IObjectConverter objectConverter)
    {
        super.setObjectConverter(objectConverter);
        return this;
    }

    @Override
    public SDDisk setEncryptConverter(IEncryptConverter encryptConverter)
    {
        super.setEncryptConverter(encryptConverter);
        return this;
    }

    @Override
    public boolean hasInt(String key)
    {
        mStringHandler.setKeyPrefix(INT);
        return mStringHandler.hasObject(key);
    }

    @Override
    public boolean removeInt(String key)
    {
        mStringHandler.setKeyPrefix(INT);
        return mStringHandler.removeObject(key);
    }

    @Override
    public boolean putInt(String key, int value)
    {
        mStringHandler.setKeyPrefix(INT);
        return mStringHandler.putObject(key, String.valueOf(value));
    }

    @Override
    public int getInt(String key, int defValue)
    {
        mStringHandler.setKeyPrefix(INT);
        String content = mStringHandler.getObject(key);
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
        mStringHandler.setKeyPrefix(LONG);
        return mStringHandler.hasObject(key);
    }

    @Override
    public boolean removeLong(String key)
    {
        mStringHandler.setKeyPrefix(LONG);
        return mStringHandler.removeObject(key);
    }

    @Override
    public boolean putLong(String key, long value)
    {
        mStringHandler.setKeyPrefix(LONG);
        return mStringHandler.putObject(key, String.valueOf(value));
    }

    @Override
    public long getLong(String key, long defValue)
    {
        mStringHandler.setKeyPrefix(LONG);
        String content = mStringHandler.getObject(key);
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
        mStringHandler.setKeyPrefix(FLOAT);
        return mStringHandler.hasObject(key);
    }

    @Override
    public boolean removeFloat(String key)
    {
        mStringHandler.setKeyPrefix(FLOAT);
        return mStringHandler.removeObject(key);
    }

    @Override
    public boolean putFloat(String key, float value)
    {
        mStringHandler.setKeyPrefix(FLOAT);
        return mStringHandler.putObject(key, String.valueOf(value));
    }

    @Override
    public float getFloat(String key, float defValue)
    {
        mStringHandler.setKeyPrefix(FLOAT);
        String content = mStringHandler.getObject(key);
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
        mStringHandler.setKeyPrefix(DOUBLE);
        return mStringHandler.hasObject(key);
    }

    @Override
    public boolean removeDouble(String key)
    {
        mStringHandler.setKeyPrefix(DOUBLE);
        return mStringHandler.removeObject(key);
    }

    @Override
    public boolean putDouble(String key, double value)
    {
        mStringHandler.setKeyPrefix(DOUBLE);
        return mStringHandler.putObject(key, String.valueOf(value));
    }

    @Override
    public double getDouble(String key, double defValue)
    {
        mStringHandler.setKeyPrefix(DOUBLE);
        String content = mStringHandler.getObject(key);
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
        mStringHandler.setKeyPrefix(BOOLEAN);
        return mStringHandler.hasObject(key);
    }

    @Override
    public boolean removeBoolean(String key)
    {
        mStringHandler.setKeyPrefix(BOOLEAN);
        return mStringHandler.removeObject(key);
    }

    @Override
    public boolean putBoolean(String key, boolean value)
    {
        mStringHandler.setKeyPrefix(BOOLEAN);
        return mStringHandler.putObject(key, String.valueOf(value));
    }

    @Override
    public boolean getBoolean(String key, boolean defValue)
    {
        mStringHandler.setKeyPrefix(BOOLEAN);
        String content = mStringHandler.getObject(key);
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
        mStringHandler.setKeyPrefix(STRING);
        return mStringHandler.hasObject(key);
    }

    @Override
    public boolean removeString(String key)
    {
        mStringHandler.setKeyPrefix(STRING);
        return mStringHandler.removeObject(key);
    }

    @Override
    public boolean putString(String key, String data)
    {
        mStringHandler.setKeyPrefix(STRING);
        return putString(key, data, false);
    }

    @Override
    public boolean putString(String key, String data, boolean encrypt)
    {
        mStringHandler.setKeyPrefix(STRING);
        mStringHandler.setEncrypt(encrypt);
        return mStringHandler.putObject(key, data);
    }

    @Override
    public String getString(String key)
    {
        mStringHandler.setKeyPrefix(STRING);
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
