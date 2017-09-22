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

import java.io.File;

/**
 * Created by zhengjun on 2017/9/1.
 */
class ObjectHandler extends AObjectHandler<Object>
{
    private StringHandler mStringHandler;

    public ObjectHandler(ISDDiskInfo diskInfo)
    {
        super(diskInfo);
    }

    private StringHandler getStringHandler()
    {
        if (mStringHandler == null)
        {
            mStringHandler = new StringHandler(getDiskInfo());
        }
        mStringHandler.setKeyPrefix(getKeyPrefix());
        return mStringHandler;
    }

    @Override
    protected boolean onPutObject(String key, Object object, File file)
    {
        checkObjectConverter();

        String content = getDiskInfo().getObjectConverter().objectToString(object);
        return getStringHandler().putObject(key, content);
    }

    @Override
    protected Object onGetObject(String key, Class clazz, File file)
    {
        checkObjectConverter();

        String content = getStringHandler().getObject(key, null);
        if (content != null)
        {
            return getDiskInfo().getObjectConverter().stringToObject(content, clazz);
        } else
        {
            return null;
        }
    }
}
