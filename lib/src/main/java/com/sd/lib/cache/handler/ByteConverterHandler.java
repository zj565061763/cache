package com.sd.lib.cache.handler;

import com.sd.lib.cache.Disk;
import com.sd.lib.cache.DiskInfo;

import java.io.File;
import java.io.Serializable;

/**
 * 缓存可以和byte互相转换的处理类
 */
public abstract class ByteConverterHandler<T> extends BaseHandler<T>
{
    private SerializableHandler mSerializableHandler;

    public ByteConverterHandler(DiskInfo diskInfo)
    {
        super(diskInfo);
    }

    private final SerializableHandler getSerializableHandler()
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
        final Disk.EncryptConverter converter = getDiskInfo().getEncryptConverter();
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
    protected final T getCacheImpl(String key, Class clazz, File file)
    {
        final CacheModel model = (CacheModel) getSerializableHandler().getCache(key, CacheModel.class);
        if (model == null)
            return null;

        final boolean isEncrypted = model.isEncrypted;
        final Disk.EncryptConverter converter = getDiskInfo().getEncryptConverter();
        if (isEncrypted && converter == null)
        {
            getDiskInfo().getExceptionHandler().onException(new RuntimeException("content is encrypted but EncryptConverter not found when try decrypt"));
            return null;
        }

        if (isEncrypted)
            model.data = converter.decrypt(model.data);

        if (model.data == null)
            return null;

        return byteToValue(model.data, clazz);
    }

    /**
     * 缓存转byte
     *
     * @param value
     * @return
     */
    protected abstract byte[] valueToByte(T value);

    /**
     * byte转缓存
     *
     * @param bytes
     * @param clazz
     * @return
     */
    protected abstract T byteToValue(byte[] bytes, Class clazz);

    private static final class CacheModel implements Serializable
    {
        static final long serialVersionUID = 0L;

        public boolean isEncrypted = false;
        public byte[] data = null;
    }
}
