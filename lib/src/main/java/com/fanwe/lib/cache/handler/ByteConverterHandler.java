package com.fanwe.lib.cache.handler;

import com.fanwe.lib.cache.DiskInfo;
import com.fanwe.lib.cache.converter.EncryptConverter;

import java.io.File;
import java.io.Serializable;

/**
 * 缓存可以和二进制数据互相转换的处理类
 */
public abstract class ByteConverterHandler<T> extends BaseCacheHandler<T>
{
    private SerializableHandler<CacheModel> mSerializableHandler;

    public ByteConverterHandler(DiskInfo diskInfo)
    {
        super(diskInfo);
    }

    private final SerializableHandler<CacheModel> getSerializableHandler()
    {
        if (mSerializableHandler == null)
        {
            mSerializableHandler = new SerializableHandler(getDiskInfo())
            {
                @Override
                protected String getKeyPrefix()
                {
                    return ByteConverterHandler.this.getKeyPrefix();
                }
            };
        }
        return mSerializableHandler;
    }

    @Override
    protected final boolean putCacheImpl(String key, T value, File file)
    {
        final boolean encrypt = getDiskInfo().isEncrypt();
        final EncryptConverter converter = getDiskInfo().getEncryptConverter();
        if (encrypt && converter == null)
            throw new RuntimeException("you must provide an EncryptConverter instance before this");

        final byte[] data = valueToByte(value);
        if (data == null)
            throw new RuntimeException("valueToByte(T) method return null");

        final CacheModel model = new CacheModel();
        model.data = encrypt ? converter.encrypt(data) : data;
        model.isEncrypted = encrypt;

        if (model.data == null)
            throw new RuntimeException("EncryptConverter.encrypt(byte[]) method return null");

        return getSerializableHandler().putCache(key, model);
    }

    @Override
    protected final T getCacheImpl(String key, Class<T> clazz, File file)
    {
        final CacheModel model = getSerializableHandler().getCache(key, CacheModel.class);
        if (model == null)
            return null;

        final boolean isEncrypted = model.isEncrypted;
        final EncryptConverter converter = getDiskInfo().getEncryptConverter();
        if (isEncrypted && converter == null)
            throw new RuntimeException("content is encrypted but EncryptConverter not found when try decrypt");

        if (isEncrypted)
            model.data = converter.decrypt(model.data);

        return byteToValue(model.data, clazz);
    }

    /**
     * 把缓存转为二进制数据
     *
     * @param value
     * @return
     */
    protected abstract byte[] valueToByte(T value);

    /**
     * 把字符串转为缓存
     *
     * @param bytes
     * @param clazz
     * @return
     */
    protected abstract T byteToValue(byte[] bytes, Class<T> clazz);

    private static final class CacheModel implements Serializable
    {
        static final long serialVersionUID = 0L;

        public boolean isEncrypted = false;
        public byte[] data = null;
    }
}
