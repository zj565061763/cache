package com.sd.demo.cache

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sd.demo.cache.databinding.SamplePerformanceBinding
import com.sd.lib.cache.Cache
import com.sd.lib.cache.FCache
import kotlin.time.measureTime

class SamplePerformance : AppCompatActivity() {
    private val _binding by lazy { SamplePerformanceBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(_binding.root)

        _binding.btnPut.setOnClickListener {
            testPut()
        }
        _binding.btnGet.setOnClickListener {
            testGet()
        }
    }
}

private fun testPut(
    cache: Cache = FCache.get(),
    repeat: Int = 100,
) {
    val model = TestModel()
    measureTime {
        repeat(repeat) {
            cache.single(TestModel::class.java).put(model)
        }
    }.let {
        logMsg { "put time:${it.inWholeMilliseconds}" }
    }
}

private fun testGet(
    cache: Cache = FCache.get(),
    repeat: Int = 100,
) {
    measureTime {
        repeat(repeat) { index ->
            cache.single(TestModel::class.java).get()
        }
    }.let {
        logMsg { "get time:${it.inWholeMilliseconds}" }
    }
}