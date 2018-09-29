package com.sd.lib.cache.handler;

import com.sd.lib.cache.Disk;
import com.sd.lib.cache.DiskInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * 缓存可以和byte数组互相转换的处理类
 */
abstract class BytesConverterHandler<T> extends BaseHandler<T>
{
    public BytesConverterHandler(DiskInfo diskInfo)
    {
        super(diskInfo);
    }

    @Override
    protected final boolean putCacheImpl(String key, T value, File file)
    {
        final boolean encrypt = getDiskInfo().isEncrypt();
        final Disk.EncryptConverter converter = getDiskInfo().getEncryptConverter();
        if (encrypt && converter == null)
            throw new RuntimeException("you must provide an EncryptConverter instance before this");

        byte[] data = valueToByte(value);
        if (data == null)
            throw new RuntimeException("valueToByte(T) method return null");

        if (encrypt)
        {
            data = converter.encrypt(data);
            if (data == null)
                throw new RuntimeException("EncryptConverter.encrypt(byte[]) method return null");
        }

        final byte[] dataWithTag = Arrays.copyOf(data, data.length + 1);
        dataWithTag[dataWithTag.length - 1] = (byte) (encrypt ? 1 : 0);


        OutputStream out = null;
        try
        {
            out = new FileOutputStream(file);
            Utils.writeBytes(dataWithTag, out);
            return true;
        } catch (Exception e)
        {
            getDiskInfo().getExceptionHandler().onException(e);
            return false;
        } finally
        {
            Utils.closeQuietly(out);
        }
    }

    @Override
    protected final T getCacheImpl(String key, Class clazz, File file)
    {
        byte[] data = null;
        InputStream in = null;
        try
        {
            in = new FileInputStream(file);
            data = Utils.readBytes(in);
        } catch (Exception e)
        {
            getDiskInfo().getExceptionHandler().onException(e);
            return null;
        } finally
        {
            Utils.closeQuietly(in);
        }


        final boolean isEncrypted = data[data.length - 1] == 1;
        final Disk.EncryptConverter converter = getDiskInfo().getEncryptConverter();
        if (isEncrypted && converter == null)
        {
            getDiskInfo().getExceptionHandler().onException(new RuntimeException("content is encrypted but EncryptConverter not found when try decrypt"));
            return null;
        }

        data = Arrays.copyOf(data, data.length - 1);

        if (isEncrypted)
        {
            data = converter.decrypt(data);
            if (data == null)
                return null;
        }

        try
        {
            return byteToValue(data, clazz);
        } catch (Exception e)
        {
            getDiskInfo().getExceptionHandler().onException(e);
            return null;
        }
    }

    /**
     * 缓存转byte
     *
     * @param value
     * @return
     */
    protected abstract byte[] valueToByte(T value);

    /**
     * byte转缓存
     *
     * @param bytes
     * @param clazz
     * @return
     */
    protected abstract T byteToValue(byte[] bytes, Class clazz);
}
