package com.sd.lib.cache;

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
     * @param support
     * @return
     */
    Disk setMemorySupport(boolean support);

    /**
     * 设置对象转换器
     *
     * @param converter
     * @return
     */
    Disk setObjectConverter(ObjectConverter converter);

    /**
     * 设置加解密转换器
     *
     * @param converter
     * @return
     */
    Disk setEncryptConverter(EncryptConverter converter);

    /**
     * 设置异常处理对象
     *
     * @param handler
     * @return
     */
    Disk setExceptionHandler(ExceptionHandler handler);

    /**
     * 检查目录是否可用
     *
     * @return
     */
    boolean checkDirectory();

    /**
     * 返回当前目录下所有缓存文件的总大小(字节B)
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

    CommonCache<byte[]> cacheBytes();

    ObjectCache cacheObject();

    //---------- cache end ----------

    /**
     * 通用缓存接口
     */
    interface CommonCache<T>
    {
        /**
         * 放入缓存对象
         *
         * @param key
         * @param value
         * @return
         */
        boolean put(String key, T value);

        /**
         * 返回key对应的缓存
         *
         * @param key
         * @return
         */
        T get(String key);

        /**
         * 返回key对应的缓存
         *
         * @param key
         * @param defaultValue 如果获取的缓存为null或者不存在，则返回这个值
         * @return
         */
        T get(String key, T defaultValue);
    }

    /**
     * 对象缓存接口
     */
    interface ObjectCache
    {
        boolean put(Object value);

        <T> T get(Class<T> clazz);

        boolean remove(Class clazz);
    }

    interface CacheStore
    {
        /**
         * 保存缓存
         *
         * @param key
         * @param value
         * @param info
         * @return true-保存成功，false-保存失败
         */
        boolean putCache(String key, byte[] value, DiskInfo info);

        /**
         * 获得缓存
         *
         * @param key
         * @param clazz
         * @param info
         * @return
         */
        byte[] getCache(String key, Class clazz, DiskInfo info);

        /**
         * 删除缓存
         *
         * @param key
         * @param info
         * @return true-此次方法调用后删除了缓存，false-删除失败或者缓存不存在
         */
        boolean removeCache(String key, DiskInfo info);
    }

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
