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
package com.fanwe.lib.cache.handler;

import com.fanwe.lib.cache.Disk;
import com.fanwe.lib.cache.DiskInfo;

import java.io.File;
import java.io.Serializable;

/**
 * 缓存可以和byte互相转换的处理类
 */
public abstract class ByteConverterHandler<T> extends BaseHandler<T>
{
    private SerializableHandler<CacheModel> mSerializableHandler;

    public ByteConverterHandler(DiskInfo diskInfo)
    {
        super(diskInfo);
    }

    private final SerializableHandler<CacheModel> getSerializableHandler()
    {
        if (mSerializableHandler == null)
        {
            mSerializableHandler = new SerializableHandler(getDiskInfo())
            {
                @Override
                protected String getKeyPrefix()
                {
                    return ByteConverterHandler.this.getKeyPrefix();
                }
            };
        }
        return mSerializableHandler;
    }

    @Override
    protected final boolean putCacheImpl(String key, T value, File file)
    {
        final boolean encrypt = getDiskInfo().isEncrypt();
        final Disk.EncryptConverter converter = getDiskInfo().getEncryptConverter();
        if (encrypt && converter == null)
            throw new RuntimeException("you must provide an EncryptConverter instance before this");

        final byte[] data = valueToByte(value);
        if (data == null)
            throw new RuntimeException("valueToByte(T) method return null");

        final CacheModel model = new CacheModel();
        model.data = encrypt ? converter.encrypt(data) : data;
        model.isEncrypted = encrypt;

        if (model.data == null)
            throw new RuntimeException("EncryptConverter.encrypt(byte[]) method return null");

        return getSerializableHandler().putCache(key, model);
    }

    @Override
    protected final T getCacheImpl(String key, Class<T> clazz, File file)
    {
        final CacheModel model = getSerializableHandler().getCache(key, CacheModel.class);
        if (model == null)
            return null;

        final boolean isEncrypted = model.isEncrypted;
        final Disk.EncryptConverter converter = getDiskInfo().getEncryptConverter();
        if (isEncrypted && converter == null)
        {
            final Disk.ExceptionHandler handler = getDiskInfo().getExceptionHandler();
            if (handler != null)
                handler.onException(new RuntimeException("content is encrypted but EncryptConverter not found when try decrypt"));
            return null;
        }

        if (isEncrypted)
            model.data = converter.decrypt(model.data);

        return byteToValue(model.data, clazz);
    }

    /**
     * 缓存转byte
     *
     * @param value
     * @return
     */
    protected abstract byte[] valueToByte(T value);

    /**
     * byte转缓存
     *
     * @param bytes
     * @param clazz
     * @return
     */
    protected abstract T byteToValue(byte[] bytes, Class<T> clazz);

    private static final class CacheModel implements Serializable
    {
        static final long serialVersionUID = 0L;

        public boolean isEncrypted = false;
        public byte[] data = null;
    }
}
