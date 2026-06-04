package com.rosi.nectarssh

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rosi.nectarssh.data.ConnectionStorage
import com.rosi.nectarssh.data.PortForward
import com.rosi.nectarssh.data.PortForwardStorage
import com.rosi.nectarssh.ui.theme.NectarSSHTheme
import java.util.UUID

class BatchPortForwardActivity : ComponentActivity() {
    companion object {
        const val EXTRA_CONNECTION_ID = "connection_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val portForwardStorage = PortForwardStorage(this)
        val connectionStorage = ConnectionStorage(this)
        val preselectedConnectionId = intent.getStringExtra(EXTRA_CONNECTION_ID)

        setContent {
            NectarSSHTheme {
                BatchPortForwardScreen(
                    preselectedConnectionId = preselectedConnectionId,
                    connectionStorage = connectionStorage,
                    onSave = { portForwards ->
                        portForwardStorage.addPortForwards(portForwards)
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

data class PortForwardRowState(
    val nickname: String = "",
    val localPort: String = "",
    val remoteHost: String = "127.0.0.1",
    val remotePort: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchPortForwardScreen(
    preselectedConnectionId: String? = null,
    connectionStorage: ConnectionStorage,
    onSave: (List<PortForward>) -> Unit,
    onCancel: () -> Unit
) {
    var selectedConnectionId by remember { mutableStateOf(preselectedConnectionId ?: "") }
    var expandedConnectionDropdown by remember { mutableStateOf(false) }
    val connections = remember { connectionStorage.loadConnections() }
    val rows = remember { mutableStateListOf(PortForwardRowState()) }

    val isValid = selectedConnectionId.isNotBlank() && rows.any { row ->
        row.localPort.toIntOrNull() != null &&
                row.remoteHost.isNotBlank() &&
                row.remotePort.toIntOrNull() != null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Batch Add Port Forwards") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cancel")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (isValid) {
                                val portForwards = rows
                                    .filter { row ->
                                        row.localPort.toIntOrNull() != null &&
                                                row.remoteHost.isNotBlank() &&
                                                row.remotePort.toIntOrNull() != null
                                    }
                                    .map { row ->
                                        PortForward(
                                            id = UUID.randomUUID().toString(),
                                            connectionId = selectedConnectionId,
                                            nickname = row.nickname.ifBlank { "L${row.localPort} -> ${row.remoteHost}:${row.remotePort}" },
                                            localPort = row.localPort.toInt(),
                                            remoteHost = row.remoteHost,
                                            remotePort = row.remotePort.toInt(),
                                            enabled = true
                                        )
                                    }
                                onSave(portForwards)
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
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

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

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(rows) { index, row ->
                    PortForwardRowEditor(
                        row = row,
                        onUpdate = { updated -> rows[index] = updated },
                        onRemove = { if (rows.size > 1) rows.removeAt(index) }
                    )
                }

                item {
                    TextButton(
                        onClick = { rows.add(PortForwardRowState()) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Row")
                    }
                }
            }
        }
    }
}

@Composable
fun PortForwardRowEditor(
    row: PortForwardRowState,
    onUpdate: (PortForwardRowState) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = row.nickname,
                    onValueChange = { onUpdate(row.copy(nickname = it)) },
                    label = { Text("Nickname (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = row.localPort,
                        onValueChange = { onUpdate(row.copy(localPort = it.filter { c -> c.isDigit() })) },
                        label = { Text("Local") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = row.remoteHost,
                        onValueChange = { onUpdate(row.copy(remoteHost = it)) },
                        label = { Text("Host") },
                        modifier = Modifier.weight(2f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = row.remotePort,
                        onValueChange = { onUpdate(row.copy(remotePort = it.filter { c -> c.isDigit() })) },
                        label = { Text("Remote") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Close, contentDescription = "Remove")
            }
        }
    }
}
