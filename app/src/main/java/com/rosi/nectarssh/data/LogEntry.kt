package com.rosi.nectarssh.data

data class LogEntry(
    val timestamp: Long,
    val level: LogLevel,
    val message: String
)

enum class LogLevel {
    INFO,
    WARNING,
    ERROR,
    DEBUG
}
