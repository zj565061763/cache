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
public class FloatHandler extends ObjectHandler<Float>
{
    private static final String FLOAT = "float_";

    private StringHandler mStringHandler;

    public FloatHandler(File directory)
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
        return getStringHandler().getFileKey(FLOAT + key);
    }

    @Override
    protected boolean onPutObject(String key, Float object, File file)
    {
        return getStringHandler().putObject(FLOAT + key, String.valueOf(object));
    }

    @Override
    protected Float onGetObject(String key, File file)
    {
        String content = getStringHandler().getObject(FLOAT + key);
        return Float.valueOf(content);
    }
}
