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
    private static final String DEFAULT_FILE_DIR = "disk_file";
    private static final String DEFAULT_CACHE_DIR = "disk_cache";

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

    private ObjectHandler mObjectHandler;
    private SerializableHandler mSerializableHandler;

    protected SDDisk(File directory)
    {
        super(directory);
    }

    /**
     * 打开"Android/data/包名/files/disk_file"目录
     *
     * @return
     */
    public static SDDisk open()
    {
        return openDir(getFileDir(DEFAULT_FILE_DIR));
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
     * 打开"Android/data/包名/cache/disk_cache"目录
     *
     * @return
     */
    public static SDDisk openCache()
    {
        return openDir(getCacheDir(DEFAULT_CACHE_DIR));
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
    public SDDisk setEncrypt(boolean encrypt)
    {
        super.setEncrypt(encrypt);
        return this;
    }

    @Override
    public SDDisk setEncryptConverter(IEncryptConverter encryptConverter)
    {
        super.setEncryptConverter(encryptConverter);
        return this;
    }

    @Override
    public SDDisk setObjectConverter(IObjectConverter objectConverter)
    {
        super.setObjectConverter(objectConverter);
        return this;
    }

    @Override
    public SDDisk setMemorySupport(boolean memorySupport)
    {
        super.setMemorySupport(memorySupport);
        return this;
    }

    //---------- int start ----------

    private StringHandler getIntHandler()
    {
        if (mIntHandler == null)
        {
            mIntHandler = new StringHandler(getDirectory(), INT);
            mIntHandler.setDiskConfig(this);
        }
        return mIntHandler;
    }

    @Override
    public boolean hasInt(String key)
    {
        synchronized (INT)
        {
            return getIntHandler().hasObject(key);
        }
    }

    @Override
    public boolean removeInt(String key)
    {
        synchronized (INT)
        {
            removeMemory(key, getIntHandler());
            return getIntHandler().removeObject(key);
        }
    }

    @Override
    public boolean putInt(String key, int value)
    {
        synchronized (INT)
        {
            boolean result = getIntHandler().putObject(key, String.valueOf(value));
            if (result)
            {
                putMemory(key, value, getIntHandler());
            }
            return result;
        }
    }

    @Override
    public int getInt(String key, int defValue)
    {
        synchronized (INT)
        {
            if (isMemorySupport())
            {
                Integer result = getMemory(key, getIntHandler());
                if (result != null) return result;
            }

            String content = getIntHandler().getObject(key, null);
            if (content != null)
            {
                return Integer.valueOf(content);
            } else
            {
                return defValue;
            }
        }
    }

    //---------- long start ----------

    private StringHandler getLongHandler()
    {
        if (mLongHandler == null)
        {
            mLongHandler = new StringHandler(getDirectory(), LONG);
            mLongHandler.setDiskConfig(this);
        }
        return mLongHandler;
    }

    @Override
    public boolean hasLong(String key)
    {
        synchronized (LONG)
        {
            return getLongHandler().hasObject(key);
        }
    }

    @Override
    public boolean removeLong(String key)
    {
        synchronized (LONG)
        {
            removeMemory(key, getLongHandler());
            return getLongHandler().removeObject(key);
        }
    }

    @Override
    public boolean putLong(String key, long value)
    {
        synchronized (LONG)
        {
            boolean result = getLongHandler().putObject(key, String.valueOf(value));
            if (result)
            {
                putMemory(key, value, getLongHandler());
            }
            return result;
        }
    }

    @Override
    public long getLong(String key, long defValue)
    {
        synchronized (LONG)
        {
            if (isMemorySupport())
            {
                Long result = getMemory(key, getLongHandler());
                if (result != null) return result;
            }

            String content = getLongHandler().getObject(key, null);
            if (content != null)
            {
                return Long.valueOf(content);
            } else
            {
                return defValue;
            }
        }
    }

    //---------- float start ----------

    private StringHandler getFloatHandler()
    {
        if (mFloatHandler == null)
        {
            mFloatHandler = new StringHandler(getDirectory(), FLOAT);
            mFloatHandler.setDiskConfig(this);
        }
        return mFloatHandler;
    }

    @Override
    public boolean hasFloat(String key)
    {
        synchronized (FLOAT)
        {
            return getFloatHandler().hasObject(key);
        }
    }

    @Override
    public boolean removeFloat(String key)
    {
        synchronized (FLOAT)
        {
            removeMemory(key, getFloatHandler());
            return getFloatHandler().removeObject(key);
        }
    }

    @Override
    public boolean putFloat(String key, float value)
    {
        synchronized (FLOAT)
        {
            boolean result = getFloatHandler().putObject(key, String.valueOf(value));
            if (result)
            {
                putMemory(key, value, getFloatHandler());
            }
            return result;
        }
    }

    @Override
    public float getFloat(String key, float defValue)
    {
        synchronized (FLOAT)
        {
            if (isMemorySupport())
            {
                Float result = getMemory(key, getFloatHandler());
                if (result != null) return result;
            }

            String content = getFloatHandler().getObject(key, null);
            if (content != null)
            {
                return Float.valueOf(content);
            } else
            {
                return defValue;
            }
        }
    }

    //---------- double start ----------

    private StringHandler getDoubleHandler()
    {
        if (mDoubleHandler == null)
        {
            mDoubleHandler = new StringHandler(getDirectory(), DOUBLE);
            mDoubleHandler.setDiskConfig(this);
        }
        return mDoubleHandler;
    }

    @Override
    public boolean hasDouble(String key)
    {
        synchronized (DOUBLE)
        {
            return getDoubleHandler().hasObject(key);
        }
    }

    @Override
    public boolean removeDouble(String key)
    {
        synchronized (DOUBLE)
        {
            removeMemory(key, getDoubleHandler());
            return getDoubleHandler().removeObject(key);
        }
    }

    @Override
    public boolean putDouble(String key, double value)
    {
        synchronized (DOUBLE)
        {
            boolean result = getDoubleHandler().putObject(key, String.valueOf(value));
            if (result)
            {
                putMemory(key, value, getDoubleHandler());
            }
            return result;
        }
    }

    @Override
    public double getDouble(String key, double defValue)
    {
        synchronized (DOUBLE)
        {
            if (isMemorySupport())
            {
                Double result = getMemory(key, getDoubleHandler());
                if (result != null) return result;
            }

            String content = getDoubleHandler().getObject(key, null);
            if (content != null)
            {
                return Double.valueOf(content);
            } else
            {
                return defValue;
            }
        }
    }

    //---------- boolean start ----------

    private StringHandler getBooleanHandler()
    {
        if (mBooleanHandler == null)
        {
            mBooleanHandler = new StringHandler(getDirectory(), BOOLEAN);
            mBooleanHandler.setDiskConfig(this);
        }
        return mBooleanHandler;
    }

    @Override
    public boolean hasBoolean(String key)
    {
        synchronized (BOOLEAN)
        {
            return getBooleanHandler().hasObject(key);
        }
    }

    @Override
    public boolean removeBoolean(String key)
    {
        synchronized (BOOLEAN)
        {
            removeMemory(key, getBooleanHandler());
            return getBooleanHandler().removeObject(key);
        }
    }

    @Override
    public boolean putBoolean(String key, boolean value)
    {
        synchronized (BOOLEAN)
        {
            boolean result = getBooleanHandler().putObject(key, String.valueOf(value));
            if (result)
            {
                putMemory(key, value, getBooleanHandler());
            }
            return result;
        }
    }

    @Override
    public boolean getBoolean(String key, boolean defValue)
    {
        synchronized (BOOLEAN)
        {
            if (isMemorySupport())
            {
                Boolean result = getMemory(key, getBooleanHandler());
                if (result != null) return result;
            }

            String content = getBooleanHandler().getObject(key, null);
            if (content != null)
            {
                return Boolean.valueOf(content);
            } else
            {
                return defValue;
            }
        }
    }

    //---------- string start ----------

    private StringHandler getStringHandler()
    {
        if (mStringHandler == null)
        {
            mStringHandler = new StringHandler(getDirectory(), STRING);
            mStringHandler.setDiskConfig(this);
        }
        return mStringHandler;
    }

    @Override
    public boolean hasString(String key)
    {
        synchronized (STRING)
        {
            return getStringHandler().hasObject(key);
        }
    }

    @Override
    public boolean removeString(String key)
    {
        synchronized (STRING)
        {
            removeMemory(key, getStringHandler());
            return getStringHandler().removeObject(key);
        }
    }

    @Override
    public boolean putString(String key, String value)
    {
        synchronized (STRING)
        {
            boolean result = getStringHandler().putObject(key, value);
            if (result)
            {
                putMemory(key, value, getStringHandler());
            }
            return result;
        }
    }

    @Override
    public String getString(String key)
    {
        synchronized (STRING)
        {
            if (isMemorySupport())
            {
                String result = getMemory(key, getStringHandler());
                if (result != null) return result;
            }

            return getStringHandler().getObject(key, null);
        }
    }

    //---------- object start ----------


    private ObjectHandler getObjectHandler()
    {
        if (mObjectHandler == null)
        {
            mObjectHandler = new ObjectHandler(getDirectory(), OBJECT);
            mObjectHandler.setDiskConfig(this);
        }
        return mObjectHandler;
    }

    @Override
    public boolean hasObject(Class clazz)
    {
        synchronized (OBJECT)
        {
            return getObjectHandler().hasObject(clazz.getName());
        }
    }

    @Override
    public boolean removeObject(Class clazz)
    {
        synchronized (OBJECT)
        {
            removeMemory(clazz.getName(), getObjectHandler());
            return getObjectHandler().removeObject(clazz.getName());
        }
    }

    @Override
    public boolean putObject(Object object)
    {
        synchronized (OBJECT)
        {
            boolean result = getObjectHandler().putObject(object.getClass().getName(), object);
            if (result)
            {
                putMemory(object.getClass().getName(), object, getObjectHandler());
            }
            return result;
        }
    }

    @Override
    public <T> T getObject(Class<T> clazz)
    {
        synchronized (OBJECT)
        {
            if (isMemorySupport())
            {
                T result = getMemory(clazz.getName(), getObjectHandler());
                if (result != null) return result;
            }

            return (T) getObjectHandler().getObject(clazz.getName(), clazz);
        }
    }

    //---------- Serializable start ----------

    private SerializableHandler getSerializableHandler()
    {
        if (mSerializableHandler == null)
        {
            mSerializableHandler = new SerializableHandler(getDirectory(), SERIALIZABLE);
            mSerializableHandler.setDiskConfig(this);
        }
        return mSerializableHandler;
    }

    @Override
    public boolean hasSerializable(Class clazz)
    {
        synchronized (SERIALIZABLE)
        {
            return getSerializableHandler().hasObject(clazz.getName());
        }
    }

    @Override
    public boolean removeSerializable(Class clazz)
    {
        synchronized (SERIALIZABLE)
        {
            removeMemory(clazz.getName(), getSerializableHandler());
            return getSerializableHandler().removeObject(clazz.getName());
        }
    }

    @Override
    public boolean putSerializable(Serializable object)
    {
        synchronized (SERIALIZABLE)
        {
            boolean result = getSerializableHandler().putObject(object.getClass().getName(), object);
            if (result)
            {
                putMemory(object.getClass().getName(), object, getSerializableHandler());
            }
            return result;
        }
    }

    @Override
    public <T extends Serializable> T getSerializable(Class<T> clazz)
    {
        synchronized (SERIALIZABLE)
        {
            if (isMemorySupport())
            {
                T result = getMemory(clazz.getName(), getSerializableHandler());
                if (result != null) return result;
            }

            return (T) getSerializableHandler().getObject(clazz.getName(), clazz);
        }
    }
}
