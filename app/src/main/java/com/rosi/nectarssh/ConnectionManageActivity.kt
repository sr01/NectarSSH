package com.rosi.nectarssh

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.rosi.nectarssh.data.Connection
import com.rosi.nectarssh.data.ConnectionStorage
import com.rosi.nectarssh.data.Identity
import com.rosi.nectarssh.data.IdentityStorage
import com.rosi.nectarssh.data.PortForward
import com.rosi.nectarssh.data.PortForwardStorage
import com.rosi.nectarssh.data.RecentStorage
import com.rosi.nectarssh.data.RecentType
import com.rosi.nectarssh.service.SSHTunnelService
import com.rosi.nectarssh.ui.theme.NectarSSHTheme
import java.util.UUID

class ConnectionManageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NectarSSHTheme {
                ConnectionManageScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionManageScreen(onBack: () -> Unit) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Port Forwards", "Connections", "Identities")
    val context = LocalContext.current
    var portForwardsRefresh by remember { mutableStateOf(0) }
    var connectionsRefresh by remember { mutableStateOf(0) }
    var identitiesRefresh by remember { mutableStateOf(0) }
    
    val recentStorage = remember { RecentStorage(context) }

    val portForwardLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            portForwardsRefresh++
        }
    }

    val connectionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            connectionsRefresh++
        }
    }

    val identityLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            identitiesRefresh++
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Connections") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                when (selectedTabIndex) {
                    0 -> { // Port Forwards tab
                        val intent = Intent(context, PortForwardActivity::class.java)
                        portForwardLauncher.launch(intent)
                    }
                    1 -> { // Connections tab
                        val intent = Intent(context, ConnectionActivity::class.java)
                        connectionLauncher.launch(intent)
                    }
                    2 -> { // Identities tab
                        val intent = Intent(context, IdentityActivity::class.java)
                        identityLauncher.launch(intent)
                    }
                }
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> PortForwardsTab(
                    refreshTrigger = portForwardsRefresh,
                    onPortForwardConnect = { portForward, connection ->
                        recentStorage.addRecentItem(portForward.id, RecentType.PORT_FORWARD)
                        val sessionId = UUID.randomUUID().toString()
                        val serviceIntent = Intent(context, SSHTunnelService::class.java).apply {
                            action = SSHTunnelService.ACTION_START_SESSION
                            putExtra(SSHTunnelService.EXTRA_CONNECTION_ID, connection.id)
                            putExtra(SSHTunnelService.EXTRA_SESSION_ID, sessionId)
                        }
                        context.startService(serviceIntent)
                        val logIntent = Intent().apply {
                            setClassName(context, "com.rosi.nectarssh.ui.connection.ConnectionLogActivity")
                            putExtra("session_id", sessionId)
                        }
                        context.startActivity(logIntent)
                    },
                    onPortForwardEdit = { portForward ->
                        val intent = Intent(context, PortForwardActivity::class.java).apply {
                            putExtra(PortForwardActivity.EXTRA_PORT_FORWARD_ID, portForward.id)
                        }
                        portForwardLauncher.launch(intent)
                    },
                    onPortForwardDelete = {
                        portForwardsRefresh++
                    }
                )
                1 -> ConnectionsTab(
                    refreshTrigger = connectionsRefresh,
                    onConnectionClick = { connection ->
                        recentStorage.addRecentItem(connection.id, RecentType.CONNECTION)
                        val sessionId = UUID.randomUUID().toString()
                        val serviceIntent = Intent(context, SSHTunnelService::class.java).apply {
                            action = SSHTunnelService.ACTION_START_SESSION
                            putExtra(SSHTunnelService.EXTRA_CONNECTION_ID, connection.id)
                            putExtra(SSHTunnelService.EXTRA_SESSION_ID, sessionId)
                        }
                        context.startService(serviceIntent)
                        val logIntent = Intent().apply {
                            setClassName(context, "com.rosi.nectarssh.ui.connection.ConnectionLogActivity")
                            putExtra("session_id", sessionId)
                        }
                        context.startActivity(logIntent)
                    },
                    onConnectionEdit = { connection ->
                        val intent = Intent(context, ConnectionActivity::class.java).apply {
                            putExtra(ConnectionActivity.EXTRA_CONNECTION_ID, connection.id)
                        }
                        connectionLauncher.launch(intent)
                    },
                    onConnectionDelete = {
                        connectionsRefresh++
                    }
                )
                2 -> IdentitiesTab(
                    refreshTrigger = identitiesRefresh,
                    onIdentityClick = { identity ->
                        val intent = Intent(context, IdentityActivity::class.java).apply {
                            putExtra(IdentityActivity.EXTRA_IDENTITY_ID, identity.id)
                        }
                        identityLauncher.launch(intent)
                    }
                )
            }
        }
    }
}

