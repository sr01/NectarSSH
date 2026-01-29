package com.rosi.nectarssh

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.rosi.nectarssh.data.Identity
import com.rosi.nectarssh.data.IdentityStorage
import com.rosi.nectarssh.ui.theme.NectarSSHTheme
import java.util.UUID

class IdentityActivity : ComponentActivity() {
    companion object {
        const val EXTRA_IDENTITY_ID = "identity_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val storage = IdentityStorage(this)
        val identityId = intent.getStringExtra(EXTRA_IDENTITY_ID)
        val existingIdentity = identityId?.let { storage.getIdentity(it) }

        setContent {
            NectarSSHTheme {
                NewIdentityScreen(
                    existingIdentity = existingIdentity,
                    onSave = { identity ->
                        if (existingIdentity != null) {
                            storage.updateIdentity(identity)
                        } else {
                            storage.addIdentity(identity)
                        }
                        setResult(Activity.RESULT_OK)
                        finish()
                    },
                    onCancel = {
                        setResult(Activity.RESULT_CANCELED)
                        finish()
                    },
                    onImportKey = { uri ->
                        try {
                            val path = contentResolver.openInputStream(uri)?.use { inputStream ->
                                storage.savePrivateKey(inputStream.readBytes())
                            }
                            if (path != null) {
                                Result.success(path)
                            } else {
                                Result.failure(Exception("Failed to read file"))
                            }
                        } catch (e: Exception) {
                            Result.failure(e)
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewIdentityScreen(
    existingIdentity: Identity? = null,
    onSave: (Identity) -> Unit,
    onCancel: () -> Unit,
    onImportKey: (Uri) -> Result<String>
) {
    val context = LocalContext.current
    val isEditMode = existingIdentity != null
    var nickname by remember { mutableStateOf(existingIdentity?.nickname ?: "") }
    var username by remember { mutableStateOf(existingIdentity?.username ?: "") }
    var password by remember { mutableStateOf<String?>(existingIdentity?.password) }
    var privateKeyPath by remember { mutableStateOf<String?>(existingIdentity?.privateKeyPath) }
    var privateKeyPassphrase by remember { mutableStateOf<String?>(existingIdentity?.privateKeyPassphrase) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showPassphraseDialog by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val result = onImportKey(it)
            result.onSuccess { path ->
                privateKeyPath = path
                Toast.makeText(context, "Import successful", Toast.LENGTH_SHORT).show()
            }.onFailure { exception ->
                Toast.makeText(
                    context,
                    "Import failed: ${exception.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Update Identity" else "New Identity") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cancel")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (username.isNotBlank()) {
                                val identity = Identity(
                                    id = existingIdentity?.id ?: UUID.randomUUID().toString(),
                                    nickname = nickname.ifBlank { username },
                                    username = username,
                                    password = password,
                                    privateKeyPath = privateKeyPath,
                                    privateKeyPassphrase = privateKeyPassphrase
                                )
                                onSave(identity)
                            }
                        },
                        enabled = username.isNotBlank()
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = nickname,
                onValueChange = { nickname = it },
                label = { Text("Nickname (Optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth()
            )

            // Password field with SET button
            OutlinedTextField(
                value = password ?: "",
                onValueChange = { },
                label = { Text("Password") },
                readOnly = true,
                enabled = false,
                visualTransformation = PasswordVisualTransformation(),
                trailingIcon = {
                    Button(onClick = { showPasswordDialog = true }) {
                        Text("SET")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Private Key field with IMPORT button
            OutlinedTextField(
                value = privateKeyPath?.let { "Key imported" } ?: "",
                onValueChange = { },
                label = { Text("Private Key") },
                readOnly = true,
                enabled = false,
                trailingIcon = {
                    Button(onClick = { filePickerLauncher.launch("*/*") }) {
                        Text("IMPORT")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Private Key Passphrase field (only show when key is imported)
            if (privateKeyPath != null) {
                OutlinedTextField(
                    value = privateKeyPassphrase ?: "",
                    onValueChange = { },
                    label = { Text("Private Key Passphrase") },
                    readOnly = true,
                    enabled = false,
                    visualTransformation = PasswordVisualTransformation(),
                    trailingIcon = {
                        Button(onClick = { showPassphraseDialog = true }) {
                            Text("SET")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    if (showPasswordDialog) {
        PasswordDialog(
            currentPassword = password ?: "",
            onDismiss = { showPasswordDialog = false },
            onConfirm = { newPassword ->
                password = newPassword
                showPasswordDialog = false
            }
        )
    }

    if (showPassphraseDialog) {
        PassphraseDialog(
            currentPassphrase = privateKeyPassphrase ?: "",
            onDismiss = { showPassphraseDialog = false },
            onConfirm = { newPassphrase ->
                privateKeyPassphrase = newPassphrase
                showPassphraseDialog = false
            }
        )
    }
}

@Composable
fun PasswordDialog(
    currentPassword: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var passwordText by remember { mutableStateOf(currentPassword) }
    var showPassword by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Password") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = passwordText,
                    onValueChange = { passwordText = it },
                    label = { Text("Password") },
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Checkbox(
                        checked = showPassword,
                        onCheckedChange = { showPassword = it }
                    )
                    Text(
                        text = "Show password",
                        modifier = Modifier.padding(start = 8.dp, top = 12.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(passwordText) }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun PassphraseDialog(
    currentPassphrase: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var passphraseText by remember { mutableStateOf(currentPassphrase) }
    var showPassphrase by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Private Key Passphrase") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = passphraseText,
                    onValueChange = { passphraseText = it },
                    label = { Text("Passphrase") },
                    visualTransformation = if (showPassphrase) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Checkbox(
                        checked = showPassphrase,
                        onCheckedChange = { showPassphrase = it }
                    )
                    Text(
                        text = "Show passphrase",
                        modifier = Modifier.padding(start = 8.dp, top = 12.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(passphraseText) }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
