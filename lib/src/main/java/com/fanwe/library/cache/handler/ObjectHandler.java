package com.fanwe.library.cache.handler;

import java.io.File;

/**
 * Created by Administrator on 2017/8/31.
 */
public abstract class ObjectHandler<T> implements IObjectHandler<T>
{
    private File mDirectory;
    private boolean mEncrypt;

    public ObjectHandler(File directory)
    {
        mDirectory = directory;
    }

    @Override
    public final boolean isEncrypt()
    {
        return mEncrypt;
    }

    @Override
    public final void setEncrypt(boolean encrypt)
    {
        this.mEncrypt = encrypt;
    }

    private File getObjectFile(String key)
    {
        String realKey = getKeyPrefix() + key;
        return new File(mDirectory, realKey);
    }

    @Override
    public final boolean hasObject(String key)
    {
        return getObjectFile(key).exists();
    }

    @Override
    public final boolean putObject(String key, T object)
    {
        return onPutObject(getObjectFile(key), object);
    }

    @Override
    public final T getObject(String key)
    {
        return onGetObject(getObjectFile(key));
    }

    @Override
    public final boolean removeObject(String key)
    {
        File file = getObjectFile(key);
        if (file.exists())
        {
            return file.delete();
        } else
        {
            return true;
        }
    }

    abstract protected String getKeyPrefix();

    abstract protected boolean onPutObject(File file, T object);

    abstract protected T onGetObject(File file);
}
