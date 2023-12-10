package com.sd.demo.cache.impl

import com.sd.demo.cache.logMsg
import com.sd.lib.closeable.FAutoCloseFactory
import java.io.File
import java.io.InputStream
import java.io.OutputStream

interface FileResource : AutoCloseable {
    fun write(data: ByteArray)

    fun read(): ByteArray?

    fun size(): Long

    fun delete()

    companion object {
        private val _factory = FAutoCloseFactory(FileResource::class.java)

        fun create(file: File): FileResource {
            val key = file.absolutePath
            return _factory.create(key) { FileResourceImpl(file) }
        }
    }
}

private class FileResourceImpl(
    private val file: File,
) : FileResource {

    private var _output: OutputStream? = null
    private var _input: InputStream? = null

    private fun getOutput(): OutputStream {
        return _output ?: kotlin.run {
            file.fCreateFile()
            file.outputStream().buffered().also {
                _output = it
            }
        }
    }

    private fun getInput(): InputStream {
        return _input ?: kotlin.run {
            file.fCreateFile()
            file.inputStream().buffered().also {
                _input = it
            }
        }
    }

    override fun write(data: ByteArray) {
        getOutput().run {
            write(data)
            flush()
        }
    }

    override fun read(): ByteArray? {
        return if (_input != null || file.isFile) {
            getInput().readBytes()
        } else {
            null
        }
    }

    override fun size(): Long {
        return file.length()
    }

    override fun delete() {
        close()
        file.deleteRecursively()
    }

    override fun close() {
        logMsg { "file close ${file.absolutePath}" }
        try {
            _input?.close()
        } finally {
            _input = null
        }
        try {
            _output?.close()
        } finally {
            _output = null
        }
    }
}

private fun File?.fCreateFile(): Boolean {
    if (this == null) return false
    if (this.isFile) return true
    if (this.isDirectory) this.deleteRecursively()
    return this.parentFile.fMakeDirs() && this.createNewFile()
}

private fun File?.fMakeDirs(): Boolean {
    if (this == null) return false
    if (this.isDirectory) return true
    if (this.isFile) this.delete()
    return this.mkdirs()
}