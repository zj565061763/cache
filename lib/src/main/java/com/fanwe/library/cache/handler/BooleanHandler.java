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

import java.io.File;

/**
 * Created by zhengjun on 2017/8/31.
 */
public class BooleanHandler extends ObjectHandler<Boolean>
{
    private static final String BOOLEAN = "boolean_";

    private StringHandler mStringHandler;

    public BooleanHandler(File directory)
    {
        super(directory);
        mStringHandler = new StringHandler(directory);
    }

    private StringHandler getStringHandler()
    {
        return mStringHandler;
    }

    @Override
    public String getFileKey(String key)
    {
        return getStringHandler().getFileKey(BOOLEAN + key);
    }

    @Override
    protected boolean onPutObject(String key, Boolean object, File file)
    {
        return getStringHandler().putObject(BOOLEAN + key, String.valueOf(object));
    }

    @Override
    protected Boolean onGetObject(String key, File file)
    {
        String content = getStringHandler().getObject(BOOLEAN + key);
        return Boolean.valueOf(content);
    }
}
