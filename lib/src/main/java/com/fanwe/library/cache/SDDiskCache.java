package com.fanwe.library.cache;

import android.content.Context;
import android.util.Log;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
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

    public static void init(Context context)
    {
        mContext = context.getApplicationContext();
    }

    public static SDDiskCache openFile()
    {
        return openFile(DEFAULT_FILE_DIR);
    }

    public static SDDiskCache openCache()
    {
        return openCache(DEFAULT_CACHE_DIR);
    }

    public static SDDiskCache openFile(String dirName)
    {
        return open(getFileDir(dirName));
    }

    public static SDDiskCache openCache(String dirName)
    {
        return open(getCacheDir(dirName));
    }

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

    //---------- object start ----------

    public SDDiskCache putObject(Object object)
    {
        return putObject(object, false);
    }

    public SDDiskCache putObject(Object object, boolean encrypt)
    {
        checkObjectConverter();
        if (object != null)
        {
            String realKey = createRealKey(OBJECT + object.getClass().getName());

            String data = mObjectConverter.objectToString(object);
            putString(realKey, data, encrypt);
        }
        return this;
    }

    public <T extends Serializable> T getObject(Class<T> clazz)
    {
        checkObjectConverter();
        T object = null;
        if (clazz != null)
        {
            String realKey = createRealKey(OBJECT + object.getClass().getName());

            String content = getString(realKey);
            object = mObjectConverter.stringToObject(content, clazz);
        }
        return object;
    }
    //---------- object end ----------

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
    //---------- string end ----------

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
