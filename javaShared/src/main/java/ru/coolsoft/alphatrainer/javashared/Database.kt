package ru.coolsoft.alphatrainer.javashared

import java.io.File
import java.io.InputStream
import java.security.MessageDigest


private val DATABASE_CHECKSUMS = listOf(
    "18a845ecb52627609dde5c263e320b7d", //1.2.0
    "56a5577cb00321ce4fbd9b39ceb40516", //1.2.1
    "bf39b951b2c07f051edc748a0f96144e", //1.3.0
    "a352f8bcbd745b3bc60dbc434287f79e", //1.3.1
)

val File.md5: String get() {
    val md = MessageDigest.getInstance("MD5")
    return this.inputStream().use { fis ->
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
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
