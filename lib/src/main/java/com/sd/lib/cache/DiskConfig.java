package com.sd.lib.cache;

import android.content.Context;

public class DiskConfig
{
    public final Context mContext;
    public final Disk.CacheStore mCacheStore;
    public final Disk.ObjectConverter mObjectConverter;
    public final Disk.EncryptConverter mEncryptConverter;
    public final Disk.ExceptionHandler mExceptionHandler;

    private DiskConfig(Builder builder)
    {
        mContext = builder.mContext;
        mCacheStore = builder.mCacheStore != null ? builder.mCacheStore : new FileCacheStore();
        mObjectConverter = builder.mObjectConverter;
        mEncryptConverter = builder.mEncryptConverter;
        mExceptionHandler = builder.mExceptionHandler != null ? builder.mExceptionHandler : new Disk.ExceptionHandler()
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
        private Disk.CacheStore mCacheStore;
        private Disk.ObjectConverter mObjectConverter;
        private Disk.EncryptConverter mEncryptConverter;
        private Disk.ExceptionHandler mExceptionHandler;

        /**
         * 设置缓存存取对象
         *
         * @param cacheStore
         * @return
         */
        public Builder setCacheStore(Disk.CacheStore cacheStore)
        {
            mCacheStore = cacheStore;
            return this;
        }

        /**
         * 设置对象转换器
         *
         * @param converter
         * @return
         */
        public Builder setObjectConverter(Disk.ObjectConverter converter)
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
        public Builder setEncryptConverter(Disk.EncryptConverter converter)
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
        public Builder setExceptionHandler(Disk.ExceptionHandler handler)
        {
            mExceptionHandler = handler;
            return this;
        }

        public DiskConfig build(Context context)
        {
            mContext = context.getApplicationContext();
            return new DiskConfig(this);
        }
    }
}
