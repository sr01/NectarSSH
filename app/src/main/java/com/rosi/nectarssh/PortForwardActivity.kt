package com.rosi.nectarssh

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rosi.nectarssh.data.Connection
import com.rosi.nectarssh.data.ConnectionStorage
import com.rosi.nectarssh.data.PortForward
import com.rosi.nectarssh.data.PortForwardStorage
import com.rosi.nectarssh.ui.theme.NectarSSHTheme
import java.util.UUID

class PortForwardActivity : ComponentActivity() {
    companion object {
        const val EXTRA_PORT_FORWARD_ID = "port_forward_id"
        const val EXTRA_CONNECTION_ID = "connection_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val portForwardStorage = PortForwardStorage(this)
        val connectionStorage = ConnectionStorage(this)
        val portForwardId = intent.getStringExtra(EXTRA_PORT_FORWARD_ID)
        val preselectedConnectionId = intent.getStringExtra(EXTRA_CONNECTION_ID)
        val existingPortForward = portForwardId?.let { portForwardStorage.getPortForward(it) }

        setContent {
            NectarSSHTheme {
                PortForwardScreen(
                    existingPortForward = existingPortForward,
                    preselectedConnectionId = preselectedConnectionId,
                    connectionStorage = connectionStorage,
                    onSave = { portForward ->
                        if (existingPortForward != null) {
                            portForwardStorage.updatePortForward(portForward)
                        } else {
                            portForwardStorage.addPortForward(portForward)
                        }
                        setResult(Activity.RESULT_OK)
                        finish()
                    },
                    onCancel = {
                        setResult(Activity.RESULT_CANCELED)
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortForwardScreen(
    existingPortForward: PortForward? = null,
    preselectedConnectionId: String? = null,
    connectionStorage: ConnectionStorage,
    onSave: (PortForward) -> Unit,
    onCancel: () -> Unit
) {
    val isEditMode = existingPortForward != null
    var nickname by remember { mutableStateOf(existingPortForward?.nickname ?: "") }
    var localPort by remember { mutableStateOf(existingPortForward?.localPort?.toString() ?: "") }
    var remoteHost by remember { mutableStateOf(existingPortForward?.remoteHost ?: "127.0.0.1") }
    var remotePort by remember { mutableStateOf(existingPortForward?.remotePort?.toString() ?: "") }
    var selectedConnectionId by remember {
        mutableStateOf(existingPortForward?.connectionId ?: preselectedConnectionId ?: "")
    }
    var expandedConnectionDropdown by remember { mutableStateOf(false) }

    val connections = remember { connectionStorage.loadConnections() }

    val isValid = localPort.toIntOrNull() != null &&
            remoteHost.isNotBlank() &&
            remotePort.toIntOrNull() != null &&
            selectedConnectionId.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Port Forward" else "New Port Forward") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cancel")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (isValid) {
                                val portForward = PortForward(
                                    id = existingPortForward?.id ?: UUID.randomUUID().toString(),
                                    connectionId = selectedConnectionId,
                                    nickname = nickname.ifBlank { "L$localPort -> $remoteHost:$remotePort" },
                                    localPort = localPort.toInt(),
                                    remoteHost = remoteHost,
                                    remotePort = remotePort.toInt(),
                                    enabled = existingPortForward?.enabled ?: true
                                )
                                onSave(portForward)
                            }
                        },
                        enabled = isValid
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
            // Connection dropdown
            ExposedDropdownMenuBox(
                expanded = expandedConnectionDropdown,
                onExpandedChange = { expandedConnectionDropdown = it }
            ) {
                OutlinedTextField(
                    value = if (selectedConnectionId.isNotBlank()) {
                        connections.find { it.id == selectedConnectionId }?.nickname ?: ""
                    } else {
                        ""
                    },
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Connection") },
                    trailingIcon = {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Connection")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expandedConnectionDropdown,
                    onDismissRequest = { expandedConnectionDropdown = false }
                ) {
                    if (connections.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No connections available") },
                            onClick = { expandedConnectionDropdown = false },
                            enabled = false
                        )
                    } else {
                        connections.forEach { connection ->
                            DropdownMenuItem(
                                text = { Text(connection.nickname) },
                                onClick = {
                                    selectedConnectionId = connection.id
                                    expandedConnectionDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = nickname,
                onValueChange = { nickname = it },
                label = { Text("Nickname (Optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider()

            Text(
                text = "Local",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = localPort,
                onValueChange = { localPort = it.filter { c -> c.isDigit() } },
                label = { Text("Local Port") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                supportingText = { Text("Port on this device to listen on") }
            )

            HorizontalDivider()

            Text(
                text = "Remote",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = remoteHost,
                onValueChange = { remoteHost = it },
                label = { Text("Remote Host") },
                modifier = Modifier.fillMaxWidth(),
                supportingText = { Text("Host accessible from SSH server (use 127.0.0.1 for server itself)") }
            )

            OutlinedTextField(
                value = remotePort,
                onValueChange = { remotePort = it.filter { c -> c.isDigit() } },
                label = { Text("Remote Port") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                supportingText = { Text("Port on remote host to forward to") }
            )
        }
    }
}
