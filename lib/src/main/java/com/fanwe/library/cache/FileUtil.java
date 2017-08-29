package com.fanwe.library.cache;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

/**
 * Created by Administrator on 2017/8/29.
 */

class FileUtil
{

    public static String readString(InputStream in) throws IOException
    {
        if (!(in instanceof BufferedInputStream))
        {
            in = new BufferedInputStream(in);
        }
        Reader reader = new InputStreamReader(in, "UTF-8");
        StringBuilder sb = new StringBuilder();
        char[] buf = new char[1024];
        int len = -1;
        while ((len = reader.read(buf)) >= 0)
        {
            sb.append(buf, 0, len);
        }
        return sb.toString();
    }

    public static void writeString(OutputStream out, String string) throws IOException
    {
        Writer writer = new OutputStreamWriter(out, "UTF-8");
        writer.write(string);
        writer.flush();
    }

    public static boolean deleteFileOrDir(File file)
    {
        if (file == null || !file.exists())
        {
            return true;
        }
        if (file.isFile())
        {
            return file.delete();
        }
        File[] files = file.listFiles();
        if (files != null)
        {
            for (File item : files)
            {
                deleteFileOrDir(item);
            }
        }
        return file.delete();
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
