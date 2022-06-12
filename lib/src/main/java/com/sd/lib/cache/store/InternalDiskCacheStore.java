package com.sd.lib.cache.store;

import android.content.Context;

import java.io.File;

public class InternalDiskCacheStore extends SimpleDiskCacheStore {
    public InternalDiskCacheStore(Context context) {
        super(new File(context.getFilesDir(), "disk_file"));
    }
}
