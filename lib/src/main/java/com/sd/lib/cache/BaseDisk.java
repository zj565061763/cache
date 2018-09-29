package com.sd.lib.cache;

import android.content.Context;

import java.io.File;

abstract class BaseDisk implements Disk, DiskInfo
{
    private final File mDirectory;

    private static Context mContext;
    private static EncryptConverter sGlobalEncryptConverter;
    private static ObjectConverter sGlobalObjectConverter;

    private boolean mEncrypt;
    private EncryptConverter mEncryptConverter;
    private ObjectConverter mObjectConverter;
    private ExceptionHandler mExceptionHandler;

    private boolean mMemorySupport;

    protected BaseDisk(File directory)
    {
        if (directory == null)
            throw new NullPointerException("directory must not be null");

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
    public Disk setEncrypt(boolean encrypt)
    {
        mEncrypt = encrypt;
        return this;
    }

    @Override
    public Disk setMemorySupport(boolean memorySupport)
    {
        mMemorySupport = memorySupport;
        return this;
    }

    @Override
    public Disk setEncryptConverter(EncryptConverter encryptConverter)
    {
        mEncryptConverter = encryptConverter;
        return this;
    }

    @Override
    public Disk setObjectConverter(ObjectConverter objectConverter)
    {
        mObjectConverter = objectConverter;
        return this;
    }

    @Override
    public Disk setExceptionHandler(ExceptionHandler exceptionHandler)
    {
        mExceptionHandler = exceptionHandler;
        return this;
    }

    @Override
    public final long size()
    {
        synchronized (Disk.class)
        {
            return getFileOrDirSize(mDirectory);
        }
    }

    @Override
    public final void delete()
    {
        synchronized (Disk.class)
        {
            deleteFileOrDir(mDirectory);
        }
    }

    //---------- Disk end ----------

    //---------- DiskInfo start ----------

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
            synchronized (Disk.class)
            {
                if (!mDirectory.exists() && !mDirectory.mkdirs())
                    throw new RuntimeException("create directory failure, check the directory:" + mDirectory.getAbsolutePath());
            }
        }
        return mDirectory;
    }

    @Override
    public final EncryptConverter getEncryptConverter()
    {
        return mEncryptConverter != null ? mEncryptConverter : sGlobalEncryptConverter;
    }

    @Override
    public final ObjectConverter getObjectConverter()
    {
        return mObjectConverter != null ? mObjectConverter : sGlobalObjectConverter;
    }

    @Override
    public final ExceptionHandler getExceptionHandler()
    {
        return mExceptionHandler;
    }

    //---------- DiskInfo end ----------

    //---------- util method start ----------

    private static Context getContext()
    {
        if (mContext == null)
            throw new NullPointerException("you must invoke FDisk.init(Context) method before this");
        return mContext;
    }

    /**
     * 返回外部存储"Android/data/包名/files/dirName"目录
     *
     * @param dirName
     * @return
     */
    protected static final File getExternalFilesDir(String dirName)
    {
        return getContext().getExternalFilesDir(dirName);
    }

    /**
     * 返回内部存储"/data/包名/files/dirName"目录
     *
     * @param dirName
     * @return
     */
    protected static final File getInternalFilesDir(String dirName)
    {
        return new File(getContext().getFilesDir(), dirName);
    }

    private static boolean deleteFileOrDir(File path)
    {
        if (path == null || !path.exists())
            return true;

        if (path.isFile())
            return path.delete();

        final File[] files = path.listFiles();
        if (files != null)
        {
            for (File file : files)
            {
                deleteFileOrDir(file);
            }
        }
        return path.delete();
    }

    private static long getFileOrDirSize(File file)
    {
        if (file == null || !file.exists())
            return 0;

        if (!file.isDirectory())
            return file.length();

        final File[] files = file.listFiles();
        if (files == null || files.length <= 0)
            return 0;

        long length = 0;
        for (File item : files)
        {
            length += getFileOrDirSize(item);
        }
        return length;
    }

    //---------- util method end ----------
}
