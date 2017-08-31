package com.fanwe.library.cache;

import java.io.Serializable;

/**
 * Created by zhengjun on 2017/9/1.
 */
public interface ISDDisk
{
    /**
     * 设置对象转换器
     *
     * @param objectConverter
     * @return
     */
    ISDDisk setObjectConverter(IObjectConverter objectConverter);

    /**
     * 设置加解密转换器
     *
     * @param encryptConverter
     * @return
     */
    ISDDisk setEncryptConverter(IEncryptConverter encryptConverter);

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

    boolean putString(String key, String data);

    boolean putString(String key, String data, boolean encrypt);

    String getString(String key);

    //---------- Serializable start ----------

    boolean hasSerializable(Class clazz);

    <T extends Serializable> boolean putSerializable(T object);

    <T extends Serializable> T getSerializable(Class<T> clazz);

    boolean removeSerializable(Class clazz);

    //---------- object start ----------

    boolean hasObject(Class clazz);

    boolean removeObject(Class clazz);

    boolean putObject(Object object);

    boolean putObject(Object object, boolean encrypt);

    <T> T getObject(Class<T> clazz);
}
