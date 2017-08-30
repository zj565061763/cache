package com.fanwe.library.cache;

import android.content.Context;
import android.text.TextUtils;
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

    private static final String DEFAULT_FILE_DIR = "files";
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
        return hasStringValue(key, ValueType.Int);
    }

    public boolean removeInt(String key)
    {
        return removeStringValue(key, ValueType.Int);
    }

    public boolean putInt(String key, int value)
    {
        return putStringValue(key, String.valueOf(value), false, ValueType.Int);
    }

    public int getInt(String key, int defValue)
    {
        String content = getStringValue(key, ValueType.Int);
        if (content == null)
        {
            return defValue;
        }
        return Integer.valueOf(content);
    }

    //---------- long start ----------

    public boolean hasLong(String key)
    {
        return hasStringValue(key, ValueType.Long);
    }

    public boolean removeLong(String key)
    {
        return removeStringValue(key, ValueType.Long);
    }

    public boolean putLong(String key, long value)
    {
        return putStringValue(key, String.valueOf(value), false, ValueType.Long);
    }

    public long getLong(String key, long defValue)
    {
        String content = getStringValue(key, ValueType.Long);
        if (content == null)
        {
            return defValue;
        }
        return Long.valueOf(content);
    }

    //---------- float start ----------

    public boolean hasFloat(String key)
    {
        return hasStringValue(key, ValueType.Float);
    }

    public boolean removeFloat(String key)
    {
        return removeStringValue(key, ValueType.Float);
    }

    public boolean putFloat(String key, float value)
    {
        return putStringValue(key, String.valueOf(value), false, ValueType.Float);
    }

    public float getFloat(String key, float defValue)
    {
        String content = getStringValue(key, ValueType.Float);
        if (content == null)
        {
            return defValue;
        }
        return Float.valueOf(content);
    }

    //---------- double start ----------

    public boolean hasDouble(String key)
    {
        return hasStringValue(key, ValueType.Double);
    }

    public boolean removeDouble(String key)
    {
        return removeStringValue(key, ValueType.Double);
    }

    public boolean putDouble(String key, double value)
    {
        return putStringValue(key, String.valueOf(value), false, ValueType.Double);
    }

    public double getDouble(String key, double defValue)
    {
        String content = getStringValue(key, ValueType.Double);
        if (content == null)
        {
            return defValue;
        }
        return Double.valueOf(content);
    }

    //---------- boolean start ----------

    public boolean hasBoolean(String key)
    {
        return hasStringValue(key, ValueType.Boolean);
    }

    public boolean removeBoolean(String key)
    {
        return removeStringValue(key, ValueType.Boolean);
    }

    public boolean putBoolean(String key, boolean value)
    {
        return putStringValue(key, String.valueOf(value), false, ValueType.Boolean);
    }

    public boolean getBoolean(String key, boolean defValue)
    {
        String content = getStringValue(key, ValueType.Boolean);
        if (content == null)
        {
            return defValue;
        }
        return Boolean.valueOf(content);
    }

    //---------- object start ----------

    public boolean hasObject(Class clazz)
    {
        return hasStringValue(clazz.getName(), ValueType.Object);
    }

    public boolean removeObject(Class clazz)
    {
        return removeStringValue(clazz.getName(), ValueType.Object);
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
            putStringValue(object.getClass().getName(), data, encrypt, ValueType.Object);
        }
        return this;
    }

    public <T> T getObject(Class<T> clazz)
    {
        checkObjectConverter();
        String content = getStringValue(clazz.getName(), ValueType.Object);
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
        return hasStringValue(key, ValueType.String);
    }

    public boolean removeString(String key)
    {
        return removeStringValue(key, ValueType.String);
    }

    public boolean putString(String key, String data)
    {
        return putString(key, data, false);
    }

    public boolean putString(String key, String data, boolean encrypt)
    {
        return putStringValue(key, data, encrypt, ValueType.String);
    }

    public String getString(String key)
    {
        return getStringValue(key, ValueType.String);
    }

    //---------- private string value start ----------

    private boolean hasStringValue(String key, ValueType type)
    {
        return hasSerializable(getValueTypeKey(key, type));
    }

    private boolean removeStringValue(String key, ValueType type)
    {
        return removeSerializable(getValueTypeKey(key, type));
    }

    private boolean putStringValue(String key, String data, boolean encrypt, ValueType type)
    {
        checkEncryptConverter(encrypt);

        CacheModel model = new CacheModel();
        model.setData(data);
        model.setEncrypt(encrypt);
        model.encryptIfNeed(getEncryptConverter());

        return putSerializable(getValueTypeKey(key, type), model);
    }

    private String getStringValue(String key, ValueType type)
    {
        CacheModel model = getSerializable(getValueTypeKey(key, type));
        if (model == null)
        {
            return null;
        }
        checkEncryptConverter(model.isEncrypt());

        model.decryptIfNeed(getEncryptConverter());
        return model.getData();
    }

    //---------- Serializable start ----------

    public boolean hasSerializable(Class clazz)
    {
        String key = generateKey(clazz.getName());
        return hasSerializable(key);
    }

    public <T extends Serializable> boolean putSerializable(T object)
    {
        String key = generateKey(object.getClass().getName());
        return putSerializable(key, object);
    }

    public <T extends Serializable> T getSerializable(Class<T> clazz)
    {
        String key = generateKey(clazz.getName());
        return getSerializable(key);
    }

    public boolean removeSerializable(Class clazz)
    {
        String key = generateKey(clazz.getName());
        return removeSerializable(key);
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

    private String getValueTypeKey(String key, ValueType type)
    {
        if (TextUtils.isEmpty(key))
        {
            throw new IllegalArgumentException("key must not be null or empty");
        }
        key = generateKey(key);
        switch (type)
        {
            case Int:
                return INT + key;
            case Long:
                return LONG + key;
            case Float:
                return FLOAT + key;
            case Double:
                return DOUBLE + key;
            case Boolean:
                return BOOLEAN + key;
            case String:
                return STRING + key;
            case Object:
                return OBJECT + key;
            default:
                return "";
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
        File cache = new File(mDirectory, key);
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

    private static String generateKey(String key)
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