@Composable
fun PortForwardsTab(
    refreshTrigger: Int,
    onPortForwardConnect: (PortForward, Connection) -> Unit,
    onPortForwardEdit: (PortForward) -> Unit,
    onPortForwardDelete: () -> Unit
) {
    val context = LocalContext.current
    val portForwardStorage = remember { PortForwardStorage(context) }
    val connectionStorage = remember { ConnectionStorage(context) }
    var portForwards by remember { mutableStateOf(portForwardStorage.loadPortForwards()) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var portForwardToDelete by remember { mutableStateOf<PortForward?>(null) }

    LaunchedEffect(refreshTrigger) {
        portForwards = portForwardStorage.loadPortForwards()
    }

    if (portForwards.isEmpty()) {
        EmptyListMessage("No port forwards")
    } else {
        val connections = remember { connectionStorage.loadConnections() }
        val groupedPortForwards = portForwards.groupBy { it.connectionId }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            groupedPortForwards.forEach { (connectionId, forwards) ->
                val connection = connections.find { it.id == connectionId }

                item {
                    Text(
                        text = connection?.nickname ?: "Unknown Connection",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                items(forwards) { portForward ->
                    PortForwardListItem(
                        portForward = portForward,
                        onClick = {
                            connection?.let { onPortForwardConnect(portForward, it) }
                        },
                        onEdit = { onPortForwardEdit(portForward) },
                        onDuplicate = {
                            val duplicated = portForward.copy(
                                id = UUID.randomUUID().toString(),
                                nickname = "${portForward.nickname} (Copy)"
                            )
                            portForwardStorage.addPortForward(duplicated)
                            onPortForwardEdit(duplicated)
                        },
                        onDelete = {
                            portForwardToDelete = portForward
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }
    }

    if (showDeleteDialog && portForwardToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                portForwardToDelete = null
            },
            title = { Text("Delete Port Forward") },
            text = { Text("Are you sure you want to delete \"${portForwardToDelete!!.nickname}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    portForwardStorage.deletePortForward(portForwardToDelete!!.id)
                    portForwards = portForwardStorage.loadPortForwards()
                    showDeleteDialog = false
                    portForwardToDelete = null
                    onPortForwardDelete()
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    portForwardToDelete = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PortForwardListItem(
    portForward: PortForward,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showMenu = true }
            )
    ) {
        Box {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = portForward.nickname,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "localhost:${portForward.localPort} -> ${portForward.remoteHost}:${portForward.remotePort}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Edit") },
                    onClick = {
                        showMenu = false
                        onEdit()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Duplicate") },
                    onClick = {
                        showMenu = false
                        onDuplicate()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Delete") },
                    onClick = {
                        showMenu = false
                        onDelete()
                    }
                )
            }
        }
    }
}

@Composable
fun ConnectionsTab(
    refreshTrigger: Int,
    onConnectionClick: (Connection) -> Unit,
    onConnectionEdit: (Connection) -> Unit,
    onConnectionDelete: () -> Unit
) {
    val context = LocalContext.current
    val connectionStorage = remember { ConnectionStorage(context) }
    val identityStorage = remember { IdentityStorage(context) }
    var connections by remember { mutableStateOf(connectionStorage.loadConnections()) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var connectionToDelete by remember { mutableStateOf<Connection?>(null) }

    LaunchedEffect(refreshTrigger) {
        connections = connectionStorage.loadConnections()
    }

    if (connections.isEmpty()) {
        EmptyListMessage("No connections")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(connections) { connection ->
                ConnectionListItem(
                    connection = connection,
                    identityStorage = identityStorage,
                    onClick = { onConnectionClick(connection) },
                    onEdit = { onConnectionEdit(connection) },
                    onDuplicate = {
                        val duplicated = connection.copy(
                            id = UUID.randomUUID().toString(),
                            nickname = "${connection.nickname} (Copy)"
                        )
                        connectionStorage.addConnection(duplicated)
                        onConnectionEdit(duplicated)
                    },
                    onDelete = {
                        connectionToDelete = connection
                        showDeleteDialog = true
                    }
                )
            }
        }
    }

    if (showDeleteDialog && connectionToDelete != null) {
        DeleteConnectionDialog(
            connection = connectionToDelete!!,
            onConfirm = {
                connectionStorage.deleteConnection(connectionToDelete!!.id)
                connections = connectionStorage.loadConnections()
                showDeleteDialog = false
                connectionToDelete = null
                onConnectionDelete()
            },
            onDismiss = {
                showDeleteDialog = false
                connectionToDelete = null
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ConnectionListItem(
    connection: Connection,
    identityStorage: IdentityStorage,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showMenu = true }
            )
    ) {
        Box {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = connection.nickname,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${connection.address}:${connection.port}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Edit") },
                    onClick = {
                        showMenu = false
                        onEdit()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Duplicate") },
                    onClick = {
                        showMenu = false
                        onDuplicate()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Delete") },
                    onClick = {
                        showMenu = false
                        onDelete()
                    }
                )
            }
        }
    }
}

@Composable
fun IdentitiesTab(refreshTrigger: Int, onIdentityClick: (Identity) -> Unit) {
    val context = LocalContext.current
    val storage = remember { IdentityStorage(context) }
    var identities by remember { mutableStateOf(storage.loadIdentities()) }

    LaunchedEffect(refreshTrigger) {
        identities = storage.loadIdentities()
    }

    if (identities.isEmpty()) {
        EmptyListMessage("No identities")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(identities) { identity ->
                IdentityListItem(
                    identity = identity,
                    onClick = { onIdentityClick(identity) }
                )
            }
        }
    }
}

@Composable
fun IdentityListItem(identity: Identity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = identity.nickname,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = identity.username,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun DeleteConnectionDialog(
    connection: Connection,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Connection") },
        text = { Text("Are you sure you want to delete \"${connection.nickname}\"?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete")
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
fun EmptyListMessage(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
