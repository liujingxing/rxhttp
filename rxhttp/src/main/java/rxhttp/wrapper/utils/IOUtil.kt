@file:JvmName("IOUtil")

package rxhttp.wrapper.utils

import java.io.*

/**
 * User: ljx
 * Date: 2016/11/15
 * Time: 15:31
 */
object IOUtil {
    private const val LENGTH_BYTE = 8 * 1024 //一次性读写的字节个数，用于字节读取

    @JvmStatic
    fun copy(inStream: InputStream, outStream: OutputStream): Int {
        val buffer = ByteArray(1024 * 8)
        val bis = BufferedInputStream(inStream, 1024 * 8)
        val bos = BufferedOutputStream(outStream, 1024 * 8)
        var count = 0
        var n = 0
        try {
            while (bis.read(buffer, 0, 1024 * 8).also { n = it } != -1) {
                bos.write(buffer, 0, n)
                count += n
            }
            bos.flush()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            close(bis, bos)
        }
        return count
    }

    /**
     * 根据文件路径获取文件里面的内容
     *
     * @param filePath 文件路径
     * @return 文件里面的内容
     */
    @JvmStatic
    fun read(filePath: String): String? {
        return read(File(filePath))
    }

    /**
     * 根据文件对象获取文本文件里面的的内容
     *
     * @param file 文件对象
     * @return 文件里面的内容
     */
    @JvmStatic
    fun read(file: File): String? {
        try {
            return if (!isFile(file)) null else read(FileInputStream(file))
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * 读取输入流里面的内容，并按String类型返回结果
     *
     * @param inStream 要读取的流
     * @return 读取的结果
     */
    @JvmStatic
    fun read(inStream: InputStream): String? {
        try {
            val stringBuffer = StringBuilder()
            var length: Int
            val bytes = ByteArray(LENGTH_BYTE)
            while (inStream.read(bytes).also { length = it } != -1) {
                stringBuffer.append(String(bytes, 0, length))
            }
            return stringBuffer.toString()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            close(inStream)
        }
        return null
    }

    @JvmStatic
    @JvmOverloads
    @Throws(IOException::class)
    fun write(inStream: InputStream, path: String, append: Boolean = false, progress: ((Long) -> Unit)? = null): Boolean {
        return write(inStream, File(path), append, progress)
    }

    /**
     * 读取流里面的内容，并以文件的形式保存
     *
     * @param inStream      要读取的流
     * @param dstFile 保存的目标文件对象
     * @param append  是否追加
     * @return 是否写入成功
     * @throws IOException 写失败异常
     */
    @JvmStatic
    @JvmOverloads
    @Throws(IOException::class)
    fun write(inStream: InputStream, dstFile: File, append: Boolean = false, progress: ((Long) -> Unit)? = null): Boolean {
        val parentFile = dstFile.parentFile
        if (!parentFile.exists() && !parentFile.mkdirs()) {
            throw IOException("Directory $parentFile create fail")
        }
        return write(inStream, FileOutputStream(dstFile, append), progress)
    }

    @JvmStatic
    @Throws(IOException::class)
    fun write(inStream: InputStream?, outStream: OutputStream?, progress: ((Long) -> Unit)? = null): Boolean {
        if (inStream == null || outStream == null) {
            throw IllegalArgumentException("inStream or outStream can not be null")
        }
        return try {
            val bytes = ByteArray(LENGTH_BYTE)
            var totalReadLength: Long = 0
            var readLength: Int
            while (inStream.read(bytes, 0, bytes.size).also { readLength = it } != -1) {
                outStream.write(bytes, 0, readLength)
                progress?.apply {
                    totalReadLength += readLength;
                    invoke(totalReadLength)
                }
            }
            true
        } finally {
            close(inStream, outStream)
        }
    }

    /**
     * 判断文件对象是否是一个文件，不是或者不存在则引发非法参数异常
     *
     * @param dir 文件对象
     */
    private fun isFile(dir: File): Boolean {
        return dir.exists() && dir.isFile
    }

    @JvmStatic
    fun close(vararg closeables: Closeable?) {
        for (closeable in closeables) {
            if (closeable == null) continue
            try {
                closeable.close()
            } catch (ignored: IOException) {
            }
        }
    }
}