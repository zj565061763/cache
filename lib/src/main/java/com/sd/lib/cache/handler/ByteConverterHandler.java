package com.sd.lib.cache.handler;

import com.sd.lib.cache.Disk;
import com.sd.lib.cache.DiskInfo;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * 缓存可以和byte互相转换的处理类
 */
public abstract class ByteConverterHandler<T> extends BaseHandler<T>
{
    public ByteConverterHandler(DiskInfo diskInfo)
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
            out = new BufferedOutputStream(new FileOutputStream(file));
            out.write(dataWithTag);
            out.flush();
            return true;
        } catch (Exception e)
        {
            getDiskInfo().getExceptionHandler().onException(e);
            return false;
        } finally
        {
            closeQuietly(out);
        }
    }

    @Override
    protected final T getCacheImpl(String key, Class clazz, File file)
    {
        byte[] data = null;
        InputStream in = null;
        try
        {
            in = new BufferedInputStream(new FileInputStream(file));
            data = new byte[in.available()];
            in.read(data);
        } catch (Exception e)
        {
            getDiskInfo().getExceptionHandler().onException(e);
            return null;
        } finally
        {
            closeQuietly(in);
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

        return byteToValue(data, clazz);
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
}
