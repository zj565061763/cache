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
package com.fanwe.lib.cache.core.handler;

import com.fanwe.lib.cache.IEncryptConverter;
import com.fanwe.lib.cache.core.IDiskInfo;

import java.io.File;
import java.io.Serializable;

/**
 * Created by zhengjun on 2017/8/31.
 */
abstract class AbstractStringHandler<T> extends CacheHandler<T>
{
    private SerializableHandler<CacheModel> mSerializableHandler;

    public AbstractStringHandler(IDiskInfo diskInfo)
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
                    return AbstractStringHandler.this.getKeyPrefix();
                }
            };
        }
        return mSerializableHandler;
    }

    @Override
    protected boolean putCacheImpl(String key, T value, File file)
    {
        checkEncryptConverter();

        final CacheModel model = new CacheModel();
        final String data = valueToString(value);

        model.data = data;
        model.encrypt = getDiskInfo().isEncrypt();
        model.encryptIfNeed(getDiskInfo().getEncryptConverter());

        return getSerializableHandler().putCache(key, model);
    }

    @Override
    protected T getCacheImpl(String key, Class clazz, File file)
    {
        final CacheModel model = getSerializableHandler().getCache(key, CacheModel.class);
        if (model == null)
        {
            return null;
        }

        model.decryptIfNeed(getDiskInfo().getEncryptConverter());
        final String string = model.data;

        return stringToValue(string);
    }

    protected abstract String valueToString(T value);

    protected abstract T stringToValue(String string);


    private static class CacheModel implements Serializable
    {
        static final long serialVersionUID = 0L;

        public String data;
        public boolean encrypt;

        /**
         * 加密
         *
         * @param converter
         */
        public void encryptIfNeed(IEncryptConverter converter)
        {
            if (encrypt && converter != null)
            {
                data = converter.encrypt(data);
            }
        }

        /**
         * 解密
         *
         * @param converter
         */
        public void decryptIfNeed(IEncryptConverter converter)
        {
            if (encrypt && converter != null)
            {
                data = converter.decrypt(data);
            }
        }
    }
}
