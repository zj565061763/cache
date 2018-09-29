package com.sd.lib.cache;

import com.sd.lib.cache.handler.BooleanHandler;
import com.sd.lib.cache.handler.DoubleHandler;
import com.sd.lib.cache.handler.FloatHandler;
import com.sd.lib.cache.handler.IntegerHandler;
import com.sd.lib.cache.handler.LongHandler;
import com.sd.lib.cache.handler.ObjectHandler;
import com.sd.lib.cache.handler.SerializableHandler;
import com.sd.lib.cache.handler.StringHandler;

import java.io.File;

public class FDisk extends BaseDisk
{
    private static final String DEFAULT_FILE_DIR = "disk_file";

    protected FDisk(File directory)
    {
        super(directory);
    }

    //---------- open start ----------

    /**
     * 内部存储目录"/data/包名/files/disk_file"
     *
     * @return
     */
    public static Disk open()
    {
        return openDir(getInternalFilesDir(DEFAULT_FILE_DIR));
    }

    /**
     * 优先使用外部存储，如果外部存储不存在，则使用内部存储
     * <p>
     * 外部存储目录"Android/data/包名/files/disk_file"
     *
     * @return
     */
    public static Disk openExternal()
    {
        final Disk disk = openDir(getExternalFilesDir(DEFAULT_FILE_DIR));
        if (disk.checkDirectory())
            return disk;
        else
            return open();
    }

    /**
     * 打开指定的目录
     *
     * @param directory
     * @return
     */
    public static Disk openDir(File directory)
    {
        return new FDisk(directory);
    }

    //---------- open end ----------

    //---------- cache start ----------

    private IntegerHandler mIntegerHandler;
    private LongHandler mLongHandler;
    private FloatHandler mFloatHandler;
    private DoubleHandler mDoubleHandler;
    private BooleanHandler mBooleanHandler;
    private StringHandler mStringHandler;

    private SerializableHandler mSerializableHandler;
    private ObjectHandler mObjectHandler;

    @Override
    public CommonCache<Integer> cacheInteger()
    {
        if (mIntegerHandler == null)
            mIntegerHandler = new IntegerHandler(this);
        return mIntegerHandler;
    }

    @Override
    public CommonCache<Long> cacheLong()
    {
        if (mLongHandler == null)
            mLongHandler = new LongHandler(this);
        return mLongHandler;
    }

    @Override
    public CommonCache<Float> cacheFloat()
    {
        if (mFloatHandler == null)
            mFloatHandler = new FloatHandler(this);
        return mFloatHandler;
    }

    @Override
    public CommonCache<Double> cacheDouble()
    {
        if (mDoubleHandler == null)
            mDoubleHandler = new DoubleHandler(this);
        return mDoubleHandler;
    }

    @Override
    public CommonCache<Boolean> cacheBoolean()
    {
        if (mBooleanHandler == null)
            mBooleanHandler = new BooleanHandler(this);
        return mBooleanHandler;
    }

    @Override
    public CommonCache<String> cacheString()
    {
        if (mStringHandler == null)
            mStringHandler = new StringHandler(this);
        return mStringHandler;
    }

    @Override
    public SerializableCache cacheSerializable()
    {
        if (mSerializableHandler == null)
            mSerializableHandler = new SerializableHandler(this);
        return mSerializableHandler;
    }

    @Override
    public ObjectCache cacheObject()
    {
        if (mObjectHandler == null)
            mObjectHandler = new ObjectHandler(this);
        return mObjectHandler;
    }

    //---------- cache end ----------
}
