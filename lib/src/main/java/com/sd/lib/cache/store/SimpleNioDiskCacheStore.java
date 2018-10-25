package com.sd.lib.cache.store;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * 文件缓存实现类
 */
public class SimpleNioDiskCacheStore extends DiskCacheStore
{
    public SimpleNioDiskCacheStore(File directory)
    {
        super(directory);
    }

    @Override
    protected boolean putCacheImpl(String key, byte[] value, File file) throws Exception
    {
        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(file);
            final FileChannel channel = fos.getChannel();

            final ByteBuffer buffer = ByteBuffer.allocate(value.length);
            buffer.put(value);

            buffer.flip();
            while (buffer.hasRemaining())
            {
                channel.write(buffer);
            }

            return true;
        } finally
        {
            closeQuietly(fos);
        }
    }

    @Override
    protected byte[] getCacheImpl(String key, Class clazz, File file) throws Exception
    {
        ByteArrayOutputStream baos = null;
        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream(file);
            final FileChannel channel = fis.getChannel();

            baos = new ByteArrayOutputStream();

            final ByteBuffer buffer = ByteBuffer.allocate(1024);
            while (true)
            {
                buffer.clear();
                final int length = channel.read(buffer);
                if (length < 0)
                    break;

                baos.write(buffer.array(), 0, length);
            }

            return baos.toByteArray();
        } finally
        {
            closeQuietly(baos);
            closeQuietly(fis);
        }
    }
}
