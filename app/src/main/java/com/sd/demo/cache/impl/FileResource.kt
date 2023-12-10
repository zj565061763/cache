package com.sd.demo.cache.impl

import com.sd.demo.cache.logMsg
import com.sd.lib.closeable.FCloseableFactory
import java.io.File
import java.io.RandomAccessFile

interface FileResource : AutoCloseable {
    fun write(data: ByteArray)

    fun read(): ByteArray

    fun size(): Long

    fun delete()

    companion object {
        private val _factory = FCloseableFactory(FileResource::class.java)

        fun create(file: File): FileResource {
            val key = file.absolutePath
            return _factory.create(key) { FileResourceImpl(file) }
        }
    }
}

private class FileResourceImpl(
    private val file: File,
) : FileResource {

    private var _raf: RandomAccessFile? = null

    private fun getRaf(): RandomAccessFile {
        return _raf ?: kotlin.run {
            logMsg { "file open ${file.absolutePath}" }
            file.fCreateFile()
            RandomAccessFile(file, "rw").also {
                _raf = it
            }
        }
    }

    override fun write(data: ByteArray) {
        getRaf().write(data)
    }

    override fun read(): ByteArray {
        val buffer = ByteArray(size().toInt())
        getRaf().readFully(buffer)
        return buffer
    }

    override fun size(): Long {
        return file.length()
    }

    override fun delete() {
        file.deleteRecursively()
    }

    override fun close() {
        try {
            _raf?.close()
        } finally {
            _raf = null
            logMsg { "file close ${file.absolutePath}" }
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