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
package com.fanwe.lib.cache;

import com.fanwe.lib.cache.api.ICommonCache;
import com.fanwe.lib.cache.api.IObjectCache;
import com.fanwe.lib.cache.converter.IEncryptConverter;
import com.fanwe.lib.cache.converter.IObjectConverter;

import java.io.Serializable;

/**
 * Created by zhengjun on 2017/9/1.
 */
public interface IDisk
{
    /**
     * 设置是否加解密
     *
     * @param encrypt
     * @return
     */
    IDisk setEncrypt(boolean encrypt);

    /**
     * 设置是否支持内存存储
     *
     * @param memorySupport
     * @return
     */
    IDisk setMemorySupport(boolean memorySupport);

    /**
     * 设置加解密转换器
     *
     * @param encryptConverter
     * @return
     */
    IDisk setEncryptConverter(IEncryptConverter encryptConverter);

    /**
     * 设置对象转换器
     *
     * @param objectConverter
     * @return
     */
    IDisk setObjectConverter(IObjectConverter objectConverter);

    /**
     * 返回当前目录的大小
     *
     * @return
     */
    long size();

    /**
     * 删除该目录以及目录下的所有缓存
     */
    void delete();

    //---------- cache start ----------

    ICommonCache<Integer> cacheInteger();

    ICommonCache<Long> cacheLong();

    ICommonCache<Float> cacheFloat();

    ICommonCache<Double> cacheDouble();

    ICommonCache<Boolean> cacheBoolean();

    ICommonCache<String> cacheString();

    <T extends Serializable> IObjectCache<T> cacheSerializable(Class<T> clazz);

    <T> IObjectCache<T> cacheObject(Class<T> clazz);

    //---------- cache end ----------
}
