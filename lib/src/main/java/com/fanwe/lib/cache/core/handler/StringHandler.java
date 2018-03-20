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

import com.fanwe.lib.cache.core.IDiskInfo;

import java.io.File;

/**
 * Created by zhengjun on 2017/8/31.
 */
public class StringHandler extends CacheHandler<String>
{
    private SerializableHandler<CacheModel> mSerializableHandler;

    public StringHandler(IDiskInfo diskInfo)
    {
        super(diskInfo);
    }

    private SerializableHandler<CacheModel> getSerializableHandler()
    {
        if (mSerializableHandler == null)
        {
            mSerializableHandler = new SerializableHandler(getDiskInfo())
            {
                @Override
                protected String getKeyPrefix()
                {
                    return StringHandler.this.getKeyPrefix();
                }
            };
        }
        return mSerializableHandler;
    }

    @Override
    protected String getKeyPrefix()
    {
        return "string_";
    }

    @Override
    protected boolean putCacheImpl(String key, String value, File file)
    {
        checkEncryptConverter();

        final CacheModel model = new CacheModel();
        model.setData(value);
        model.setEncrypt(getDiskInfo().isEncrypt());
        model.encryptIfNeed(getDiskInfo().getEncryptConverter());

        return getSerializableHandler().putCache(key, model);
    }

    @Override
    protected String getCacheImpl(String key, Class clazz, File file)
    {
        final CacheModel model = getSerializableHandler().getCache(key, CacheModel.class);
        if (model == null)
        {
            return null;
        }

        model.decryptIfNeed(getDiskInfo().getEncryptConverter());
        return model.getData();
    }
}
