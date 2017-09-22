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
import java.io.Serializable;

/**
 * Created by zhengjun on 2017/9/1.
 */
interface ISDDisk
{
    /**
     * 设置是否加密
     *
     * @param encrypt
     * @return
     */
    ISDDisk setEncrypt(boolean encrypt);

    /**
     * 设置加解密转换器
     *
     * @param encryptConverter
     * @return
     */
    ISDDisk setEncryptConverter(IEncryptConverter encryptConverter);

    /**
     * 设置对象转换器
     *
     * @param objectConverter
     * @return
     */
    ISDDisk setObjectConverter(IObjectConverter objectConverter);

    /**
     * 设置是否支持内存存储
     *
     * @param memorySupport
     * @return
     */
    ISDDisk setMemorySupport(boolean memorySupport);

    /**
     * 返回当前对象关联的目录
     *
     * @return
     */
    File getDirectory();

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

    //---------- int start ----------

    boolean hasInt(String key);

    boolean removeInt(String key);

    boolean putInt(String key, int value);

    int getInt(String key, int defValue);

    //---------- long start ----------

    boolean hasLong(String key);

    boolean removeLong(String key);

    boolean putLong(String key, long value);

    long getLong(String key, long defValue);

    //---------- float start ----------

    boolean hasFloat(String key);

    boolean removeFloat(String key);

    boolean putFloat(String key, float value);

    float getFloat(String key, float defValue);

    //---------- double start ----------

    boolean hasDouble(String key);

    boolean removeDouble(String key);

    boolean putDouble(String key, double value);

    double getDouble(String key, double defValue);

    //---------- boolean start ----------

    boolean hasBoolean(String key);

    boolean removeBoolean(String key);

    boolean putBoolean(String key, boolean value);

    boolean getBoolean(String key, boolean defValue);

    //---------- string start ----------

    boolean hasString(String key);

    boolean removeString(String key);

    boolean putString(String key, String value);

    String getString(String key);

    //---------- object start ----------

    boolean hasObject(Class clazz);

    boolean removeObject(Class clazz);

    boolean putObject(Object object);

    <T> T getObject(Class<T> clazz);

    //---------- Serializable start ----------

    boolean hasSerializable(Class clazz);

    boolean removeSerializable(Class clazz);

    boolean putSerializable(Serializable object);

    <T extends Serializable> T getSerializable(Class<T> clazz);
}
