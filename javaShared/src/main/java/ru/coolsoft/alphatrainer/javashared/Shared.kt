package ru.coolsoft.alphatrainer.javashared

import java.io.File
import java.io.InputStream
import java.security.MessageDigest


private val DATABASE_CHECKSUMS = listOf(
    "18a845ecb52627609dde5c263e320b7d", //1.2.0
    "56a5577cb00321ce4fbd9b39ceb40516" //1.2.1
)

private val File.md5: String get() {
    val md = MessageDigest.getInstance("MD5")
    return this.inputStream().use { fis ->
        val buffer = ByteArray(8192)
        generateSequence {
            when (val bytesRead = fis.read(buffer)) {
                -1 -> null
                else -> bytesRead
            }
        }.forEach { bytesRead -> md.update(buffer, 0, bytesRead) }
        md.digest().joinToString("") { "%02x".format(it) }
    }
}

fun File.assertDatabase(inputStream: InputStream?) {
    if (!exists() || DATABASE_CHECKSUMS.contains(md5)) outputStream().use { outStream ->
        inputStream.use { inStream ->
            inStream!!.copyTo(outStream)
        }
    }
}
