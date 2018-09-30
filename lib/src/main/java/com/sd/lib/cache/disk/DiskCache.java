package com.sd.lib.cache.disk;

import com.sd.lib.cache.Cache;

public interface DiskCache extends Cache
{
    /**
     * 返回当前目录下所有缓存文件的总大小(字节B)
     *
     * @return
     */
    long size();

    /**
     * 删除该目录以及目录下的所有缓存
     */
    void delete();
}
