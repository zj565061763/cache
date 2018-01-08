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
package com.fanwe.lib.cache;

import android.text.TextUtils;

import java.io.File;

/**
 * Created by zhengjun on 2017/8/31.
 */
abstract class AObjectHandler<T> implements IObjectHandler<T>
{
    private FIDiskInfo mDiskInfo;
    private String mKeyPrefix;

    public AObjectHandler(FIDiskInfo diskInfo)
    {
        if (diskInfo == null)
        {
            throw new NullPointerException("diskInfo must not be null");
        }

        mDiskInfo = diskInfo;
    }

    @Override
    public void setKeyPrefix(String keyPrefix)
    {
        mKeyPrefix = keyPrefix;
    }

    protected final String getKeyPrefix()
    {
        if (mKeyPrefix == null)
        {
            mKeyPrefix = "";
        }
        return mKeyPrefix;
    }

    public final String getRealKey(String key)
    {
        return getKeyPrefix() + Utils.MD5(key);
    }

    protected final FIDiskInfo getDiskInfo()
    {
        return mDiskInfo;
    }

    protected final void checkEncryptConverter()
    {
        if (getDiskInfo().isEncrypt() && getDiskInfo().getEncryptConverter() == null)
        {
            throw new NullPointerException("you must provide an IEncryptConverter instance before this");
        }
    }

    protected final void checkObjectConverter()
    {
        if (getDiskInfo().getObjectConverter() == null)
        {
            throw new NullPointerException("you must provide an IObjectConverter instance before this");
        }
    }

    private void ensureDirectoryExists()
    {
        if (!getDiskInfo().getDirectory().exists())
        {
            getDiskInfo().getDirectory().mkdirs();
        }
    }

    private File getObjectFile(String key)
    {
        if (TextUtils.isEmpty(key))
        {
            throw new IllegalArgumentException("key is null or empty");
        }

        String realKey = getRealKey(key);
        ensureDirectoryExists();
        File file = new File(getDiskInfo().getDirectory(), realKey);
        return file;
    }

    @Override
    public final boolean hasObject(String key)
    {
        boolean result = getObjectFile(key).exists();
        return result;
    }

    @Override
    public final boolean putObject(String key, T object)
    {
        if (object == null)
        {
            removeObject(key);
            return true;
        }

        boolean result = onPutObject(key, object, getObjectFile(key));
        return result;
    }

    @Override
    public final T getObject(String key, Class clazz)
    {
        File file = getObjectFile(key);
        if (!file.exists())
        {
            return null;
        }

        T result = onGetObject(key, clazz, file);
        return result;
    }

    @Override
    public final boolean removeObject(String key)
    {
        File file = getObjectFile(key);
        if (file.exists())
        {
            return file.delete();
        } else
        {
            return true;
        }
    }

    abstract protected boolean onPutObject(String key, T object, File file);

    abstract protected T onGetObject(String key, Class clazz, File file);
}
