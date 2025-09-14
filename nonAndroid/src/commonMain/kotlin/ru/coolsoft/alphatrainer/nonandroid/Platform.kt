package ru.coolsoft.alphatrainer.nonandroid

import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import ru.coolsoft.alphatrainer.shared.MultiplatformLogger
import kotlin.time.Clock.System.now
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun systemLogger(): MultiplatformLogger = object : MultiplatformLogger {
    private fun log(severity:String, tag: String?, message: String){
        println("${now().format(DateTimeComponents.Formats.ISO_DATE_TIME_OFFSET)} - $tag - $severity - $message")
    }
    override fun d(tag: String?, message: String) {
        log("DEBUG", tag, message)
    }

    override fun i(tag: String?, message: String) {
        log("INFO", tag, message)
    }

    override fun e(tag: String?, message: String) {
        log("ERROR", tag, message)
    }
}
