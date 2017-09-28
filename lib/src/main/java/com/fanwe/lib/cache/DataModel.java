/*
 * Copyright (C) 2017 zhengjun, fanwe (http://www.fanwe.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fanwe.lib.cache;

import java.io.Serializable;


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
