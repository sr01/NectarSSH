package com.rosi.nectarssh

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.rosi.nectarssh.data.ExportData
import com.rosi.nectarssh.data.ImportExportManager
import com.rosi.nectarssh.data.ImportResult
import com.rosi.nectarssh.ui.theme.NectarSSHTheme
import com.rosi.nectarssh.util.PermissionHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File

class ImportActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NectarSSHTheme {
                ImportScreen(onFinish = { finish() })
            }
        }
    }
}

enum class ImportState {
    LOADING,
    CONFIRMATION,
    IMPORTING,
    SUCCESS,
    ERROR
}

@Composable
fun ImportScreen(onFinish: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var state by remember { mutableStateOf(ImportState.LOADING) }
    var exportData by remember { mutableStateOf<ExportData?>(null) }
    var importResult by remember { mutableStateOf<ImportResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var permissionsGranted by remember { mutableStateOf(PermissionHelper.isStoragePermissionGranted(context)) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { loadDataFromUri(it, context, scope, { exportData = it }, { state = it }, { errorMessage = it }) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        permissionsGranted = result.values.all { it }
    }

    LaunchedEffect(Unit) {
        if (!permissionsGranted) {
            val permissions = PermissionHelper.getRequiredStoragePermissions()
            if (permissions.isNotEmpty()) permissionLauncher.launch(permissions) else permissionsGranted = true
        }
    }

    LaunchedEffect(permissionsGranted) {
        if (!permissionsGranted) return@LaunchedEffect
        val activity = context as ImportActivity
        val intentUri = activity.intent?.data
        val extraContent = activity.intent?.getStringExtra("IMPORT_CONTENT")

        scope.launch(Dispatchers.IO) {
            val jsonContent = when {
                intentUri != null -> {
                    readTextFromUri(context, intentUri)
                }
                extraContent != null -> {
                    when {
                        extraContent.startsWith("{") -> extraContent // Raw JSON
                        else -> {
                            // Try to treat as a path or URI string
                            val uri = try {
                                if (extraContent.startsWith("/") || extraContent.startsWith("file://")) {
                                    Uri.fromFile(File(extraContent.removePrefix("file://")))
                                } else {
                                    Uri.parse(extraContent)
                                }
                            } catch (e: Exception) {
                                null
                            }
                            
                            if (uri != null && (uri.scheme == "content" || uri.scheme == "file")) {
                                readTextFromUri(context, uri)
                            } else {
                                null
                            }
                        }
                    }
                }
                else -> null
            }

            if (jsonContent == null) {
                withContext(Dispatchers.Main) {
                    errorMessage = "No import data found. Try selecting the file manually."
                    state = ImportState.ERROR
                }
            } else {
                processJsonContent(jsonContent, { exportData = it }, { state = it }, { errorMessage = it })
            }
        }
    }

    Scaffold { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (state) {
                ImportState.LOADING -> LoadingScreen()
                ImportState.CONFIRMATION -> exportData?.let { data ->
                    ImportConfirmationScreen(
                        exportData = data,
                        onConfirm = {
                            state = ImportState.IMPORTING
                            scope.launch(Dispatchers.IO) {
                                val result = ImportExportManager.importData(context, Json.encodeToString(ExportData.serializer(), data))
                                withContext(Dispatchers.Main) {
                                    importResult = result
                                    state = if (result.success) ImportState.SUCCESS else ImportState.ERROR
                                }
                            }
                        },
                        onCancel = onFinish
                    )
                }
                ImportState.IMPORTING -> LoadingScreen(message = "Importing...")
                ImportState.SUCCESS -> importResult?.let { ImportSuccessScreen(result = it, onClose = onFinish) }
                ImportState.ERROR -> ErrorScreen(
                    message = errorMessage ?: "Access Denied. Ensure the app has storage permissions or select the file manually.",
                    onPickFile = { filePickerLauncher.launch(arrayOf("application/json")) },
                    onClose = onFinish
                )
            }
        }
    }
}

private fun readTextFromUri(context: android.content.Context, uri: Uri): String? {
    return try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            inputStream.bufferedReader().use { it.readText() }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun loadDataFromUri(uri: Uri, context: android.content.Context, scope: kotlinx.coroutines.CoroutineScope, onData: (ExportData) -> Unit, onState: (ImportState) -> Unit, onError: (String) -> Unit) {
    scope.launch(Dispatchers.IO) {
        val content = readTextFromUri(context, uri)
        processJsonContent(content, onData, onState, onError)
    }
}

private suspend fun processJsonContent(content: String?, onData: (ExportData) -> Unit, onState: (ImportState) -> Unit, onError: (String) -> Unit) {
    withContext(Dispatchers.Main) {
        if (content == null) {
            onError("Could not read file. Access may be restricted.")
            onState(ImportState.ERROR)
            return@withContext
        }
        val validation = ImportExportManager.validateImportData(content)
        if (validation.success) {
            try {
                onData(Json.decodeFromString<ExportData>(content))
                onState(ImportState.CONFIRMATION)
            } catch (e: Exception) {
                onError("Malformed JSON: ${e.message}")
                onState(ImportState.ERROR)
            }
        } else {
            onError(validation.message)
            onState(ImportState.ERROR)
        }
    }
}

@Composable
fun LoadingScreen(message: String = "Loading...") {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(message)
        }
    }
}

@Composable
fun ImportConfirmationScreen(exportData: ExportData, onConfirm: () -> Unit, onCancel: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Import Configuration", style = MaterialTheme.typography.headlineMedium)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Summary:", style = MaterialTheme.typography.titleMedium)
                Text("Identities: ${exportData.identities.size}")
                Text("Connections: ${exportData.connections.size}")
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onCancel, modifier = Modifier.weight(1f)) { Text("Cancel") }
            Button(onClick = onConfirm, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Import") }
        }
    }
}

@Composable
fun ImportSuccessScreen(result: ImportResult, onClose: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Success!", style = MaterialTheme.typography.headlineMedium)
        Text("Imported ${result.connectionCount} connections.")
        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = onClose, modifier = Modifier.fillMaxWidth()) { Text("Close") }
    }
}

@Composable
fun ErrorScreen(message: String, onPickFile: () -> Unit, onClose: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
        Text("Import Failed", style = MaterialTheme.typography.headlineMedium)
        Text(message, color = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onPickFile, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.Info, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Select File Manually")
        }
        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = onClose, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) { Text("Cancel") }
    }
}
