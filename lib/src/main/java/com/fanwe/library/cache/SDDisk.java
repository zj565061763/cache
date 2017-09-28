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
import java.util.HashMap;
import java.util.Map;

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

    private StringHandler mStringHandler;

    private ObjectHandler mObjectHandler;
    private SerializableHandler mSerializableHandler;

    private static final Map<String, SDDisk> MAP_INSTANCE = new HashMap<>();

    protected SDDisk(File directory)
    {
        super(directory);
    }

    /**
     * 外部存储"Android/data/包名/files/disk_file"目录
     *
     * @return
     */
    public static SDDisk open()
    {
        return openDir(getExternalFilesDir(DEFAULT_FILE_DIR));
    }

    /**
     * 外部存储"Android/data/包名/files/dirName"目录
     *
     * @param dirName
     * @return
     */
    public static SDDisk open(String dirName)
    {
        return openDir(getExternalFilesDir(dirName));
    }

    /**
     * 外部存储"Android/data/包名/cache/disk_cache"目录
     *
     * @return
     */
    public static SDDisk openCache()
    {
        return openDir(getExternalCacheDir(DEFAULT_CACHE_DIR));
    }

    /**
     * 外部存储"Android/data/包名/cache/dirName"目录
     *
     * @param dirName
     * @return
     */
    public static SDDisk openCache(String dirName)
    {
        return openDir(getExternalCacheDir(dirName));
    }

    /**
     * 内部存储"/data/包名/files/disk_file"目录
     *
     * @return
     */
    public static SDDisk openInternal()
    {
        return openDir(getInternalFilesDir(DEFAULT_FILE_DIR));
    }

    /**
     * 内部存储"/data/包名/files/dirName"目录
     *
     * @param dirName
     * @return
     */
    public static SDDisk openInternal(String dirName)
    {
        return openDir(getInternalFilesDir(dirName));
    }

    /**
     * 内部存储"/data/包名/cache/disk_cache"目录
     *
     * @return
     */
    public static SDDisk openInternalCache()
    {
        return openDir(getInternalCacheDir(DEFAULT_CACHE_DIR));
    }

    /**
     * 内部存储"/data/包名/cache/dirName"目录
     *
     * @param dirName
     * @return
     */
    public static SDDisk openInternalCache(String dirName)
    {
        return openDir(getInternalCacheDir(dirName));
    }

    /**
     * 打开指定的目录
     *
     * @param directory
     * @return
     */
    public synchronized static SDDisk openDir(File directory)
    {
        if (directory == null)
        {
            throw new NullPointerException("directory must not be null");
        } else
        {
            if (!directory.exists() && !directory.mkdirs())
            {
                throw new IllegalArgumentException("directory can not be create, theck the directory path");
            }
        }

        final String path = directory.getAbsolutePath();
        SDDisk instance = MAP_INSTANCE.get(path);
        if (instance == null)
        {
            instance = new SDDisk(directory);
            MAP_INSTANCE.put(path, instance);
        }
        return instance;
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

    @Override
    public boolean hasInt(String key)
    {
        synchronized (this)
        {
            getStringHandler().setKeyPrefix(INT);

            return getStringHandler().hasObject(key);
        }
    }

    @Override
    public boolean removeInt(String key)
    {
        synchronized (this)
        {
            getStringHandler().setKeyPrefix(INT);

            removeMemory(key, getStringHandler());
            return getStringHandler().removeObject(key);
        }
    }

    @Override
    public boolean putInt(String key, int value)
    {
        synchronized (this)
        {
            getStringHandler().setKeyPrefix(INT);

            boolean result = getStringHandler().putObject(key, String.valueOf(value));
            if (result)
            {
                putMemory(key, value, getStringHandler());
            }
            return result;
        }
    }

    @Override
    public int getInt(String key, int defValue)
    {
        synchronized (this)
        {
            getStringHandler().setKeyPrefix(INT);

            if (isMemorySupport())
            {
                Integer result = getMemory(key, getStringHandler());
                if (result != null) return result;
            }

            String content = getStringHandler().getObject(key, null);
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

    @Override
    public boolean hasLong(String key)
    {
        synchronized (this)
        {
            getStringHandler().setKeyPrefix(LONG);

            return getStringHandler().hasObject(key);
        }
    }

    @Override
    public boolean removeLong(String key)
    {
        synchronized (this)
        {
            getStringHandler().setKeyPrefix(LONG);

            removeMemory(key, getStringHandler());
            return getStringHandler().removeObject(key);
        }
    }

    @Override
    public boolean putLong(String key, long value)
    {
        synchronized (this)
        {
            getStringHandler().setKeyPrefix(LONG);

            boolean result = getStringHandler().putObject(key, String.valueOf(value));
            if (result)
            {
                putMemory(key, value, getStringHandler());
            }
            return result;
        }
    }

    @Override
    public long getLong(String key, long defValue)
    {
        synchronized (this)
        {
            getStringHandler().setKeyPrefix(LONG);

            if (isMemorySupport())
            {
                Long result = getMemory(key, getStringHandler());
                if (result != null) return result;
            }

            String content = getStringHandler().getObject(key, null);
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

    @Override
    public boolean hasFloat(String key)
    {
        synchronized (this)
        {
            getStringHandler().setKeyPrefix(FLOAT);

            return getStringHandler().hasObject(key);
        }
    }

    @Override
    public boolean removeFloat(String key)
    {
        synchronized (this)
        {
            getStringHandler().setKeyPrefix(FLOAT);

            removeMemory(key, getStringHandler());
            return getStringHandler().removeObject(key);
        }
    }

    @Override
    public boolean putFloat(String key, float value)
    {
        synchronized (this)
        {
            getStringHandler().setKeyPrefix(FLOAT);

            boolean result = getStringHandler().putObject(key, String.valueOf(value));
            if (result)
            {
                putMemory(key, value, getStringHandler());
            }
            return result;
        }
    }

    @Override
    public float getFloat(String key, float defValue)
    {
        synchronized (this)
        {
            getStringHandler().setKeyPrefix(FLOAT);

            if (isMemorySupport())
            {
                Float result = getMemory(key, getStringHandler());
                if (result != null) return result;
            }

            String content = getStringHandler().getObject(key, null);
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

    @Override
    public boolean hasDouble(String key)
    {
        synchronized (this)
        {
            getStringHandler().setKeyPrefix(DOUBLE);

            return getStringHandler().hasObject(key);
        }
    }

    @Override
    public boolean removeDouble(String key)
    {
        synchronized (this)
        {
            getStringHandler().setKeyPrefix(DOUBLE);

            removeMemory(key, getStringHandler());
            return getStringHandler().removeObject(key);
        }
    }

    @Override
    public boolean putDouble(String key, double value)
    {
        synchronized (this)
        {
            getStringHandler().setKeyPrefix(DOUBLE);

            boolean result = getStringHandler().putObject(key, String.valueOf(value));
            if (result)
            {
                putMemory(key, value, getStringHandler());
            }
            return result;
        }
    }

    @Override
    public double getDouble(String key, double defValue)
    {
        synchronized (this)
        {
            getStringHandler().setKeyPrefix(DOUBLE);

            if (isMemorySupport())
            {
                Double result = getMemory(key, getStringHandler());
                if (result != null) return result;
            }

            String content = getStringHandler().getObject(key, null);
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

    @Override
    public boolean hasBoolean(String key)
    {
        synchronized (this)
        {
            getStringHandler().setKeyPrefix(BOOLEAN);

            return getStringHandler().hasObject(key);
        }
    }

    @Override
    public boolean removeBoolean(String key)
    {
        synchronized (this)
        {
            getStringHandler().setKeyPrefix(BOOLEAN);

            removeMemory(key, getStringHandler());
            return getStringHandler().removeObject(key);
        }
    }

    @Override
    public boolean putBoolean(String key, boolean value)
    {
        synchronized (this)
        {
            getStringHandler().setKeyPrefix(BOOLEAN);

            boolean result = getStringHandler().putObject(key, String.valueOf(value));
            if (result)
            {
                putMemory(key, value, getStringHandler());
            }
            return result;
        }
    }

    @Override
    public boolean getBoolean(String key, boolean defValue)
    {
        synchronized (this)
        {
            getStringHandler().setKeyPrefix(BOOLEAN);

            if (isMemorySupport())
            {
                Boolean result = getMemory(key, getStringHandler());
                if (result != null) return result;
            }

            String content = getStringHandler().getObject(key, null);
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
            mStringHandler = new StringHandler(this);
        }
        return mStringHandler;
    }

    @Override
    public boolean hasString(String key)
    {
        synchronized (this)
        {
            getStringHandler().setKeyPrefix(STRING);

            return getStringHandler().hasObject(key);
        }
    }

    @Override
    public boolean removeString(String key)
    {
        synchronized (this)
        {
            getStringHandler().setKeyPrefix(STRING);

            removeMemory(key, getStringHandler());
            return getStringHandler().removeObject(key);
        }
    }

    @Override
    public boolean putString(String key, String value)
    {
        synchronized (this)
        {
            getStringHandler().setKeyPrefix(STRING);

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
        synchronized (this)
        {
            getStringHandler().setKeyPrefix(STRING);

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
            mObjectHandler = new ObjectHandler(this);
            mObjectHandler.setKeyPrefix(OBJECT);
        }
        return mObjectHandler;
    }

    @Override
    public boolean hasObject(Class clazz)
    {
        synchronized (this)
        {
            return getObjectHandler().hasObject(clazz.getName());
        }
    }

    @Override
    public boolean removeObject(Class clazz)
    {
        synchronized (this)
        {
            removeMemory(clazz.getName(), getObjectHandler());
            return getObjectHandler().removeObject(clazz.getName());
        }
    }

    @Override
    public boolean putObject(Object object)
    {
        synchronized (this)
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
        synchronized (this)
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
            mSerializableHandler = new SerializableHandler(this);
            mSerializableHandler.setKeyPrefix(SERIALIZABLE);
        }
        return mSerializableHandler;
    }

    @Override
    public boolean hasSerializable(Class clazz)
    {
        synchronized (this)
        {
            return getSerializableHandler().hasObject(clazz.getName());
        }
    }

    @Override
    public boolean removeSerializable(Class clazz)
    {
        synchronized (this)
        {
            removeMemory(clazz.getName(), getSerializableHandler());
            return getSerializableHandler().removeObject(clazz.getName());
        }
    }

    @Override
    public boolean putSerializable(Serializable object)
    {
        synchronized (this)
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
        synchronized (this)
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
