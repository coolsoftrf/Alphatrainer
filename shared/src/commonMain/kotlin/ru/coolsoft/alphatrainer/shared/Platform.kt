package ru.coolsoft.alphatrainer.shared

interface MultiplatformLogger {
    fun d(tag: String?, message: String)
    fun i(tag: String?, message: String)
    fun e(tag: String?, message: String)
}

const val DATABASE_FILENAME = "Trainer.db"