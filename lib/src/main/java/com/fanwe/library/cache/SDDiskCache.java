package com.fanwe.library.cache;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

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
    private static final String STRING = "string_";
    private static final String OBJECT = "object_";
    private static final String SERIALIZABLE = "serializable_";

    private static Map<String, Object> sMapDirLocker = new HashMap<>();
    private Object mLocker;

    private static Context mContext;
    private static IObjectConverter sGlobalObjectConverter;
    private static IEncryptConverter sGlobalEncryptConverter;

    private File mDirectory;
    private IObjectConverter mObjectConverter;
    private IEncryptConverter mEncryptConverter;

    private SDDiskCache(File directory)
    {
        if (directory == null)
        {
            throw new NullPointerException("directory file is null");
        }

        synchronized (sMapDirLocker)
        {
            try
            {
                mDirectory = directory;
                ensureDirectoryExists();

                final String path = directory.getAbsolutePath();
                if (!sMapDirLocker.containsKey(path))
                {
                    sMapDirLocker.put(path, path);
                }

                mLocker = sMapDirLocker.get(path);
            } catch (Exception e)
            {
                Log.e(TAG, String.valueOf(e));
            }
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
    public static SDDiskCache openDir(File directory)
    {
        return new SDDiskCache(directory);
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
        synchronized (mLocker)
        {
            mObjectConverter = objectConverter;
            return this;
        }
    }

    /**
     * 设置加解密转换器
     *
     * @param encryptConverter
     * @return
     */
    public SDDiskCache setEncryptConverter(IEncryptConverter encryptConverter)
    {
        synchronized (mLocker)
        {
            mEncryptConverter = encryptConverter;
            return this;
        }
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

    public boolean removeInt(String key)
    {
        return removeString(INT + key);
    }

    public boolean putInt(String key, int value)
    {
        return putString(INT + key, String.valueOf(value));
    }

    public boolean putInt(String key, int value, boolean encrypt)
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

    public boolean removeLong(String key)
    {
        return removeString(LONG + key);
    }

    public boolean putLong(String key, long value)
    {
        return putString(LONG + key, String.valueOf(value));
    }

    public boolean putLong(String key, long value, boolean encrypt)
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

    public boolean removeFloat(String key)
    {
        return removeString(FLOAT + key);
    }

    public boolean putFloat(String key, float value)
    {
        return putString(FLOAT + key, String.valueOf(value));
    }

    public boolean putFloat(String key, float value, boolean encrypt)
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

    public boolean removeDouble(String key)
    {
        return removeString(DOUBLE + key);
    }

    public boolean putDouble(String key, double value)
    {
        return putString(DOUBLE + key, String.valueOf(value));
    }

    public boolean putDouble(String key, double value, boolean encrypt)
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

    public boolean removeBoolean(String key)
    {
        return removeString(BOOLEAN + key);
    }

    public boolean putBoolean(String key, boolean value)
    {
        return putString(BOOLEAN + key, String.valueOf(value));
    }

    public boolean putBoolean(String key, boolean value, boolean encrypt)
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

    public boolean removeObject(Class clazz)
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
        checkObjectConverter();
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
        return hasSerializable(STRING + key);
    }

    public boolean removeString(String key)
    {
        return removeSerializable(STRING + key);
    }

    public boolean putString(String key, String data)
    {
        return putString(key, data, false);
    }

    public boolean putString(String key, String data, boolean encrypt)
    {
        checkEncryptConverter(encrypt);

        CacheModel model = new CacheModel();
        model.setData(data);
        model.setEncrypt(encrypt);
        model.encryptIfNeed(getEncryptConverter());

        return putSerializable(STRING + key, model);
    }

    public String getString(String key)
    {
        CacheModel model = getSerializable(STRING + key);
        if (model == null)
        {
            return null;
        }
        checkEncryptConverter(model.isEncrypt());

        model.decryptIfNeed(getEncryptConverter());
        final String result = model.getData();
        return result;
    }

    //---------- Serializable start ----------

    public boolean hasSerializable(Class clazz)
    {
        return hasSerializable(clazz.getName());
    }

    public <T extends Serializable> boolean putSerializable(T object)
    {
        return putSerializable(object.getClass().getName(), object);
    }

    public <T extends Serializable> T getSerializable(Class<T> clazz)
    {
        return getSerializable(clazz.getName());
    }

    public boolean removeSerializable(Class clazz)
    {
        return removeSerializable(clazz.getName());
    }

    //---------- Serializable private start ----------

    private boolean hasSerializable(String key)
    {
        synchronized (mLocker)
        {
            File cache = getCacheFile(SERIALIZABLE + key);
            return cache.exists();
        }
    }

    private <T extends Serializable> boolean putSerializable(String key, T object)
    {
        synchronized (mLocker)
        {
            if (object != null)
            {
                ObjectOutputStream os = null;
                try
                {
                    File cache = getCacheFile(SERIALIZABLE + key);
                    os = new ObjectOutputStream(new FileOutputStream(cache));
                    os.writeObject(object);
                    os.flush();
                    return true;
                } catch (Exception e)
                {
                    Log.e(TAG, "putSerializable:" + e);
                } finally
                {
                    FileUtil.closeQuietly(os);
                }
            }
            return false;
        }
    }

    private <T extends Serializable> T getSerializable(String key)
    {
        synchronized (mLocker)
        {
            ObjectInputStream is = null;
            try
            {
                File cache = getCacheFile(SERIALIZABLE + key);
                if (!cache.exists())
                {
                    return null;
                }
                is = new ObjectInputStream(new FileInputStream(cache));
                return (T) is.readObject();
            } catch (Exception e)
            {
                Log.e(TAG, "getSerializable:" + e);
            } finally
            {
                FileUtil.closeQuietly(is);
            }
            return null;
        }
    }

    private boolean removeSerializable(String key)
    {
        synchronized (mLocker)
        {
            File cache = getCacheFile(SERIALIZABLE + key);
            if (cache.exists())
            {
                return cache.delete();
            } else
            {
                return true;
            }
        }
    }

    /**
     * 返回当前目录的大小
     *
     * @return
     */
    public long size()
    {
        synchronized (mLocker)
        {
            return mDirectory.length();
        }
    }

    /**
     * 删除该目录以及目录下的所有缓存
     */
    public void delete()
    {
        synchronized (mLocker)
        {
            FileUtil.deleteFileOrDir(mDirectory);
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

    private void ensureDirectoryExists()
    {
        if (!mDirectory.exists())
        {
            mDirectory.mkdirs();
        }
    }

    private File getCacheFile(String key)
    {
        ensureDirectoryExists();

        String realkey = createRealKey(key);
        File cache = new File(mDirectory, realkey);
        return cache;
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
