package com.rosi.nectarssh.ui.connection

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rosi.nectarssh.data.ConnectionStatus
import com.rosi.nectarssh.data.LogEntry
import com.rosi.nectarssh.data.LogLevel
import com.rosi.nectarssh.data.PassphraseRequest
import com.rosi.nectarssh.data.PassphraseResponse
import com.rosi.nectarssh.data.SessionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharedFlow
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionLogScreen(
    sessionId: String,
    getSessionState: () -> SessionState?,
    getSessionStateFlow: () -> SharedFlow<SessionState>?,
    getLogFlow: () -> SharedFlow<LogEntry>?,
    getPassphraseRequestFlow: () -> SharedFlow<PassphraseRequest>?,
    onPassphraseResponse: (PassphraseResponse) -> Unit,
    onDisconnect: () -> Unit,
    onKeepRunning: () -> Unit,
    onBack: () -> Unit
) {
    var sessionState by remember { mutableStateOf<SessionState?>(null) }
    var loadingFailed by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    var showDisconnectDialog by remember { mutableStateOf(false) }
    var showPassphraseDialog by remember { mutableStateOf(false) }
    var currentPassphraseRequest by remember { mutableStateOf<PassphraseRequest?>(null) }

    // Initial load with timeout
    LaunchedEffect(sessionId) {
        var attempts = 0
        while (attempts < 20 && sessionState == null && !loadingFailed) {
            val initialState = getSessionState()
            if (initialState != null) {
                sessionState = initialState
                if (initialState.logs.isNotEmpty()) {
                    listState.scrollToItem(initialState.logs.size - 1)
                }
                break
            }
            delay(100)
            attempts++
        }

        // If still null after 2 seconds, mark as failed
        if (sessionState == null) {
            loadingFailed = true
        }
    }

    // Collect session state updates via Flow
    LaunchedEffect(sessionId) {
        val stateFlow = getSessionStateFlow()
        stateFlow?.collect { newState ->
            loadingFailed = false // Reset failed state if we get updates
            val oldLogSize = sessionState?.logs?.size ?: 0
            sessionState = newState

            // Auto-finish activity if disconnected from external source (like notification)
            if (newState.status == ConnectionStatus.DISCONNECTED) {
                onBack()
            }

            if (newState.logs.size > oldLogSize) {
                listState.animateScrollToItem(newState.logs.size - 1)
            }
        }
    }

    // Listen for passphrase requests
    LaunchedEffect(sessionId) {
        val passphraseFlow = getPassphraseRequestFlow()
        passphraseFlow?.collect { request ->
            if (request.sessionId == sessionId) {
                currentPassphraseRequest = request
                showPassphraseDialog = true
            }
        }
    }

    // Handle back button
    BackHandler {
        if (sessionState?.status == ConnectionStatus.CONNECTED || sessionState?.status == ConnectionStatus.CONNECTING) {
            showDisconnectDialog = true
        } else {
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        val nickname = sessionState?.nickname ?: "Connection"
                        Text(nickname)
                        Text(
                            text = when {
                                loadingFailed -> "Session not found"
                                sessionState?.status == ConnectionStatus.CONNECTING -> "Connecting..."
                                sessionState?.status == ConnectionStatus.CONNECTED -> "Connected"
                                sessionState?.status == ConnectionStatus.DISCONNECTING -> "Disconnecting..."
                                sessionState?.status == ConnectionStatus.DISCONNECTED -> "Disconnected"
                                sessionState?.status == ConnectionStatus.ERROR -> "Error"
                                else -> "Loading..."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = when {
                                loadingFailed -> Color.Red
                                sessionState?.status == ConnectionStatus.CONNECTED -> Color.Green
                                sessionState?.status == ConnectionStatus.ERROR -> Color.Red
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (sessionState?.status == ConnectionStatus.CONNECTED || sessionState?.status == ConnectionStatus.CONNECTING) {
                            showDisconnectDialog = true
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        val currentLogs = sessionState?.logs ?: emptyList()

        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .background(Color.Black)
        ) {
            if (loadingFailed && currentLogs.isEmpty()) {
                // Show error message when session not found
                Text(
                    text = "Session not found. It may have already ended.",
                    color = Color.Red,
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    state = listState
                ) {
                    items(currentLogs) { log ->
                        LogEntryItem(log)
                    }
                }
            }
        }
    }

    if (showDisconnectDialog) {
        DisconnectDialog(
            onDisconnect = {
                showDisconnectDialog = false
                onDisconnect()
            },
            onKeepRunning = {
                showDisconnectDialog = false
                onKeepRunning()
            },
            onDismiss = {
                showDisconnectDialog = false
            }
        )
    }

    if (showPassphraseDialog && currentPassphraseRequest != null) {
        PassphraseDialog(
            onProvidePassphrase = { passphrase, savePassphrase ->
                showPassphraseDialog = false
                onPassphraseResponse(
                    PassphraseResponse(
                        sessionId = currentPassphraseRequest!!.sessionId,
                        passphrase = passphrase,
                        savePassphrase = savePassphrase
                    )
                )
                currentPassphraseRequest = null
            },
            onCancel = {
                showPassphraseDialog = false
                onPassphraseResponse(
                    PassphraseResponse(
                        sessionId = currentPassphraseRequest!!.sessionId,
                        passphrase = null,
                        savePassphrase = false
                    )
                )
                currentPassphraseRequest = null
            }
        )
    }
}

@Composable
fun LogEntryItem(log: LogEntry) {
    val message = if (log.level == LogLevel.DEBUG) {
        // Stream output (normal)
        parseAnsi(log.message)
    } else {
        // App log (bold white for INFO, appropriate colors for WARNING/ERROR)
        buildAnnotatedString {
            val color = when (log.level) {
                LogLevel.INFO -> Color.White
                LogLevel.WARNING -> Color(0xFFFFA726) // Orange
                LogLevel.ERROR -> Color(0xFFEF5350) // Red
                else -> Color.Gray
            }
            val weight = if (log.level == LogLevel.INFO) FontWeight.Bold else FontWeight.Normal
            append(AnnotatedString(log.message, SpanStyle(color = color, fontWeight = weight)))
        }
    }

    Text(
        text = message,
        fontFamily = FontFamily.Monospace,
        style = MaterialTheme.typography.bodySmall.copy(
            lineHeight = 16.sp,
            letterSpacing = 0.sp
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

/**
 * Enhanced ANSI escape code parser for Compose AnnotatedString.
 */
fun parseAnsi(input: String): AnnotatedString {
    val cleanedInput = input.replace(Regex("""\$<\d+>"""), "")

    return buildAnnotatedString {
        var i = 0
        var currentStyle = AnsiStyle()

        while (i < cleanedInput.length) {
            val nextEscape = cleanedInput.indexOf("\u001b", i)
            if (nextEscape == -1) {
                val remaining = cleanedInput.substring(i)
                if (remaining.isNotEmpty()) {
                    append(AnnotatedString(remaining, currentStyle.toSpanStyle()))
                }
                break
            }

            if (nextEscape > i) {
                append(AnnotatedString(cleanedInput.substring(i, nextEscape), currentStyle.toSpanStyle()))
            }

            if (nextEscape + 1 < cleanedInput.length && cleanedInput[nextEscape + 1] == '[') {
                var endOfEscape = nextEscape + 2
                while (endOfEscape < cleanedInput.length) {
                    val c = cleanedInput[endOfEscape]
                    if (c.isLetter() || c == '@') break
                    endOfEscape++
                }

                if (endOfEscape >= cleanedInput.length) {
                    i = nextEscape + 1
                    continue
                }

                val command = cleanedInput[endOfEscape]
                if (command == 'm') {
                    val code = cleanedInput.substring(nextEscape + 2, endOfEscape)
                    currentStyle = applyAnsiStyle(code, currentStyle)
                }
                i = endOfEscape + 1
            } else if (nextEscape + 1 < cleanedInput.length && cleanedInput[nextEscape + 1] == ']') {
                var endOfEscape = nextEscape + 2
                while (endOfEscape < cleanedInput.length) {
                    if (cleanedInput[endOfEscape] == '\u0007') break
                    else if (endOfEscape + 1 < cleanedInput.length &&
                               cleanedInput[endOfEscape] == '\u001b' &&
                               cleanedInput[endOfEscape + 1] == '\\') {
                        endOfEscape++
                        break
                    }
                    endOfEscape++
                }
                i = if (endOfEscape < cleanedInput.length) endOfEscape + 1 else endOfEscape
            } else {
                var endOfEscape = nextEscape + 1
                if (endOfEscape < cleanedInput.length) endOfEscape++
                i = endOfEscape
            }
        }
    }
}

private data class AnsiStyle(
    val foreground: Color = Color.White,
    val background: Color? = null,
    val bold: Boolean = false,
    val dim: Boolean = false,
    val italic: Boolean = false,
    val underline: Boolean = false,
    val strikethrough: Boolean = false
) {
    fun toSpanStyle(): SpanStyle {
        val finalColor = if (dim) foreground.copy(alpha = 0.6f) else foreground
        val decoration = buildList {
            if (underline) add(TextDecoration.Underline)
            if (strikethrough) add(TextDecoration.LineThrough)
        }.let { if (it.isEmpty()) null else TextDecoration.combine(it) }

        return SpanStyle(
            color = finalColor,
            background = background ?: Color.Transparent,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            fontStyle = if (italic) FontStyle.Italic else FontStyle.Normal,
            textDecoration = decoration
        )
    }
}

private fun applyAnsiStyle(code: String, current: AnsiStyle): AnsiStyle {
    if (code.isEmpty() || code == "0") return AnsiStyle()
    val parts = code.split(';').mapNotNull { it.toIntOrNull() }
    var result = current
    var i = 0
    while (i < parts.size) {
        val n = parts[i]
        result = when (n) {
            0 -> AnsiStyle()
            1 -> result.copy(bold = true)
            2 -> result.copy(dim = true)
            3 -> result.copy(italic = true)
            4 -> result.copy(underline = true)
            9 -> result.copy(strikethrough = true)
            22 -> result.copy(bold = false, dim = false)
            23 -> result.copy(italic = false)
            24 -> result.copy(underline = false)
            29 -> result.copy(strikethrough = false)
            30 -> result.copy(foreground = Color(0xFF000000))
            31 -> result.copy(foreground = Color(0xFFCD3131))
            32 -> result.copy(foreground = Color(0xFF0DBC79))
            33 -> result.copy(foreground = Color(0xFFE5E510))
            34 -> result.copy(foreground = Color(0xFF2472C8))
            35 -> result.copy(foreground = Color(0xFFBC3FBC))
            36 -> result.copy(foreground = Color(0xFF11A8CD))
            37 -> result.copy(foreground = Color(0xFFE5E5E5))
            39 -> result.copy(foreground = Color.White)
            40 -> result.copy(background = Color(0xFF000000))
            41 -> result.copy(background = Color(0xFFCD3131))
            42 -> result.copy(background = Color(0xFF0DBC79))
            43 -> result.copy(background = Color(0xFFE5E510))
            44 -> result.copy(background = Color(0xFF2472C8))
            45 -> result.copy(background = Color(0xFFBC3FBC))
            46 -> result.copy(background = Color(0xFF11A8CD))
            47 -> result.copy(background = Color(0xFFE5E5E5))
            49 -> result.copy(background = null)
            90 -> result.copy(foreground = Color(0xFF666666))
            91 -> result.copy(foreground = Color(0xFFF14C4C))
            92 -> result.copy(foreground = Color(0xFF23D18B))
            93 -> result.copy(foreground = Color(0xFFF5F543))
            94 -> result.copy(foreground = Color(0xFF3B8EEA))
            95 -> result.copy(foreground = Color(0xFFD670D6))
            96 -> result.copy(foreground = Color(0xFF29B8DB))
            97 -> result.copy(foreground = Color(0xFFE5E5E5))
            100 -> result.copy(background = Color(0xFF666666))
            101 -> result.copy(background = Color(0xFFF14C4C))
            102 -> result.copy(background = Color(0xFF23D18B))
            103 -> result.copy(background = Color(0xFFF5F543))
            104 -> result.copy(background = Color(0xFF3B8EEA))
            105 -> result.copy(background = Color(0xFFD670D6))
            106 -> result.copy(background = Color(0xFF29B8DB))
            107 -> result.copy(background = Color(0xFFE5E5E5))
            38 -> {
                if (i + 1 < parts.size) {
                    when (parts[i + 1]) {
                        5 -> if (i + 2 < parts.size) { val res = result.copy(foreground = ansi256ToColor(parts[i + 2])); i += 2; res } else result
                        2 -> if (i + 4 < parts.size) { val res = result.copy(foreground = Color(parts[i + 2], parts[i + 3], parts[i + 4])); i += 4; res } else result
                        else -> result
                    }
                } else result
            }
            48 -> {
                if (i + 1 < parts.size) {
                    when (parts[i + 1]) {
                        5 -> if (i + 2 < parts.size) { val res = result.copy(background = ansi256ToColor(parts[i + 2])); i += 2; res } else result
                        2 -> if (i + 4 < parts.size) { val res = result.copy(background = Color(parts[i + 2], parts[i + 3], parts[i + 4])); i += 4; res } else result
                        else -> result
                    }
                } else result
            }
            else -> result
        }
        i++
    }
    return result
}

private fun ansi256ToColor(index: Int): Color {
    return when (index) {
        0 -> Color(0xFF000000)
        1 -> Color(0xFFCD3131)
        2 -> Color(0xFF0DBC79)
        3 -> Color(0xFFE5E510)
        4 -> Color(0xFF2472C8)
        5 -> Color(0xFFBC3FBC)
        6 -> Color(0xFF11A8CD)
        7 -> Color(0xFFE5E5E5)
        8 -> Color(0xFF666666)
        9 -> Color(0xFFF14C4C)
        10 -> Color(0xFF23D18B)
        11 -> Color(0xFFF5F543)
        12 -> Color(0xFF3B8EEA)
        13 -> Color(0xFFD670D6)
        14 -> Color(0xFF29B8DB)
        15 -> Color(0xFFFFFFFF)
        in 16..231 -> {
            val idx = index - 16
            Color((idx / 36) * 51, ((idx % 36) / 6) * 51, (idx % 6) * 51)
        }
        in 232..255 -> {
            val gray = 8 + (index - 232) * 10
            Color(gray, gray, gray)
        }
        else -> Color.White
    }
}

@Composable
fun PassphraseDialog(onProvidePassphrase: (String, Boolean) -> Unit, onCancel: () -> Unit) {
    var passphrase by remember { mutableStateOf("") }
    var showPassphrase by remember { mutableStateOf(false) }
    var savePassphrase by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Private Key Passphrase Required") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Enter passphrase for private key:")
                OutlinedTextField(
                    value = passphrase,
                    onValueChange = { passphrase = it },
                    label = { Text("Passphrase") },
                    visualTransformation = if (showPassphrase) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Checkbox(checked = showPassphrase, onCheckedChange = { showPassphrase = it })
                    Text("Show passphrase")
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Checkbox(checked = savePassphrase, onCheckedChange = { savePassphrase = it })
                    Text("Save passphrase")
                }
            }
        },
        confirmButton = { TextButton(onClick = { onProvidePassphrase(passphrase, savePassphrase) }, enabled = passphrase.isNotBlank()) { Text("OK") } },
        dismissButton = { TextButton(onClick = onCancel) { Text("Cancel") } }
    )
}

@Composable
fun DisconnectDialog(onDisconnect: () -> Unit, onKeepRunning: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Connection Active") },
        text = { Text("Keep SSH connection running in background?") },
        confirmButton = { TextButton(onClick = onDisconnect) { Text("Disconnect") } },
        dismissButton = { Row { TextButton(onClick = onKeepRunning) { Text("Keep Running") }; Spacer(modifier = Modifier.width(8.dp)); TextButton(onClick = onDismiss) { Text("Cancel") } } }
    )
}
