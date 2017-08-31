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
package com.fanwe.library.cache.handler;

import android.text.TextUtils;

import com.fanwe.library.cache.IEncryptConverter;

import java.io.File;

/**
 * Created by zhengjun on 2017/8/31.
 */
public abstract class ObjectHandler<T> implements IObjectHandler<T>
{
    public static final String TAG = "ObjectHandler";

    private File mDirectory;

    private boolean mEncrypt;
    private IEncryptConverter mEncryptConverter;

    public ObjectHandler(File directory)
    {
        mDirectory = directory;
    }

    @Override
    public void setEncrypt(boolean encrypt)
    {
        mEncrypt = encrypt;
    }

    @Override
    public void setEncryptConverter(IEncryptConverter encryptConverter)
    {
        mEncryptConverter = encryptConverter;
    }

    protected final boolean isEncrypt()
    {
        return mEncrypt;
    }

    protected final IEncryptConverter getEncryptConverter()
    {
        return mEncryptConverter;
    }

    private File getObjectFile(String key)
    {
        if (TextUtils.isEmpty(key))
        {
            throw new IllegalArgumentException("key is null or empty");
        }

        String realKey = getFileKey(key);
        File file = new File(mDirectory, realKey);
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
    public final T getObject(String key)
    {
        File file = getObjectFile(key);
        if (!file.exists())
        {
            return null;
        }

        T result = onGetObject(key, file);
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

    abstract public String getFileKey(String key);

    abstract protected boolean onPutObject(String key, T object, File file);

    abstract protected T onGetObject(String key, File file);
}
