package com.fanwe.www.cache;

import android.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * aes加解密工具类
 */
public class AESUtil
{

    public static final String DEFAULT_KEY = "1400009129000000";

    public static String decrypt(String content, String key)
    {
        String result = null;
        byte[] decryptResult = null;
        try
        {
            byte[] contentBytes = Base64.decode(content, 0);
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            decryptResult = cipher.doFinal(contentBytes);
            if (decryptResult != null)
            {
                result = new String(decryptResult, "UTF-8");
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return result;
    }

    public static String decrypt(String content)
    {
        return decrypt(content, DEFAULT_KEY);
    }

    public static String encrypt(String content)
    {
        return encrypt(content, DEFAULT_KEY);
    }

    public static String encrypt(String content, String key)
    {
        byte[] encryptResult = null;
        String result = null;
        try
        {
            byte[] contentBytes = content.getBytes("UTF-8");
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            encryptResult = cipher.doFinal(contentBytes);
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        if (encryptResult != null)
        {
            result = Base64.encodeToString(encryptResult, 0);
        }
        return result;
    }

}
