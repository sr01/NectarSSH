package com.rosi.nectarssh.data

data class ParsedSshHost(
    val nickname: String,
    val hostname: String,
    val port: Int = 22,
    val user: String? = null,
    val localForwards: List<ParsedLocalForward> = emptyList()
)

data class ParsedLocalForward(
    val localPort: Int,
    val remoteHost: String,
    val remotePort: Int
)

object SshConfigParser {

    fun parse(input: String): List<ParsedSshHost> {
        val hosts = mutableListOf<ParsedSshHost>()
        var currentNickname: String? = null
        var hostname: String? = null
        var port = 22
        var user: String? = null
        var forwards = mutableListOf<ParsedLocalForward>()

        fun flushHost() {
            if (currentNickname != null && hostname != null) {
                hosts.add(
                    ParsedSshHost(
                        nickname = currentNickname!!,
                        hostname = hostname!!,
                        port = port,
                        user = user,
                        localForwards = forwards.toList()
                    )
                )
            } else if (currentNickname == null && forwards.isNotEmpty()) {
                hosts.add(
                    ParsedSshHost(
                        nickname = hostname ?: "Imported",
                        hostname = hostname ?: "",
                        port = port,
                        user = user,
                        localForwards = forwards.toList()
                    )
                )
            }
            currentNickname = null
            hostname = null
            port = 22
            user = null
            forwards = mutableListOf()
        }

        for (rawLine in input.lines()) {
            val line = rawLine.trim()
            if (line.isEmpty() || line.startsWith("#")) continue

            val parts = line.split("\\s+".toRegex(), limit = 2)
            if (parts.size < 2) continue

            val directive = parts[0].lowercase()
            val value = parts[1]

            when (directive) {
                "host" -> {
                    flushHost()
                    currentNickname = value
                }
                "hostname" -> hostname = value
                "port" -> port = value.toIntOrNull() ?: 22
                "user" -> user = value
                "localforward" -> {
                    parseLocalForward(value)?.let { forwards.add(it) }
                }
            }
        }

        flushHost()

        if (hosts.isEmpty()) {
            val standaloneForwards = mutableListOf<ParsedLocalForward>()
            for (rawLine in input.lines()) {
                val line = rawLine.trim()
                if (line.isEmpty()) continue
                val forward = parseLocalForwardLine(line)
                if (forward != null) standaloneForwards.add(forward)
            }
            if (standaloneForwards.isNotEmpty()) {
                hosts.add(
                    ParsedSshHost(
                        nickname = "Imported",
                        hostname = "",
                        port = 22,
                        user = null,
                        localForwards = standaloneForwards
                    )
                )
            }
        }

        return hosts
    }

    private fun parseLocalForward(value: String): ParsedLocalForward? {
        // Format: <localPort> <remoteHost>:<remotePort>
        // Or: <bindAddress>:<localPort> <remoteHost>:<remotePort>
        val parts = value.trim().split("\\s+".toRegex(), limit = 2)
        if (parts.size != 2) return null

        val localPort = parts[0].toIntOrNull()
            ?: parts[0].substringAfterLast(":").toIntOrNull()
            ?: return null

        val remote = parts[1]
        val lastColon = remote.lastIndexOf(':')
        if (lastColon <= 0) return null

        val remoteHost = remote.substring(0, lastColon)
        val remotePort = remote.substring(lastColon + 1).toIntOrNull() ?: return null

        return ParsedLocalForward(localPort, remoteHost, remotePort)
    }

    private fun parseLocalForwardLine(line: String): ParsedLocalForward? {
        val trimmed = if (line.lowercase().startsWith("localforward")) {
            line.substringAfter(" ").trim()
        } else {
            line
        }
        return parseLocalForward(trimmed)
    }
}
