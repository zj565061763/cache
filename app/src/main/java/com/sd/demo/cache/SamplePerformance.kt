package com.sd.demo.cache

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sd.demo.cache.databinding.SamplePerformanceBinding
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

private fun testPut(repeat: Int = 100) {
    val cache = FCache.getDefault().single(TestModel::class.java)
    val model = TestModel()
    measureTime {
        repeat(repeat) {
            cache.put(model)
        }
    }.also {
        logMsg { "put time:${it.inWholeMilliseconds}" }
    }
}

private fun testGet(repeat: Int = 100) {
    val cache = FCache.getDefault().single(TestModel::class.java)
    measureTime {
        repeat(repeat) {
            cache.get()
        }
    }.also {
        logMsg { "get time:${it.inWholeMilliseconds}" }
    }
}