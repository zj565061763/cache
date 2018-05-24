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

import com.fanwe.lib.cache.api.CommonCache;
import com.fanwe.lib.cache.api.ObjectCache;
import com.fanwe.lib.cache.api.SerializableCache;

/**
 * Created by zhengjun on 2017/9/1.
 */
public interface Disk
{
    /**
     * 设置保存缓存的时候是否加密
     *
     * @param encrypt
     * @return
     */
    Disk setEncrypt(boolean encrypt);

    /**
     * 设置是否支持内存存储
     *
     * @param memorySupport
     * @return
     */
    Disk setMemorySupport(boolean memorySupport);

    /**
     * 设置加解密转换器
     *
     * @param encryptConverter
     * @return
     */
    Disk setEncryptConverter(EncryptConverter encryptConverter);

    /**
     * 设置对象转换器
     *
     * @param objectConverter
     * @return
     */
    Disk setObjectConverter(ObjectConverter objectConverter);

    /**
     * 设置异常处理对象
     *
     * @param exceptionHandler
     * @return
     */
    Disk setExceptionHandler(ExceptionHandler exceptionHandler);

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

    CommonCache<Integer> cacheInteger();

    CommonCache<Long> cacheLong();

    CommonCache<Float> cacheFloat();

    CommonCache<Double> cacheDouble();

    CommonCache<Boolean> cacheBoolean();

    CommonCache<String> cacheString();

    SerializableCache cacheSerializable();

    ObjectCache cacheObject();

    //---------- cache end ----------

    /**
     * 对象转换器
     */
    interface ObjectConverter
    {
        /**
         * 对象转byte
         *
         * @param object
         * @return
         */
        byte[] objectToByte(Object object);

        /**
         * byte转对象
         *
         * @param bytes
         * @param clazz
         * @param <T>
         * @return
         */
        <T> T byteToObject(byte[] bytes, Class<T> clazz);
    }

    /**
     * 加解密转换器
     */
    interface EncryptConverter
    {
        /**
         * 加密数据
         *
         * @param bytes
         * @return
         */
        byte[] encrypt(byte[] bytes);

        /**
         * 解密数据
         *
         * @param bytes
         * @return
         */
        byte[] decrypt(byte[] bytes);
    }

    /**
     * 异常处理类
     */
    interface ExceptionHandler
    {
        void onException(Exception e);
    }
}
