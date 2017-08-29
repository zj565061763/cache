package com.fanwe.library.cache;

import android.content.Context;
import android.util.Log;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Administrator on 2017/8/29.
 */

public class SDDiskCache
{
    private static final String TAG = "SDDiskCache";

    private static final String DEFAULT_FILE_DIR = "file";
    private static final String DEFAULT_CACHE_DIR = "cache";

    private static final String INT = "int_";
    private static final String LONG = "long_";
    private static final String FLOAT = "float_";
    private static final String DOUBLE = "double_";
    private static final String BOOLEAN = "boolean_";
    private static final String OBJECT = "object_";

    private static Context mContext;

    private File mDirectory;
    private IObjectConverter mObjectConverter;
    private IEncryptConverter mEncryptConverter;

    private SDDiskCache(File directory)
    {
        if (directory == null)
        {
            throw new NullPointerException("directory file is null");
        }
        mDirectory = directory;
        ensureDirectoryNotNull();
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
     * 返回和SD卡文件目录下"file"目录关联的操作对象
     *
     * @return
     */
    public static SDDiskCache open()
    {
        return open(DEFAULT_FILE_DIR);
    }

    /**
     * 返回和SD卡缓存目录下"cache"目录关联的操作对象
     *
     * @return
     */
    public static SDDiskCache openCache()
    {
        return openCache(DEFAULT_CACHE_DIR);
    }

    /**
     * 返回和SD卡文件目录下指定目录关联的操作对象
     *
     * @param dirName
     * @return
     */
    public static SDDiskCache open(String dirName)
    {
        return open(getFileDir(dirName));
    }

    /**
     * 返回和SD卡缓存目录下指定目录关联的操作对象
     *
     * @param dirName
     * @return
     */
    public static SDDiskCache openCache(String dirName)
    {
        return open(getCacheDir(dirName));
    }

    /**
     * 返回和指定目录关联的操作对象
     *
     * @param directory
     * @return
     */
    public static SDDiskCache open(File directory)
    {
        return new SDDiskCache(directory);
    }

    /**
     * 设置对象转换器
     *
     * @param objectConverter
     * @return
     */
    public SDDiskCache setObjectConverter(IObjectConverter objectConverter)
    {
        mObjectConverter = objectConverter;
        return this;
    }

    /**
     * 设置加解密转换器
     *
     * @param encryptConverter
     * @return
     */
    public SDDiskCache setEncryptConverter(IEncryptConverter encryptConverter)
    {
        mEncryptConverter = encryptConverter;
        return this;
    }

    //---------- int start ----------

    public boolean hasInt(String key)
    {
        return hasString(INT + key);
    }

    public SDDiskCache putInt(String key, int value)
    {
        return putString(INT + key, String.valueOf(value));
    }

    public SDDiskCache putInt(String key, int value, boolean encrypt)
    {
        return putString(INT + key, String.valueOf(value), encrypt);
    }

    public int getInt(String key)
    {
        return Integer.valueOf(getString(key));
    }

    //---------- long start ----------

    public boolean hasLong(String key)
    {
        return hasString(LONG + key);
    }

    public SDDiskCache putLong(String key, long value)
    {
        return putString(LONG + key, String.valueOf(value));
    }

    public SDDiskCache putLong(String key, long value, boolean encrypt)
    {
        return putString(LONG + key, String.valueOf(value), encrypt);
    }

    public long getLong(String key)
    {
        return Long.valueOf(getString(key));
    }

    //---------- float start ----------

    public boolean hasFloat(String key)
    {
        return hasString(FLOAT + key);
    }

    public SDDiskCache putFloat(String key, float value)
    {
        return putString(FLOAT + key, String.valueOf(value));
    }

    public SDDiskCache putFloat(String key, float value, boolean encrypt)
    {
        return putString(FLOAT + key, String.valueOf(value), encrypt);
    }

    public float getFloat(String key)
    {
        return Float.valueOf(getString(key));
    }

    //---------- double start ----------

    public boolean hasDouble(String key)
    {
        return hasString(DOUBLE + key);
    }

    public SDDiskCache putDouble(String key, double value)
    {
        return putString(DOUBLE + key, String.valueOf(value));
    }

    public SDDiskCache putDouble(String key, double value, boolean encrypt)
    {
        return putString(DOUBLE + key, String.valueOf(value), encrypt);
    }

    public double getDouble(String key)
    {
        return Double.valueOf(getString(key));
    }

    //---------- boolean start ----------

    public boolean hasBoolean(String key)
    {
        return hasString(BOOLEAN + key);
    }

    public SDDiskCache putBoolean(String key, boolean value)
    {
        return putString(BOOLEAN + key, String.valueOf(value));
    }

    public SDDiskCache putBoolean(String key, boolean value, boolean encrypt)
    {
        return putString(BOOLEAN + key, String.valueOf(value), encrypt);
    }

    public boolean getBoolean(String key)
    {
        return Boolean.valueOf(getString(key));
    }

    //---------- object start ----------

    public <T> boolean hasObject(Class<T> clazz)
    {
        return hasString(OBJECT + clazz.getName());
    }

    public SDDiskCache putObject(Object object)
    {
        return putObject(object, false);
    }

    public SDDiskCache putObject(Object object, boolean encrypt)
    {
        checkObjectConverter();
        if (object != null)
        {
            String data = mObjectConverter.objectToString(object);
            putString(OBJECT + object.getClass().getName(), data, encrypt);
        }
        return this;
    }

    public <T> T getObject(Class<T> clazz)
    {
        checkObjectConverter();
        T object = null;
        if (clazz != null)
        {
            String content = getString(OBJECT + object.getClass().getName());
            object = mObjectConverter.stringToObject(content, clazz);
        }
        return object;
    }

    //---------- string start ----------

    public boolean hasString(String key)
    {
        String realKey = createRealKey(key);
        File cacheFile = getCahceFile(realKey);
        return cacheFile.exists();
    }

    public SDDiskCache putString(String key, String data)
    {
        return putString(key, data, false);
    }

    public SDDiskCache putString(String key, String data, boolean encrypt)
    {
        checkObjectConverter();
        checkEncryptConverter(encrypt);

        OutputStream os = null;
        try
        {
            String realKey = createRealKey(key);
            File cacheFile = getCahceFile(realKey);
            if (!cacheFile.exists())
            {
                cacheFile.mkdirs();
            }

            CacheModel model = new CacheModel();
            model.setData(data);
            model.setEncrypt(encrypt);
            model.encryptIfNeed(mEncryptConverter);
            final String result = mObjectConverter.objectToString(model);

            os = new FileOutputStream(cacheFile);
            FileUtil.writeString(os, result);
        } catch (Exception e)
        {
            e.printStackTrace();
            Log.e(TAG, "putString error:" + String.valueOf(e));
        } finally
        {
            FileUtil.closeQuietly(os);
        }
        return this;
    }

    public String getString(String key)
    {
        checkObjectConverter();

        InputStream is = null;
        try
        {
            String realKey = createRealKey(key);
            File cacheFile = getCahceFile(realKey);
            if (!cacheFile.exists())
            {
                return null;
            }

            is = new FileInputStream(cacheFile);
            String content = FileUtil.readString(is);
            if (content == null)
            {
                return null;
            }

            CacheModel model = mObjectConverter.stringToObject(content, CacheModel.class);
            model.decryptIfNeed(mEncryptConverter);
            final String result = model.getData();
            return result;
        } catch (Exception e)
        {
            e.printStackTrace();
            Log.e(TAG, "getString error:" + String.valueOf(e));
        } finally
        {
            FileUtil.closeQuietly(is);
        }
        return null;
    }

    /**
     * 返回当前目录的大小
     *
     * @return
     */
    public long size()
    {
        return mDirectory.length();
    }

    //---------- util method start ----------

    public static Context getContext()
    {
        return mContext;
    }

    private void checkObjectConverter()
    {
        if (mObjectConverter == null)
        {
            throw new NullPointerException("JsonConverter is null you must invoke setObjectConverter() method before this");
        }
    }

    private void checkEncryptConverter(boolean encrypt)
    {
        if (encrypt && mEncryptConverter == null)
        {
            throw new NullPointerException("EncryptConverter is null you must invoke setEncryptConverter() method before this");
        }
    }

    private static void checkContext()
    {
        if (mContext == null)
        {
            throw new NullPointerException("Context is null you must invoke init() static method before this");
        }
    }

    private void ensureDirectoryNotNull()
    {
        if (!mDirectory.exists())
        {
            mDirectory.mkdirs();
        }
    }

    private File getCahceFile(String key)
    {
        ensureDirectoryNotNull();
        File cacheFile = new File(mDirectory, key);
        return cacheFile;
    }

    private static File getFileDir(String dirName)
    {
        checkContext();
        File dir = getContext().getExternalFilesDir(dirName);
        return dir;
    }

    private static File getCacheDir(String dirName)
    {
        checkContext();
        File dir = new File(getContext().getExternalCacheDir(), dirName);
        return dir;
    }

    private static String createRealKey(String key)
    {
        return MD5(key);
    }

    private static String MD5(String value)
    {
        String result;
        try
        {
            final MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(value.getBytes());
            byte[] bytes = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++)
            {
                String hex = Integer.toHexString(0xFF & bytes[i]);
                if (hex.length() == 1)
                {
                    sb.append('0');
                }
                sb.append(hex);
            }
            result = sb.toString();
        } catch (NoSuchAlgorithmException e)
        {
            result = null;
        }
        return result;
    }

    private static void closeQuietly(Closeable closeable)
    {
        if (closeable != null)
        {
            try
            {
                closeable.close();
            } catch (Throwable ignored)
            {
            }
        }
    }

    //---------- util method end ----------
}
