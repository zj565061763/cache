package com.fanwe.lib.cache;
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

import com.fanwe.lib.cache.api.SimpleSerializableCache;
import com.fanwe.lib.cache.handler.BooleanHandler;
import com.fanwe.lib.cache.handler.DoubleHandler;
import com.fanwe.lib.cache.handler.FloatHandler;
import com.fanwe.lib.cache.handler.IntegerHandler;
import com.fanwe.lib.cache.handler.LongHandler;
import com.fanwe.lib.cache.handler.ObjectHandler;
import com.fanwe.lib.cache.handler.StringHandler;

import java.io.File;

public class FDisk extends BaseDisk
{
    private static final String DEFAULT_FILE_DIR = "disk_file";
    private static final String DEFAULT_CACHE_DIR = "disk_cache";

    protected FDisk(File directory)
    {
        super(directory);
    }

    //---------- open start ----------

    /**
     * 外部存储"Android/data/包名/files/disk_file"目录
     *
     * @return
     */
    public static Disk open()
    {
        return openDir(getExternalFilesDir(DEFAULT_FILE_DIR));
    }

    /**
     * 外部存储"Android/data/包名/files/dirName"目录
     *
     * @param dirName
     * @return
     */
    public static Disk open(String dirName)
    {
        return openDir(getExternalFilesDir(dirName));
    }

    /**
     * 外部存储"Android/data/包名/cache/disk_cache"目录
     *
     * @return
     */
    public static Disk openCache()
    {
        return openDir(getExternalCacheDir(DEFAULT_CACHE_DIR));
    }

    /**
     * 外部存储"Android/data/包名/cache/dirName"目录
     *
     * @param dirName
     * @return
     */
    public static Disk openCache(String dirName)
    {
        return openDir(getExternalCacheDir(dirName));
    }

    /**
     * 内部存储"/data/包名/files/disk_file"目录
     *
     * @return
     */
    public static Disk openInternal()
    {
        return openDir(getInternalFilesDir(DEFAULT_FILE_DIR));
    }

    /**
     * 内部存储"/data/包名/files/dirName"目录
     *
     * @param dirName
     * @return
     */
    public static Disk openInternal(String dirName)
    {
        return openDir(getInternalFilesDir(dirName));
    }

    /**
     * 内部存储"/data/包名/cache/disk_cache"目录
     *
     * @return
     */
    public static Disk openInternalCache()
    {
        return openDir(getInternalCacheDir(DEFAULT_CACHE_DIR));
    }

    /**
     * 内部存储"/data/包名/cache/dirName"目录
     *
     * @param dirName
     * @return
     */
    public static Disk openInternalCache(String dirName)
    {
        return openDir(getInternalCacheDir(dirName));
    }

    /**
     * 打开指定的目录
     *
     * @param directory
     * @return
     */
    public static Disk openDir(File directory)
    {
        return new FDisk(directory);
    }

    //---------- open end ----------

    //---------- cache start ----------

    private IntegerHandler mIntegerHandler;
    private LongHandler mLongHandler;
    private FloatHandler mFloatHandler;
    private DoubleHandler mDoubleHandler;
    private BooleanHandler mBooleanHandler;
    private StringHandler mStringHandler;

    private SerializableCache mSerializableCache;
    private ObjectHandler mObjectHandler;

    @Override
    public CommonCache<Integer> cacheInteger()
    {
        if (mIntegerHandler == null)
            mIntegerHandler = new IntegerHandler(this);
        return mIntegerHandler;
    }

    @Override
    public CommonCache<Long> cacheLong()
    {
        if (mLongHandler == null)
            mLongHandler = new LongHandler(this);
        return mLongHandler;
    }

    @Override
    public CommonCache<Float> cacheFloat()
    {
        if (mFloatHandler == null)
            mFloatHandler = new FloatHandler(this);
        return mFloatHandler;
    }

    @Override
    public CommonCache<Double> cacheDouble()
    {
        if (mDoubleHandler == null)
            mDoubleHandler = new DoubleHandler(this);
        return mDoubleHandler;
    }

    @Override
    public CommonCache<Boolean> cacheBoolean()
    {
        if (mBooleanHandler == null)
            mBooleanHandler = new BooleanHandler(this);
        return mBooleanHandler;
    }

    @Override
    public CommonCache<String> cacheString()
    {
        if (mStringHandler == null)
            mStringHandler = new StringHandler(this);
        return mStringHandler;
    }

    @Override
    public SerializableCache cacheSerializable()
    {
        if (mSerializableCache == null)
            mSerializableCache = new SimpleSerializableCache(this);
        return mSerializableCache;
    }

    @Override
    public ObjectCache cacheObject()
    {
        if (mObjectHandler == null)
            mObjectHandler = new ObjectHandler(this);
        return mObjectHandler;
    }

    //---------- cache end ----------
}
