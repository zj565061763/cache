package com.sd.lib.cache.handler;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class Utils
{
    private Utils()
    {
    }

    public static void writeBytes(byte[] data, OutputStream outputStream) throws IOException
    {
        if (!(outputStream instanceof BufferedOutputStream))
            outputStream = new BufferedOutputStream(outputStream);

        outputStream.write(data);
        outputStream.flush();
    }

    public static byte[] readBytes(InputStream inputStream) throws IOException
    {
        if (!(inputStream instanceof BufferedInputStream))
            inputStream = new BufferedInputStream(inputStream);

        ByteArrayOutputStream out = null;
        try
        {
            out = new ByteArrayOutputStream();
            final byte[] buffer = new byte[1024];
            int length = -1;
            while (true)
            {
                length = inputStream.read(buffer);
                if (length < 0)
                    break;
                out.write(buffer, 0, length);
            }
            return out.toByteArray();
        } finally
        {
            closeQuietly(out);
        }
    }

    public static void closeQuietly(Closeable closeable)
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
