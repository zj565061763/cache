package com.fanwe.lib.cache.core.handler;

import com.fanwe.lib.cache.IEncryptConverter;
import com.fanwe.lib.cache.core.IDiskInfo;

import java.io.File;
import java.io.Serializable;

/**
 * 值可以和字符串互相转换的处理类
 */
abstract class AbstractStringHandler<T> extends CacheHandler<T>
{
    private SerializableHandler<CacheModel> mSerializableHandler;

    public AbstractStringHandler(IDiskInfo diskInfo)
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
                    return AbstractStringHandler.this.getKeyPrefix();
                }
            };
        }
        return mSerializableHandler;
    }

    @Override
    protected boolean putCacheImpl(String key, T value, File file)
    {
        checkEncryptConverter();

        final CacheModel model = new CacheModel();
        final String data = valueToString(value);

        model.data = data;
        model.encrypt = getDiskInfo().isEncrypt();
        model.encryptIfNeed(getDiskInfo().getEncryptConverter());

        return getSerializableHandler().putCache(key, model);
    }

    @Override
    protected T getCacheImpl(String key, Class<T> clazz, File file)
    {
        final CacheModel model = getSerializableHandler().getCache(key, CacheModel.class);
        if (model == null)
        {
            return null;
        }

        model.decryptIfNeed(getDiskInfo().getEncryptConverter());
        final String string = model.data;

        return stringToValue(string, clazz);
    }

    protected abstract String valueToString(T value);

    protected abstract T stringToValue(String string, Class<T> clazz);


    private static class CacheModel implements Serializable
    {
        static final long serialVersionUID = 0L;

        public String data;
        public boolean encrypt;

        /**
         * 加密
         *
         * @param converter
         */
        public void encryptIfNeed(IEncryptConverter converter)
        {
            if (encrypt && converter != null)
            {
                data = converter.encrypt(data);
            }
        }

        /**
         * 解密
         *
         * @param converter
         */
        public void decryptIfNeed(IEncryptConverter converter)
        {
            if (encrypt && converter != null)
            {
                data = converter.decrypt(data);
            }
        }
    }
}
