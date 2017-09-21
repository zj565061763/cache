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

    public ObjectHandler(File directory, String keyPrefix)
    {
        super(directory, keyPrefix);
    }

    private StringHandler getStringHandler()
    {
        if (mStringHandler == null)
        {
            mStringHandler = new StringHandler(getDirectory(), getKeyPrefix());
        }
        return mStringHandler;
    }

    @Override
    public void setDiskConfig(ISDDiskConfig diskConfig)
    {
        super.setDiskConfig(diskConfig);
        getStringHandler().setDiskConfig(diskConfig);
    }

    @Override
    protected boolean onPutObject(String key, Object object, File file)
    {
        checkObjectConverter();

        String content = getDiskConfig().getObjectConverter().objectToString(object);
        return getStringHandler().putObject(object.getClass().getName(), content);
    }

    @Override
    protected Object onGetObject(String key, Class clazz, File file)
    {
        checkObjectConverter();

        String content = getStringHandler().getObject(clazz.getName(), null);
        if (content != null)
        {
            return getDiskConfig().getObjectConverter().stringToObject(content, clazz);
        } else
        {
            return null;
        }
    }
}
