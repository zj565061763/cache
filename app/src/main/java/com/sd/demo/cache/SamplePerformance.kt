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
            testPerformancePut()
        }

        _binding.btnGet.setOnClickListener {
            testPerformanceGet()
        }
    }
}

private fun testPerformancePut(
    cache: Cache = FCache.get(),
    repeat: Int = 100,
) {
    val content = "1".repeat(10 * 1024)
    measureTime {
        repeat(repeat) { index ->
            cache.cString().put(index.toString(), content)
        }
    }.let {
        logMsg { "put time:${it.inWholeMilliseconds}" }
    }
}

private fun testPerformanceGet(
    cache: Cache = FCache.get(),
    repeat: Int = 100,
) {
    measureTime {
        repeat(repeat) { index ->
            cache.cString().get(index.toString())
        }
    }.let {
        logMsg { "get time:${it.inWholeMilliseconds}" }
    }
}
