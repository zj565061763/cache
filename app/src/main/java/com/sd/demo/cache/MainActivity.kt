package com.sd.demo.cache

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sd.demo.cache.ui.theme.AppTheme
import com.sd.lib.cache.Cache
import com.sd.lib.cache.CacheConfig
import com.sd.lib.cache.FCache

private const val Key = "key"

class MainActivity : ComponentActivity() {

    private val cache: Cache by lazy {
        /**
         * 默认使用内部存储目录"/data/包名/files/f_disk_cache"，可以在初始化的时候设置[CacheConfig.Builder.setCacheStore]
         */
        FCache.disk()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                Content(
                    onClickPut = {
                        putData()
                    },
                    onClickGet = {
                        getData()
                    },
                    onClickRemove = {
                        removeData()
                    },
                )
            }
        }
    }


    private fun putData() {
        cache.cacheInteger().put(Key, 1)
        cache.cacheLong().put(Key, 22L)
        cache.cacheFloat().put(Key, 333.333f)
        cache.cacheDouble().put(Key, 4444.4444)
        cache.cacheBoolean().put(Key, true)
        cache.cacheString().put(Key, "hello String")

        val model = TestModel()
        cache.cacheObject().put(model)
        cache.cacheMultiObject(TestModel::class.java).put(Key, model)
        cache.cacheMultiObject(TestModel::class.java).put(Key + Key, model)
    }

    private fun getData() {
        logMsg { "cacheInteger:" + cache.cacheInteger().get(Key) }
        logMsg { "cacheLong:" + cache.cacheLong().get(Key) }
        logMsg { "cacheFloat:" + cache.cacheFloat().get(Key) }
        logMsg { "cacheDouble:" + cache.cacheDouble().get(Key) }
        logMsg { "cacheBoolean:" + cache.cacheBoolean().get(Key) }
        logMsg { "cacheString:" + cache.cacheString().get(Key) }
        logMsg { "cacheObject:" + cache.cacheObject().get(TestModel::class.java) }
        logMsg { "cacheMultiObject:" + cache.cacheMultiObject(TestModel::class.java).get(Key) }
        logMsg { "cacheMultiObject:" + cache.cacheMultiObject(TestModel::class.java).get(Key + Key) }
    }

    private fun removeData() {
        cache.cacheInteger().remove(Key)
        cache.cacheLong().remove(Key)
        cache.cacheFloat().remove(Key)
        cache.cacheDouble().remove(Key)
        cache.cacheBoolean().remove(Key)
        cache.cacheString().remove(Key)
        cache.cacheObject().remove(TestModel::class.java)
        cache.cacheMultiObject(TestModel::class.java).remove(Key)
        cache.cacheMultiObject(TestModel::class.java).remove(Key + Key)
    }
}

@Composable
private fun Content(
    onClickPut: () -> Unit,
    onClickGet: () -> Unit,
    onClickRemove: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Button(
            onClick = onClickPut
        ) {
            Text(text = "put")
        }

        Button(
            onClick = onClickGet
        ) {
            Text(text = "get")
        }

        Button(
            onClick = onClickRemove
        ) {
            Text(text = "remove")
        }
    }
}

inline fun logMsg(block: () -> String) {
    Log.i("cache-demo", block())
}