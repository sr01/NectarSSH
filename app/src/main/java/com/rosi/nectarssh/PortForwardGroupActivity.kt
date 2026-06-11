package com.rosi.nectarssh

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rosi.nectarssh.data.ConnectionStorage
import com.rosi.nectarssh.data.PortForwardGroup
import com.rosi.nectarssh.data.PortForwardGroupStorage
import com.rosi.nectarssh.data.PortForwardStorage
import com.rosi.nectarssh.ui.theme.NectarSSHTheme
import java.util.UUID

class PortForwardGroupActivity : ComponentActivity() {
    companion object {
        const val EXTRA_GROUP_ID = "group_id"
        const val EXTRA_CONNECTION_ID = "connection_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val groupStorage = PortForwardGroupStorage(this)
        val connectionStorage = ConnectionStorage(this)
        val portForwardStorage = PortForwardStorage(this)
        val groupId = intent.getStringExtra(EXTRA_GROUP_ID)
        val preselectedConnectionId = intent.getStringExtra(EXTRA_CONNECTION_ID)
        val existingGroup = groupId?.let { groupStorage.getGroup(it) }

        setContent {
            NectarSSHTheme {
                PortForwardGroupScreen(
                    existingGroup = existingGroup,
                    preselectedConnectionId = preselectedConnectionId,
                    connectionStorage = connectionStorage,
                    portForwardStorage = portForwardStorage,
                    onSave = { group ->
                        if (existingGroup != null) {
                            groupStorage.updateGroup(group)
                        } else {
                            groupStorage.addGroup(group)
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
fun PortForwardGroupScreen(
    existingGroup: PortForwardGroup?,
    preselectedConnectionId: String?,
    connectionStorage: ConnectionStorage,
    portForwardStorage: PortForwardStorage,
    onSave: (PortForwardGroup) -> Unit,
    onCancel: () -> Unit
) {
    val connections = remember { connectionStorage.loadConnections() }
    var selectedConnectionId by remember {
        mutableStateOf(existingGroup?.connectionId ?: preselectedConnectionId ?: "")
    }
    var expandedConnectionDropdown by remember { mutableStateOf(false) }
    var nickname by remember { mutableStateOf(existingGroup?.nickname ?: "") }
    var selectedPortForwardIds by remember {
        mutableStateOf(existingGroup?.portForwardIds?.toSet() ?: emptySet())
    }

    val availablePortForwards = remember(selectedConnectionId) {
        if (selectedConnectionId.isNotBlank()) {
            portForwardStorage.getPortForwardsForConnection(selectedConnectionId)
        } else {
            emptyList()
        }
    }

    val isValid = selectedConnectionId.isNotBlank() &&
            nickname.isNotBlank() &&
            selectedPortForwardIds.isNotEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (existingGroup != null) "Edit Group" else "New Group") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cancel")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (isValid) {
                                val group = PortForwardGroup(
                                    id = existingGroup?.id ?: UUID.randomUUID().toString(),
                                    connectionId = selectedConnectionId,
                                    nickname = nickname,
                                    portForwardIds = selectedPortForwardIds.toList()
                                )
                                onSave(group)
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

            OutlinedTextField(
                value = nickname,
                onValueChange = { nickname = it },
                label = { Text("Group Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

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
                                    if (selectedConnectionId != connection.id) {
                                        selectedConnectionId = connection.id
                                        selectedPortForwardIds = emptySet()
                                    }
                                    expandedConnectionDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (availablePortForwards.isNotEmpty()) {
                Text(
                    text = "Select Port Forwards",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(availablePortForwards) { pf ->
                        val isSelected = pf.id in selectedPortForwardIds
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { checked ->
                                        selectedPortForwardIds = if (checked) {
                                            selectedPortForwardIds + pf.id
                                        } else {
                                            selectedPortForwardIds - pf.id
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = pf.nickname,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = "localhost:${pf.localPort} -> ${pf.remoteHost}:${pf.remotePort}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            } else if (selectedConnectionId.isNotBlank()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No port forwards for this connection",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
