package com.sd.lib.cache;

import java.io.File;

abstract class BaseDisk implements Disk, DiskInfo
{
    private static DiskConfig sDiskConfig;

    private final File mDirectory;

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
     * @param config
     */
    public static void init(DiskConfig config)
    {
        synchronized (Disk.class)
        {
            if (config == null)
                throw new NullPointerException();

            if (sDiskConfig == null)
                sDiskConfig = config;
            else
                throw new RuntimeException("init method can only be called once");
        }
    }

    /**
     * 返回config
     *
     * @return
     */
    private static DiskConfig getDiskConfig()
    {
        if (sDiskConfig == null)
            throw new NullPointerException("you must invoke FDisk.init(DiskConfig config) before this");
        return sDiskConfig;
    }

    //---------- Disk start ----------

    @Override
    public Disk setEncrypt(boolean encrypt)
    {
        mEncrypt = encrypt;
        return this;
    }

    @Override
    public Disk setMemorySupport(boolean support)
    {
        mMemorySupport = support;
        return this;
    }

    @Override
    public Disk setEncryptConverter(EncryptConverter converter)
    {
        mEncryptConverter = converter;
        return this;
    }

    @Override
    public Disk setObjectConverter(ObjectConverter converter)
    {
        mObjectConverter = converter;
        return this;
    }

    @Override
    public Disk setExceptionHandler(ExceptionHandler handler)
    {
        mExceptionHandler = handler;
        return this;
    }

    @Override
    public final boolean checkDirectory()
    {
        if (mDirectory.exists())
            return true;

        synchronized (Disk.class)
        {
            if (mDirectory.exists())
                return true;

            return mDirectory.mkdirs();
        }
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
        if (checkDirectory())
            return mDirectory;
        else
            throw new RuntimeException("directory is not available:" + mDirectory.getAbsolutePath());
    }

    @Override
    public final EncryptConverter getEncryptConverter()
    {
        return mEncryptConverter != null ? mEncryptConverter : getDiskConfig().mEncryptConverter;
    }

    @Override
    public final ObjectConverter getObjectConverter()
    {
        return mObjectConverter != null ? mObjectConverter : getDiskConfig().mObjectConverter;
    }

    @Override
    public final ExceptionHandler getExceptionHandler()
    {
        return mExceptionHandler != null ? mExceptionHandler : getDiskConfig().mExceptionHandler;
    }

    //---------- DiskInfo end ----------

    //---------- util method start ----------

    /**
     * 返回外部存储"Android/data/包名/files/dirName"目录
     *
     * @param dirName
     * @return
     */
    protected static final File getExternalFilesDir(String dirName)
    {
        return getDiskConfig().mContext.getExternalFilesDir(dirName);
    }

    /**
     * 返回内部存储"/data/包名/files/dirName"目录
     *
     * @param dirName
     * @return
     */
    protected static final File getInternalFilesDir(String dirName)
    {
        return new File(getDiskConfig().mContext.getFilesDir(), dirName);
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
