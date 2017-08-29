package com.fanwe.library.cache;

import android.content.Context;
import android.util.Log;

import com.jakewharton.disklrucache.DiskLruCache;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Administrator on 2017/8/29.
 */

public class SDDiskCache
{
    private static final String TAG = "SDDiskCache";

    private static final int DEFAULT_APP_VERSION = 0;
    private static final int DEFAULT_INDEX = 0;
    private static final long DEFAULT_MAX_SIZE = Long.MAX_VALUE;

    private static final String DEFAULT_FILE_DIR = "file";
    private static final String DEFAULT_CACHE_DIR = "cache";

    private static final String INT = "int_";
    private static final String LONG = "long_";
    private static final String FLOAT = "float_";
    private static final String DOUBLE = "double_";
    private static final String BOOLEAN = "boolean_";
    private static final String OBJECT = "object_";

    private static Context mContext;
    private static IObjectConverter sGlobalObjectConverter;
    private static IEncryptConverter sGlobalEncryptConverter;

    private DiskLruCache mDiskLruCache;
    private IObjectConverter mObjectConverter;
    private IEncryptConverter mEncryptConverter;

    private SDDiskCache(File directory, int appVersion, long maxSize)
    {
        if (directory == null)
        {
            throw new NullPointerException("directory file is null");
        }
        try
        {
            mDiskLruCache = DiskLruCache.open(directory, appVersion, 1, maxSize);
        } catch (Exception e)
        {
            Log.e(TAG, String.valueOf(e));
        }
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
     * 返回和"Android/data/包名/files/file"目录关联的操作对象
     *
     * @return
     */
    public static SDDiskCache open()
    {
        return open(DEFAULT_FILE_DIR);
    }

    /**
     * 返回和"Android/data/包名/files/dirName"目录关联的操作对象
     *
     * @param dirName
     * @return
     */
    public static SDDiskCache open(String dirName)
    {
        return openDir(getFileDir(dirName));
    }

    /**
     * 返回和"Android/data/包名/cache/cache"目录关联的操作对象
     *
     * @return
     */
    public static SDDiskCache openCache()
    {
        return openCache(DEFAULT_CACHE_DIR);
    }

    /**
     * 返回和"Android/data/包名/cache/dirName"目录关联的操作对象
     *
     * @return
     */
    public static SDDiskCache openCache(String dirName)
    {
        return openDir(getCacheDir(dirName));
    }

    /**
     * 返回和指定目录关联的操作对象
     *
     * @param directory
     * @return
     */
    public synchronized static SDDiskCache openDir(File directory)
    {
        return new SDDiskCache(directory, DEFAULT_APP_VERSION, DEFAULT_MAX_SIZE);
    }

    /**
     * 设置全局对象转换器
     *
     * @param globalObjectConverter
     */
    public static void setGlobalObjectConverter(IObjectConverter globalObjectConverter)
    {
        sGlobalObjectConverter = globalObjectConverter;
    }

    /**
     * 设置全局加解密转换器
     *
     * @param globalEncryptConverter
     */
    public static void setGlobalEncryptConverter(IEncryptConverter globalEncryptConverter)
    {
        sGlobalEncryptConverter = globalEncryptConverter;
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

    private IObjectConverter getObjectConverter()
    {
        if (mObjectConverter != null)
        {
            return mObjectConverter;
        } else
        {
            return sGlobalObjectConverter;
        }
    }

    private IEncryptConverter getEncryptConverter()
    {
        if (mEncryptConverter != null)
        {
            return mEncryptConverter;
        } else
        {
            return sGlobalEncryptConverter;
        }
    }

    //---------- int start ----------

    public boolean hasInt(String key)
    {
        return hasString(INT + key);
    }

    public SDDiskCache removeInt(String key)
    {
        return removeString(INT + key);
    }

    public SDDiskCache putInt(String key, int value)
    {
        return putString(INT + key, String.valueOf(value));
    }

    public SDDiskCache putInt(String key, int value, boolean encrypt)
    {
        return putString(INT + key, String.valueOf(value), encrypt);
    }

    public int getInt(String key, int defValue)
    {
        String content = getString(INT + key);
        if (content == null)
        {
            return defValue;
        }
        return Integer.valueOf(content);
    }

    //---------- long start ----------

    public boolean hasLong(String key)
    {
        return hasString(LONG + key);
    }

    public SDDiskCache removeLong(String key)
    {
        return removeString(LONG + key);
    }

    public SDDiskCache putLong(String key, long value)
    {
        return putString(LONG + key, String.valueOf(value));
    }

    public SDDiskCache putLong(String key, long value, boolean encrypt)
    {
        return putString(LONG + key, String.valueOf(value), encrypt);
    }

    public long getLong(String key, long defValue)
    {
        String content = getString(LONG + key);
        if (content == null)
        {
            return defValue;
        }
        return Long.valueOf(content);
    }

    //---------- float start ----------

    public boolean hasFloat(String key)
    {
        return hasString(FLOAT + key);
    }

    public SDDiskCache removeFloat(String key)
    {
        return removeString(FLOAT + key);
    }

    public SDDiskCache putFloat(String key, float value)
    {
        return putString(FLOAT + key, String.valueOf(value));
    }

    public SDDiskCache putFloat(String key, float value, boolean encrypt)
    {
        return putString(FLOAT + key, String.valueOf(value), encrypt);
    }

    public float getFloat(String key, float defValue)
    {
        String content = getString(FLOAT + key);
        if (content == null)
        {
            return defValue;
        }
        return Float.valueOf(content);
    }

    //---------- double start ----------

    public boolean hasDouble(String key)
    {
        return hasString(DOUBLE + key);
    }

    public SDDiskCache removeDouble(String key)
    {
        return removeString(DOUBLE + key);
    }

    public SDDiskCache putDouble(String key, double value)
    {
        return putString(DOUBLE + key, String.valueOf(value));
    }

    public SDDiskCache putDouble(String key, double value, boolean encrypt)
    {
        return putString(DOUBLE + key, String.valueOf(value), encrypt);
    }

    public double getDouble(String key, double defValue)
    {
        String content = getString(DOUBLE + key);
        if (content == null)
        {
            return defValue;
        }
        return Double.valueOf(content);
    }

    //---------- boolean start ----------

    public boolean hasBoolean(String key)
    {
        return hasString(BOOLEAN + key);
    }

    public SDDiskCache removeBoolean(String key)
    {
        return removeString(BOOLEAN + key);
    }

    public SDDiskCache putBoolean(String key, boolean value)
    {
        return putString(BOOLEAN + key, String.valueOf(value));
    }

    public SDDiskCache putBoolean(String key, boolean value, boolean encrypt)
    {
        return putString(BOOLEAN + key, String.valueOf(value), encrypt);
    }

    public boolean getBoolean(String key, boolean defValue)
    {
        String content = getString(BOOLEAN + key);
        if (content == null)
        {
            return defValue;
        }
        return Boolean.valueOf(content);
    }

    //---------- object start ----------

    public boolean hasObject(Class clazz)
    {
        return hasString(OBJECT + clazz.getName());
    }

    public SDDiskCache removeObject(Class clazz)
    {
        return removeString(OBJECT + clazz.getName());
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
            String data = getObjectConverter().objectToString(object);
            putString(OBJECT + object.getClass().getName(), data, encrypt);
        }
        return this;
    }

    public <T> T getObject(Class<T> clazz)
    {
        String content = getString(OBJECT + clazz.getName());
        if (content == null)
        {
            return null;
        } else
        {
            return getObjectConverter().stringToObject(content, clazz);
        }
    }

    //---------- string start ----------

    public boolean hasString(String key)
    {
        String realKey = createRealKey(key);
        try
        {
            return mDiskLruCache.get(realKey) != null;
        } catch (Exception e)
        {
            Log.e(TAG, "hasString error:" + e);
        }
        return false;
    }

    public SDDiskCache removeString(String key)
    {
        String realKey = createRealKey(key);
        try
        {
            mDiskLruCache.remove(realKey);
        } catch (Exception e)
        {
            Log.e(TAG, "removeString error:" + e);
        }
        return this;
    }

    public SDDiskCache putString(String key, String data)
    {
        return putString(key, data, false);
    }

    public SDDiskCache putString(String key, String data, boolean encrypt)
    {
        checkObjectConverter();
        checkEncryptConverter(encrypt);

        try
        {
            CacheModel model = new CacheModel();
            model.setData(data);
            model.setEncrypt(encrypt);
            model.encryptIfNeed(getEncryptConverter());
            final String result = getObjectConverter().objectToString(model);

            String realKey = createRealKey(key);
            DiskLruCache.Editor editor = mDiskLruCache.edit(realKey);
            editor.set(DEFAULT_INDEX, result);
            editor.commit();
        } catch (Exception e)
        {
            Log.e(TAG, "putString error:" + e);
        }
        return this;
    }

    public String getString(String key)
    {
        checkObjectConverter();

        try
        {
            String realKey = createRealKey(key);
            if (mDiskLruCache.get(realKey) == null)
            {
                return null;
            }
            String content = mDiskLruCache.edit(realKey).getString(DEFAULT_INDEX);
            if (content == null)
            {
                return null;
            }

            CacheModel model = getObjectConverter().stringToObject(content, CacheModel.class);
            model.decryptIfNeed(getEncryptConverter());
            final String result = model.getData();
            return result;
        } catch (Exception e)
        {
            Log.e(TAG, "getString error:" + e);
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
        return mDiskLruCache.size();
    }

    /**
     * 删除该目录以及目录下的所有缓存
     */
    public void delete()
    {
        try
        {
            mDiskLruCache.delete();
        } catch (Exception e)
        {
            Log.e(TAG, String.valueOf(e));
        }
    }

    //---------- util method start ----------

    private static Context getContext()
    {
        return mContext;
    }

    private void checkObjectConverter()
    {
        if (getObjectConverter() == null)
        {
            throw new NullPointerException("you must provide an IObjectConverter instance before this");
        }
    }

    private void checkEncryptConverter(boolean encrypt)
    {
        if (encrypt && getEncryptConverter() == null)
        {
            throw new NullPointerException("you must provide an IEncryptConverter instance before this");
        }
    }

    private static void checkContext()
    {
        if (mContext == null)
        {
            throw new NullPointerException("you must invoke init() method before this");
        }
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
            Log.e(TAG, "MD5 error:" + e);
            result = null;
        }
        return result;
    }

    //---------- util method end ----------
}
