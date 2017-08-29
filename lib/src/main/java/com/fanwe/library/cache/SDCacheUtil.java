package com.fanwe.library.cache;

import android.util.Log;

import com.jakewharton.disklrucache.DiskLruCache;

import java.io.Closeable;
import java.io.File;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Administrator on 2017/8/29.
 */

public class SDCacheUtil
{
    private static final String TAG = "SDCacheUtil";

    private static final int DEFAULT_APP_VERSION = 0;
    private static final int DEFAULT_VALUE_COUNT = 1;
    private static final int DEFAULT_INDEX = 0;
    private static final long DEFAULT_MAX_SIZE = Long.MAX_VALUE;

    private static final String DEFAULT_FILE_DIR = "file";
    private static final String DEFAULT_CACHE_DIR = "cache";

    private DiskLruCache mDiskLruCache;
    private IObjectConverter mObjectConverter;
    private IEncryptConverter mEncryptConverter;

    private SDCacheUtil()
    {
    }

    public SDCacheUtil openFile()
    {
        return openFile(DEFAULT_FILE_DIR);
    }

    public SDCacheUtil openFile(String dirName)
    {
        try
        {
            mDiskLruCache = DiskLruCache.open(getFileDir(dirName), DEFAULT_APP_VERSION, DEFAULT_VALUE_COUNT, DEFAULT_MAX_SIZE);
        } catch (Exception e)
        {
            e.printStackTrace();
            Log.e(TAG, "openFile error:" + String.valueOf(e));
        }
        return this;
    }

    /**
     * 设置对象转换器
     *
     * @param objectConverter
     * @return
     */
    public SDCacheUtil setObjectConverter(IObjectConverter objectConverter)
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
    public SDCacheUtil setEncryptConverter(IEncryptConverter encryptConverter)
    {
        mEncryptConverter = encryptConverter;
        return this;
    }

    public SDCacheUtil putObject(Object object, boolean encrypt)
    {
        checkObjectConverter();
        if (object != null)
        {
            try
            {
                String key = createRealKey(object.getClass().getName());

                DiskLruCache.Editor editor = mDiskLruCache.edit(key);

                final String data = mObjectConverter.objectToString(object);
                CacheModel model = new CacheModel();
                model.setData(data);
                model.setEncrypt(encrypt);
                model.encryptIfNeed(mEncryptConverter);

                final String result = mObjectConverter.objectToString(model);
                editor.set(DEFAULT_INDEX, result);
                editor.commit();
            } catch (Exception e)
            {
                e.printStackTrace();
                Log.e(TAG, "putObject error:" + String.valueOf(e));
            }
        }
        return this;
    }

    public <T extends Serializable> T getObject(Class<T> clazz)
    {
        checkObjectConverter();
        T object = null;
        if (clazz != null)
        {
            try
            {
                String key = createRealKey(object.getClass().getName());

                DiskLruCache.Editor editor = mDiskLruCache.edit(key);
                String content = editor.getString(DEFAULT_INDEX);

            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return object;
    }

    public SDCacheUtil putString(String key, String data, boolean encrypt)
    {
        checkObjectConverter();
        checkEncryptConverter(encrypt);
        try
        {
            String realKey = createRealKey(key);
            DiskLruCache.Editor editor = mDiskLruCache.edit(realKey);

            CacheModel model = new CacheModel();
            model.setData(data);
            model.setEncrypt(encrypt);
            model.encryptIfNeed(mEncryptConverter);

            final String result = mObjectConverter.objectToString(model);
            editor.set(DEFAULT_INDEX, result);
            editor.commit();
        } catch (Exception e)
        {
            e.printStackTrace();
            Log.e(TAG, "putObject error:" + String.valueOf(e));
        }
        return this;
    }

    public String getString(String key)
    {
        checkObjectConverter();
        try
        {
            String realKey = createRealKey(key);
            DiskLruCache.Editor editor = mDiskLruCache.edit(realKey);

            String content = editor.getString(DEFAULT_INDEX);
            CacheModel model = mObjectConverter.stringToObject(content, CacheModel.class);
            model.decryptIfNeed(mEncryptConverter);
            return model.getData();
        } catch (Exception e)
        {
            e.printStackTrace();
            Log.e(TAG, "getString error:" + String.valueOf(e));
        }
        return null;
    }

    public long size()
    {
        return mDiskLruCache.size();
    }

    //---------- util method start ----------

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

    private File getFileDir(String dirName)
    {
        File file = SDCache.getContext().getExternalFilesDir(dirName);
        return file;
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
