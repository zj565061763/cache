package com.fanwe.lib.cache;

import android.content.Context;

import com.fanwe.lib.cache.converter.EncryptConverter;
import com.fanwe.lib.cache.converter.ObjectConverter;

import java.io.File;

abstract class BaseDisk<T extends BaseDisk> implements Disk<T>, DiskInfo
{
    private File mDirectory;

    private static Context mContext;
    private static EncryptConverter sGlobalEncryptConverter;
    private static ObjectConverter sGlobalObjectConverter;

    private boolean mEncrypt;
    private EncryptConverter mEncryptConverter;
    private ObjectConverter mObjectConverter;

    private boolean mMemorySupport;

    protected BaseDisk(File directory)
    {
        mDirectory = directory;
    }

    /**
     * 初始化
     *
     * @param context
     */
    public static void init(Context context)
    {
        mContext = context.getApplicationContext();
    }

    /**
     * 设置全局加解密转换器
     *
     * @param globalEncryptConverter
     */
    public static void setGlobalEncryptConverter(EncryptConverter globalEncryptConverter)
    {
        sGlobalEncryptConverter = globalEncryptConverter;
    }

    /**
     * 设置全局对象转换器
     *
     * @param globalObjectConverter
     */
    public static void setGlobalObjectConverter(ObjectConverter globalObjectConverter)
    {
        sGlobalObjectConverter = globalObjectConverter;
    }

    //---------- Disk start ----------

    @Override
    public T setEncrypt(boolean encrypt)
    {
        mEncrypt = encrypt;
        return (T) this;
    }

    @Override
    public T setMemorySupport(boolean memorySupport)
    {
        mMemorySupport = memorySupport;
        return (T) this;
    }

    @Override
    public T setEncryptConverter(EncryptConverter encryptConverter)
    {
        mEncryptConverter = encryptConverter;
        return (T) this;
    }

    @Override
    public T setObjectConverter(ObjectConverter objectConverter)
    {
        mObjectConverter = objectConverter;
        return (T) this;
    }

    @Override
    public final long size()
    {
        return mDirectory.length();
    }

    @Override
    public final void delete()
    {
        synchronized (FDisk.class)
        {
            deleteFileOrDir(mDirectory);
        }
    }

    //---------- Disk end ----------

    //---------- IDiskInfo start ----------

    @Override
    public final boolean isEncrypt()
    {
        return mEncrypt;
    }

    @Override
    public final boolean isMemorySupport()
    {
        return mMemorySupport;
    }

    @Override
    public final File getDirectory()
    {
        if (!mDirectory.exists())
        {
            mDirectory.mkdirs();
        }
        return mDirectory;
    }

    @Override
    public final EncryptConverter getEncryptConverter()
    {
        if (mEncryptConverter != null)
        {
            return mEncryptConverter;
        } else
        {
            return sGlobalEncryptConverter;
        }
    }

    @Override
    public final ObjectConverter getObjectConverter()
    {
        if (mObjectConverter != null)
        {
            return mObjectConverter;
        } else
        {
            return sGlobalObjectConverter;
        }
    }

    //---------- IDiskInfo end ----------

    //---------- util method start ----------

    private static Context getContext()
    {
        return mContext;
    }

    private static void checkContext()
    {
        if (mContext == null)
        {
            throw new NullPointerException("you must invoke FDisk.init(Context) method before this");
        }
    }

    /**
     * 返回外部存储"Android/data/包名/files/dirName"目录
     *
     * @param dirName
     * @return
     */
    protected static final File getExternalFilesDir(String dirName)
    {
        checkContext();
        File dir = getContext().getExternalFilesDir(dirName);
        return dir;
    }

    /**
     * 返回外部存储"Android/data/包名/cache/dirName"目录
     *
     * @param dirName
     * @return
     */
    protected static final File getExternalCacheDir(String dirName)
    {
        checkContext();
        File dir = new File(getContext().getExternalCacheDir(), dirName);
        return dir;
    }

    /**
     * 返回内部存储"/data/包名/files/dirName"目录
     *
     * @param dirName
     * @return
     */
    protected static final File getInternalFilesDir(String dirName)
    {
        checkContext();
        File dir = new File(getContext().getFilesDir(), dirName);
        return dir;
    }

    /**
     * 返回内部存储"/data/包名/cache/dirName"目录
     *
     * @param dirName
     * @return
     */
    protected static final File getInternalCacheDir(String dirName)
    {
        checkContext();
        File dir = new File(getContext().getCacheDir(), dirName);
        return dir;
    }

    private static boolean deleteFileOrDir(File path)
    {
        if (path == null || !path.exists())
        {
            return true;
        }
        if (path.isFile())
        {
            return path.delete();
        }
        File[] files = path.listFiles();
        if (files != null)
        {
            for (File file : files)
            {
                deleteFileOrDir(file);
            }
        }
        return path.delete();
    }

    //---------- util method end ----------
}
