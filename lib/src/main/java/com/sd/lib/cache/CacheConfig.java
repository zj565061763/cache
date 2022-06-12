package com.sd.lib.cache;

import android.content.Context;

import com.sd.lib.cache.store.InternalDiskCacheStore;

public class CacheConfig {
    private static CacheConfig sConfig;

    public final Context mContext;
    public final Cache.ObjectConverter mObjectConverter;
    public final Cache.EncryptConverter mEncryptConverter;
    public final Cache.ExceptionHandler mExceptionHandler;

    public final Cache.CacheStore mDiskCacheStore;

    private CacheConfig(Builder builder) {
        mContext = builder.mContext;
        mObjectConverter = builder.mObjectConverter;
        mEncryptConverter = builder.mEncryptConverter;
        mExceptionHandler = builder.mExceptionHandler != null ? builder.mExceptionHandler : new Cache.ExceptionHandler() {
            @Override
            public void onException(Exception e) {
            }
        };

        mDiskCacheStore = builder.mDiskCacheStore != null ? builder.mDiskCacheStore : new InternalDiskCacheStore(mContext);
    }

    /**
     * 初始化
     *
     * @param config
     */
    public static synchronized void init(CacheConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("config is null");
        }

        if (sConfig == null) {
            sConfig = config;
        }
    }

    /**
     * 返回配置
     *
     * @return
     */
    public static CacheConfig get() {
        if (sConfig == null) {
            throw new RuntimeException(CacheConfig.class.getSimpleName() + "has not been init");
        }
        return sConfig;
    }

    public static final class Builder {
        private Context mContext;
        private Cache.ObjectConverter mObjectConverter;
        private Cache.EncryptConverter mEncryptConverter;
        private Cache.ExceptionHandler mExceptionHandler;

        private Cache.CacheStore mDiskCacheStore;

        /**
         * 设置对象转换器
         *
         * @param converter
         * @return
         */
        public Builder setObjectConverter(Cache.ObjectConverter converter) {
            mObjectConverter = converter;
            return this;
        }

        /**
         * 设置加解密转换器
         *
         * @param converter
         * @return
         */
        public Builder setEncryptConverter(Cache.EncryptConverter converter) {
            mEncryptConverter = converter;
            return this;
        }

        /**
         * 设置异常处理对象
         *
         * @param handler
         * @return
         */
        public Builder setExceptionHandler(Cache.ExceptionHandler handler) {
            mExceptionHandler = handler;
            return this;
        }

        /**
         * 设置本地磁盘缓存
         *
         * @param store
         * @return
         */
        public Builder setDiskCacheStore(Cache.CacheStore store) {
            mDiskCacheStore = store;
            return this;
        }

        public CacheConfig build(Context context) {
            mContext = context.getApplicationContext();
            return new CacheConfig(this);
        }
    }
}
