package com.sd.lib.cache.store;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 文件缓存实现类
 */
public class SimpleDiskCacheStore extends DiskCacheStore
{
    public SimpleDiskCacheStore(File directory)
    {
        super(directory);
    }

    @Override
    protected boolean putCacheImpl(String key, byte[] value, File file) throws Exception
    {
        OutputStream out = null;
        try
        {
            out = new BufferedOutputStream(new FileOutputStream(file));
            out.write(value);
            out.flush();
            return true;
        } finally
        {
            closeQuietly(out);
        }
    }

    @Override
    protected byte[] getCacheImpl(String key, Class<?> clazz, File file) throws Exception
    {
        InputStream in = null;
        ByteArrayOutputStream baos = null;
        try
        {
            in = new BufferedInputStream(new FileInputStream(file));
            baos = new ByteArrayOutputStream();

            final byte[] buffer = new byte[1024];
            while (true)
            {
                final int length = in.read(buffer);
                if (length < 0)
                    break;

                baos.write(buffer, 0, length);
            }
            return baos.toByteArray();
        } finally
        {
            closeQuietly(baos);
            closeQuietly(in);
        }
    }
}
