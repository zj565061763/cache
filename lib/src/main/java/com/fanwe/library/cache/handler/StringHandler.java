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

import com.fanwe.library.cache.DataModel;

import java.io.File;

/**
 * Created by zhengjun on 2017/8/31.
 */
public class StringHandler extends AObjectHandler<String>
{
    public static final String STRING = "string_";

    private String mKeyPrefix = STRING;
    private SerializableHandler mSerializableHandler;

    public StringHandler(File directory, String keyPrefix)
    {
        super(directory);

        if (!TextUtils.isEmpty(keyPrefix))
        {
            mKeyPrefix = keyPrefix;
        }
        mSerializableHandler = new SerializableHandler(directory);
    }

    private SerializableHandler getSerializableHandler()
    {
        return mSerializableHandler;
    }

    @Override
    public String getFileKey(String key)
    {
        return getSerializableHandler().getFileKey(mKeyPrefix + key);
    }

    @Override
    protected boolean onPutObject(String key, String object, File file)
    {
        DataModel model = new DataModel();
        model.setData(object);
        model.setEncrypt(isEncrypt());
        model.encryptIfNeed(getEncryptConverter());

        return getSerializableHandler().putObject(mKeyPrefix + key, model);
    }

    @Override
    protected String onGetObject(String key, File file)
    {
        DataModel model = (DataModel) getSerializableHandler().getObject(mKeyPrefix + key);
        if (model != null)
        {
            model.decryptIfNeed(getEncryptConverter());
            return model.getData();
        } else
        {
            return null;
        }
    }
}
