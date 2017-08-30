package com.fanwe.library.cache;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/8/29.
 */

class DataModel implements Serializable
{
    static final long serialVersionUID = 0L;

    private String data;
    private boolean encrypt;

    public String getData()
    {
        return data;
    }

    public void setData(String data)
    {
        this.data = data;
    }

    public boolean isEncrypt()
    {
        return encrypt;
    }

    public void setEncrypt(boolean encrypt)
    {
        this.encrypt = encrypt;
    }

    /**
     * 加密
     *
     * @param converter
     */
    public void encryptIfNeed(IEncryptConverter converter)
    {
        if (encrypt && converter != null)
        {
            data = converter.encrypt(data);
        }
    }

    /**
     * 解密
     *
     * @param converter
     */
    public void decryptIfNeed(IEncryptConverter converter)
    {
        if (encrypt && converter != null)
        {
            data = converter.decrypt(data);
        }
    }
}
