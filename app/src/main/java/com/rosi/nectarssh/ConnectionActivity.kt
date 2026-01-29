package com.rosi.nectarssh

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rosi.nectarssh.data.Connection
import com.rosi.nectarssh.data.ConnectionStorage
import com.rosi.nectarssh.data.IdentityStorage
import com.rosi.nectarssh.ui.theme.NectarSSHTheme
import java.util.UUID

class ConnectionActivity : ComponentActivity() {
    companion object {
        const val EXTRA_CONNECTION_ID = "connection_id"
        private const val REQUEST_NEW_IDENTITY = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val connectionStorage = ConnectionStorage(this)
        val identityStorage = IdentityStorage(this)
        val connectionId = intent.getStringExtra(EXTRA_CONNECTION_ID)
        val existingConnection = connectionId?.let { connectionStorage.getConnection(it) }

        setContent {
            NectarSSHTheme {
                NewConnectionScreen(
                    existingConnection = existingConnection,
                    identityStorage = identityStorage,
                    onSave = { connection ->
                        if (existingConnection != null) {
                            connectionStorage.updateConnection(connection)
                        } else {
                            connectionStorage.addConnection(connection)
                        }
                        setResult(Activity.RESULT_OK)
                        finish()
                    },
                    onCancel = {
                        setResult(Activity.RESULT_CANCELED)
                        finish()
                    },
                    onCreateNewIdentity = {
                        val intent = Intent(this, IdentityActivity::class.java)
                        startActivityForResult(intent, REQUEST_NEW_IDENTITY)
                    }
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_NEW_IDENTITY && resultCode == Activity.RESULT_OK) {
            // Identity was created, recreate the activity to refresh the identity list
            recreate()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewConnectionScreen(
    existingConnection: Connection? = null,
    identityStorage: IdentityStorage,
    onSave: (Connection) -> Unit,
    onCancel: () -> Unit,
    onCreateNewIdentity: () -> Unit
) {
    val context = LocalContext.current
    val isEditMode = existingConnection != null
    var nickname by remember { mutableStateOf(existingConnection?.nickname ?: "") }
    var address by remember { mutableStateOf(existingConnection?.address ?: "") }
    var port by remember { mutableStateOf(existingConnection?.port?.toString() ?: "22") }
    var selectedIdentityId by remember { mutableStateOf(existingConnection?.identityId ?: "") }
    var expandedIdentityDropdown by remember { mutableStateOf(false) }

    val identities = remember { identityStorage.loadIdentities() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Update Connection" else "New Connection") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cancel")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (address.isNotBlank() && selectedIdentityId.isNotBlank()) {
                                val connection = Connection(
                                    id = existingConnection?.id ?: UUID.randomUUID().toString(),
                                    nickname = nickname.ifBlank { address },
                                    address = address,
                                    port = port.toIntOrNull() ?: 22,
                                    identityId = selectedIdentityId
                                )
                                onSave(connection)
                            }
                        },
                        enabled = address.isNotBlank() && selectedIdentityId.isNotBlank()
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
                value = address,
                onValueChange = { address = it },
                label = { Text("Address") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = port,
                onValueChange = { port = it },
                label = { Text("Port") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            // Identity dropdown
            ExposedDropdownMenuBox(
                expanded = expandedIdentityDropdown,
                onExpandedChange = { expandedIdentityDropdown = it }
            ) {
                OutlinedTextField(
                    value = if (selectedIdentityId.isNotBlank()) {
                        identities.find { it.id == selectedIdentityId }?.nickname ?: ""
                    } else {
                        ""
                    },
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Identity") },
                    trailingIcon = {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Identity")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expandedIdentityDropdown,
                    onDismissRequest = { expandedIdentityDropdown = false }
                ) {
                    identities.forEach { identity ->
                        DropdownMenuItem(
                            text = { Text(identity.nickname) },
                            onClick = {
                                selectedIdentityId = identity.id
                                expandedIdentityDropdown = false
                            }
                        )
                    }
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("New...") },
                        onClick = {
                            expandedIdentityDropdown = false
                            onCreateNewIdentity()
                        }
                    )
                }
            }
        }
    }
}
