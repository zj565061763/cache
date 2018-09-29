package com.sd.lib.cache.handler;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class Utils
{
    private Utils()
    {
    }

    public static String MD5(String value)
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
