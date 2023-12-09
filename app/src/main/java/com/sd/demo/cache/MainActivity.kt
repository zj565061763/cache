package com.sd.demo.cache

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.sd.demo.cache.databinding.ActivityMainBinding
import com.sd.lib.cache.fCacheBoolean
import com.sd.lib.cache.fCacheDouble
import com.sd.lib.cache.fCacheFloat
import com.sd.lib.cache.fCacheInt
import com.sd.lib.cache.fCacheLong
import com.sd.lib.cache.fCacheObject
import com.sd.lib.cache.fCacheObjectMulti
import com.sd.lib.cache.fCacheString

private const val Key = "key"

class MainActivity : AppCompatActivity() {
    private val _binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(_binding.root)

        _binding.btnPut.setOnClickListener {
            putData()
        }

        _binding.btnGet.setOnClickListener {
            getData()
        }

        _binding.btnRemove.setOnClickListener {
            removeData()
        }
    }


    private fun putData() {
        fCacheInt.put(Key, 1)
        fCacheLong.put(Key, 22L)
        fCacheFloat.put(Key, 333.333f)
        fCacheDouble.put(Key, 4444.4444)
        fCacheBoolean.put(Key, true)
        fCacheString.put(Key, "hello String")

        val model = TestModel()
        fCacheObject<TestModel>().put(model)
        fCacheObjectMulti<TestModel>().put(Key, model)
        fCacheObjectMulti<TestModel>().put(Key + Key, model)
    }

    private fun getData() {
        logMsg { "cacheInt:" + fCacheInt.get(Key) }
        logMsg { "cacheLong:" + fCacheLong.get(Key) }
        logMsg { "cacheFloat:" + fCacheFloat.get(Key) }
        logMsg { "cacheDouble:" + fCacheDouble.get(Key) }
        logMsg { "cacheBoolean:" + fCacheBoolean.get(Key) }
        logMsg { "cacheString:" + fCacheString.get(Key) }
        logMsg { "objectSingle:" + fCacheObject<TestModel>().get() }
        logMsg { "objectMulti:" + fCacheObjectMulti<TestModel>().get(Key) }
        logMsg { "objectMulti:" + fCacheObjectMulti<TestModel>().get(Key + Key) }
    }

    private fun removeData() {
        fCacheInt.remove(Key)
        fCacheLong.remove(Key)
        fCacheFloat.remove(Key)
        fCacheDouble.remove(Key)
        fCacheBoolean.remove(Key)
        fCacheString.remove(Key)
        fCacheObject<TestModel>().remove()
        fCacheObjectMulti<TestModel>().remove(Key)
        fCacheObjectMulti<TestModel>().remove(Key + Key)
    }
}

inline fun logMsg(block: () -> String) {
    Log.i("cache-demo", block())
}