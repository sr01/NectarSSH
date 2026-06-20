package com.rosi.nectarssh.ui.connection

import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.termux.terminal.TerminalSession
import com.termux.terminal.TerminalSessionClient
import com.termux.view.TerminalView
import com.termux.view.TerminalViewClient
import java.io.InputStream
import java.io.OutputStream

@Composable
fun TerminalScreen(
    inputStream: InputStream,
    outputStream: OutputStream,
    onResize: ((cols: Int, rows: Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var terminalViewRef by remember { mutableStateOf<TerminalView?>(null) }

    val sessionClient = remember {
        object : TerminalSessionClient {
            override fun onTextChanged(changedSession: TerminalSession) {
                terminalViewRef?.invalidate()
            }
            override fun onTitleChanged(changedSession: TerminalSession) {}
            override fun onSessionFinished(finishedSession: TerminalSession) {}
            override fun onCopyTextToClipboard(session: TerminalSession, text: String?) {}
            override fun onPasteTextFromClipboard(session: TerminalSession?) {}
            override fun onBell(session: TerminalSession) {}
            override fun onColorsChanged(session: TerminalSession) {}
            override fun onTerminalCursorStateChange(state: Boolean) {}
            override fun setTerminalShellPid(session: TerminalSession, pid: Int) {}
            override fun getTerminalCursorStyle(): Int = 0
            override fun logError(tag: String?, message: String?) { Log.e(tag ?: "Terminal", message ?: "") }
            override fun logWarn(tag: String?, message: String?) { Log.w(tag ?: "Terminal", message ?: "") }
            override fun logInfo(tag: String?, message: String?) { Log.i(tag ?: "Terminal", message ?: "") }
            override fun logDebug(tag: String?, message: String?) { Log.d(tag ?: "Terminal", message ?: "") }
            override fun logVerbose(tag: String?, message: String?) { Log.v(tag ?: "Terminal", message ?: "") }
            override fun logStackTraceWithMessage(tag: String?, message: String?, e: Exception?) {
                Log.e(tag ?: "Terminal", message, e)
            }
            override fun logStackTrace(tag: String?, e: Exception?) {
                Log.e(tag ?: "Terminal", "Exception", e)
            }
        }
    }

    val terminalSession = remember {
        TerminalSession(inputStream, outputStream, null, sessionClient).also { session ->
            if (onResize != null) {
                session.setOnResizeListener { cols, rows -> onResize(cols, rows) }
            }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.Black)
        ) {
            AndroidView(
                factory = { ctx ->
                    TerminalView(ctx, null).apply {
                        val scaledSize = (14 * ctx.resources.displayMetrics.scaledDensity).toInt()
                        setTextSize(scaledSize)
                        setTerminalViewClient(object : TerminalViewClient {
                            override fun onScale(scale: Float): Float = 1.0f
                            override fun onSingleTapUp(e: MotionEvent?) {
                                val imm = ctx.getSystemService(InputMethodManager::class.java)
                                imm?.showSoftInput(this@apply, InputMethodManager.SHOW_IMPLICIT)
                            }
                            override fun shouldBackButtonBeMappedToEscape(): Boolean = false
                            override fun shouldEnforceCharBasedInput(): Boolean = true
                            override fun shouldUseCtrlSpaceWorkaround(): Boolean = false
                            override fun isTerminalViewSelected(): Boolean = true
                            override fun copyModeChanged(copyMode: Boolean) {}
                            override fun onKeyDown(keyCode: Int, e: KeyEvent?, session: TerminalSession?): Boolean = false
                            override fun onKeyUp(keyCode: Int, e: KeyEvent?): Boolean = false
                            override fun onLongPress(event: MotionEvent?): Boolean = false
                            override fun readControlKey(): Boolean = false
                            override fun readAltKey(): Boolean = false
                            override fun readShiftKey(): Boolean = false
                            override fun readFnKey(): Boolean = false
                            override fun onCodePoint(codePoint: Int, ctrlDown: Boolean, session: TerminalSession?): Boolean = false
                            override fun onEmulatorSet() {}
                            override fun logError(tag: String?, message: String?) {}
                            override fun logWarn(tag: String?, message: String?) {}
                            override fun logInfo(tag: String?, message: String?) {}
                            override fun logDebug(tag: String?, message: String?) {}
                            override fun logVerbose(tag: String?, message: String?) {}
                            override fun logStackTraceWithMessage(tag: String?, message: String?, e: Exception?) {}
                            override fun logStackTrace(tag: String?, e: Exception?) {}
                        })
                        attachSession(terminalSession)
                        terminalViewRef = this
                        post {
                            if (width > 0 && height > 0) {
                                updateSize()
                                requestFocus()
                            }
                        }
                        isFocusable = true
                        isFocusableInTouchMode = true
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        ExtraKeysRow(
            onKey = { key -> terminalSession.write(key.toByteArray(), 0, key.length) }
        )
    }
}

@Composable
fun ExtraKeysRow(onKey: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp)
    ) {
        val keys = listOf(
            "ESC" to "",
            "TAB" to "\t",
            "↑" to "[A",
            "↓" to "[B",
            "←" to "[D",
            "→" to "[C",
            "/" to "/",
            "|" to "|",
            "~" to "~",
            "-" to "-"
        )

        keys.forEach { (label, value) ->
            TextButton(
                onClick = { onKey(value) },
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
            ) {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }
        }
    }
}
