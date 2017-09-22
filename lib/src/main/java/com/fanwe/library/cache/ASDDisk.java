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

import android.content.Context;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

abstract class ASDDisk implements ISDDisk, ISDDiskInfo
{
    private File mDirectory;

    private static Context mContext;
    private static IEncryptConverter sGlobalEncryptConverter;
    private static IObjectConverter sGlobalObjectConverter;

    private boolean mEncrypt;
    private IEncryptConverter mEncryptConverter;
    private IObjectConverter mObjectConverter;

    private boolean mMemorySupport;
    private static final Map<String, Object> MAP_MEMORY = new ConcurrentHashMap<>();

    protected ASDDisk(File directory)
    {
        mDirectory = directory;
    }

    /**
     * 初始化
     *
     * @param context
     */
    public static void init(Context context)
    {
        mContext = context.getApplicationContext();
    }

    public static void setDebug(boolean debug)
    {
        LogUtils.setDebug(debug);
    }

    /**
     * 设置全局加解密转换器
     *
     * @param globalEncryptConverter
     */
    public static void setGlobalEncryptConverter(IEncryptConverter globalEncryptConverter)
    {
        sGlobalEncryptConverter = globalEncryptConverter;
    }

    /**
     * 设置全局对象转换器
     *
     * @param globalObjectConverter
     */
    public static void setGlobalObjectConverter(IObjectConverter globalObjectConverter)
    {
        sGlobalObjectConverter = globalObjectConverter;
    }

    @Override
    public ASDDisk setEncrypt(boolean encrypt)
    {
        mEncrypt = encrypt;
        return this;
    }

    @Override
    public ASDDisk setEncryptConverter(IEncryptConverter encryptConverter)
    {
        mEncryptConverter = encryptConverter;
        return this;
    }

    @Override
    public ASDDisk setObjectConverter(IObjectConverter objectConverter)
    {
        mObjectConverter = objectConverter;
        return this;
    }

    @Override
    public ASDDisk setMemorySupport(boolean memorySupport)
    {
        mMemorySupport = memorySupport;
        return this;
    }

    @Override
    public File getDirectory()
    {
        return mDirectory;
    }

    @Override
    public long size()
    {
        return mDirectory.length();
    }

    @Override
    public void delete()
    {
        synchronized (this)
        {
            Utils.deleteFileOrDir(mDirectory);
        }
    }

    @Override
    public final boolean isEncrypt()
    {
        return mEncrypt;
    }

    @Override
    public final IEncryptConverter getEncryptConverter()
    {
        if (mEncryptConverter != null)
        {
            return mEncryptConverter;
        } else
        {
            return sGlobalEncryptConverter;
        }
    }

    @Override
    public final IObjectConverter getObjectConverter()
    {
        if (mObjectConverter != null)
        {
            return mObjectConverter;
        } else
        {
            return sGlobalObjectConverter;
        }
    }

    public boolean isMemorySupport()
    {
        return mMemorySupport;
    }

    protected final void removeMemory(String key, AObjectHandler handler)
    {
        MAP_MEMORY.remove(handler.getRealKey(key));
    }

    protected final void putMemory(String key, Object object, AObjectHandler handler)
    {
        if (isMemorySupport())
        {
            MAP_MEMORY.put(handler.getRealKey(key), object);
        }
    }

    protected final <T> T getMemory(String key, AObjectHandler handler)
    {
        if (isMemorySupport())
        {
            return (T) MAP_MEMORY.get(handler.getRealKey(key));
        } else
        {
            return null;
        }
    }

    //---------- util method start ----------

    private static Context getContext()
    {
        return mContext;
    }

    private static void checkContext()
    {
        if (mContext == null)
        {
            throw new NullPointerException("you must invoke init() method before this");
        }
    }

    /**
     * 返回"Android/data/包名/files/dirName"目录
     *
     * @param dirName
     * @return
     */
    protected static File getFileDir(String dirName)
    {
        checkContext();
        File dir = getContext().getExternalFilesDir(dirName);
        return dir;
    }

    /**
     * 返回"Android/data/包名/cache/dirName"目录
     *
     * @param dirName
     * @return
     */
    protected static File getCacheDir(String dirName)
    {
        checkContext();
        File dir = new File(getContext().getExternalCacheDir(), dirName);
        return dir;
    }

    //---------- util method end ----------
}
