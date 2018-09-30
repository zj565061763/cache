package com.sd.lib.cache;

import android.content.Context;

public class CacheConfig
{
    public final Context mContext;
    public final Cache.ObjectConverter mObjectConverter;
    public final Cache.EncryptConverter mEncryptConverter;
    public final Cache.ExceptionHandler mExceptionHandler;

    private CacheConfig(Builder builder)
    {
        mContext = builder.mContext;
        mObjectConverter = builder.mObjectConverter;
        mEncryptConverter = builder.mEncryptConverter;
        mExceptionHandler = builder.mExceptionHandler != null ? builder.mExceptionHandler : new Cache.ExceptionHandler()
        {
            @Override
            public void onException(Exception e)
            {
            }
        };
    }

    public static final class Builder
    {
        private Context mContext;
        private Cache.ObjectConverter mObjectConverter;
        private Cache.EncryptConverter mEncryptConverter;
        private Cache.ExceptionHandler mExceptionHandler;

        /**
         * 设置对象转换器
         *
         * @param converter
         * @return
         */
        public Builder setObjectConverter(Cache.ObjectConverter converter)
        {
            mObjectConverter = converter;
            return this;
        }

        /**
         * 设置加解密转换器
         *
         * @param converter
         * @return
         */
        public Builder setEncryptConverter(Cache.EncryptConverter converter)
        {
            mEncryptConverter = converter;
            return this;
        }

        /**
         * 设置异常处理对象
         *
         * @param handler
         * @return
         */
        public Builder setExceptionHandler(Cache.ExceptionHandler handler)
        {
            mExceptionHandler = handler;
            return this;
        }

        public CacheConfig build(Context context)
        {
            mContext = context.getApplicationContext();
            return new CacheConfig(this);
        }
    }
}
