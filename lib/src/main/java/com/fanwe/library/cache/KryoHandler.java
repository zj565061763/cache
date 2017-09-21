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

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Created by zhengjun on 2017/8/31.
 */
class KryoHandler extends AObjectHandler<Object>
{
    private static final ThreadLocal<Kryo> KRYO_THREAD_LOCAL = new ThreadLocal<Kryo>()
    {
        protected Kryo initialValue()
        {
            return new Kryo();
        }
    };

    public KryoHandler(File directory)
    {
        super(directory);
    }

    private Kryo getKryo()
    {
        return KRYO_THREAD_LOCAL.get();
    }

    @Override
    protected boolean onPutObject(String key, Object object, File file)
    {
        Output output = null;
        try
        {
            output = new Output(new FileOutputStream(file));
            getKryo().writeObject(output, object);

            LogUtils.i("put " + getKeyPrefix() + key + ":" + object);
            return true;
        } catch (Exception e)
        {
            LogUtils.e("putKryo:" + e);
        } finally
        {
            Utils.closeQuietly(output);
        }
        return false;
    }

    @Override
    protected Object onGetObject(String key, Class clazz, File file)
    {
        Input input = null;
        try
        {
            input = new Input(new FileInputStream(file));
            Object object = getKryo().readObject(input, clazz);

            LogUtils.i("get " + getKeyPrefix() + key + ":" + object);
            return object;
        } catch (Exception e)
        {
            LogUtils.e("getKryo:" + e);
        } finally
        {
            Utils.closeQuietly(input);
        }
        return null;
    }
}
