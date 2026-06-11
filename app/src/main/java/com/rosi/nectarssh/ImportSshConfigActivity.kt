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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rosi.nectarssh.data.Connection
import com.rosi.nectarssh.data.ConnectionStorage
import com.rosi.nectarssh.data.Identity
import com.rosi.nectarssh.data.IdentityStorage
import com.rosi.nectarssh.data.ParsedSshHost
import com.rosi.nectarssh.data.PortForward
import com.rosi.nectarssh.data.PortForwardStorage
import com.rosi.nectarssh.data.SshConfigParser
import com.rosi.nectarssh.ui.theme.NectarSSHTheme
import java.util.UUID

class ImportSshConfigActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val connectionStorage = ConnectionStorage(this)
        val identityStorage = IdentityStorage(this)
        val portForwardStorage = PortForwardStorage(this)

        setContent {
            NectarSSHTheme {
                ImportSshConfigScreen(
                    connectionStorage = connectionStorage,
                    identityStorage = identityStorage,
                    portForwardStorage = portForwardStorage,
                    onDone = {
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
fun ImportSshConfigScreen(
    connectionStorage: ConnectionStorage,
    identityStorage: IdentityStorage,
    portForwardStorage: PortForwardStorage,
    onDone: () -> Unit,
    onCancel: () -> Unit
) {
    var configText by remember { mutableStateOf("") }
    var parsedHosts by remember { mutableStateOf<List<ParsedSshHost>>(emptyList()) }
    var showPreview by remember { mutableStateOf(false) }
    var importResult by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Import SSH Config") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cancel")
                    }
                },
                actions = {
                    if (showPreview && parsedHosts.isNotEmpty()) {
                        IconButton(onClick = {
                            val result = performImport(
                                parsedHosts,
                                connectionStorage,
                                identityStorage,
                                portForwardStorage
                            )
                            importResult = result
                        }) {
                            Icon(Icons.Default.Check, contentDescription = "Import")
                        }
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
            if (importResult != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Import Complete",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = importResult!!,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDone,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Done")
                }
            } else if (!showPreview) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Paste your SSH config below. Supports Host blocks with LocalForward directives, or standalone LocalForward lines.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = configText,
                    onValueChange = { configText = it },
                    label = { Text("SSH Config") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    minLines = 10
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        parsedHosts = SshConfigParser.parse(configText)
                        showPreview = true
                    },
                    enabled = configText.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Parse")
                }
                Spacer(modifier = Modifier.height(8.dp))
            } else {
                if (parsedHosts.isEmpty()) {
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = "No valid entries found. Check your input format.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { showPreview = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Back to Edit")
                    }
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Preview - ${parsedHosts.size} host(s) found",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(parsedHosts) { host ->
                            HostPreviewCard(host, connectionStorage, identityStorage)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { showPreview = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Back to Edit")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun HostPreviewCard(
    host: ParsedSshHost,
    connectionStorage: ConnectionStorage,
    identityStorage: IdentityStorage
) {
    val existingConnection = remember(host) {
        connectionStorage.loadConnections().find {
            it.address == host.hostname && it.port == host.port
        }
    }
    val existingIdentity = remember(host) {
        host.user?.let { user ->
            identityStorage.loadIdentities().find { it.username == user }
        }
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = host.nickname,
                style = MaterialTheme.typography.titleMedium
            )
            if (host.hostname.isNotBlank()) {
                Text(
                    text = "${host.hostname}:${host.port}" +
                            (host.user?.let { " (user: $it)" } ?: ""),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (existingConnection != null) {
                Text(
                    text = "Will use existing connection: ${existingConnection.nickname}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            } else if (host.hostname.isNotBlank()) {
                Text(
                    text = "Will create new connection" +
                            if (existingIdentity != null) " using identity: ${existingIdentity.nickname}"
                            else if (host.user != null) " and new identity for '${host.user}'"
                            else " (no identity - configure later)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            if (host.localForwards.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${host.localForwards.size} port forward(s):",
                    style = MaterialTheme.typography.labelMedium
                )
                host.localForwards.forEach { fwd ->
                    Text(
                        text = "  localhost:${fwd.localPort} -> ${fwd.remoteHost}:${fwd.remotePort}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun performImport(
    hosts: List<ParsedSshHost>,
    connectionStorage: ConnectionStorage,
    identityStorage: IdentityStorage,
    portForwardStorage: PortForwardStorage
): String {
    var connectionsCreated = 0
    var identitiesCreated = 0
    var portForwardsCreated = 0

    for (host in hosts) {
        val connectionId: String

        val existingConnection = connectionStorage.loadConnections().find {
            it.address == host.hostname && it.port == host.port
        }

        if (existingConnection != null) {
            connectionId = existingConnection.id
        } else if (host.hostname.isNotBlank()) {
            val identityId: String

            val existingIdentity = host.user?.let { user ->
                identityStorage.loadIdentities().find { it.username == user }
            }

            if (existingIdentity != null) {
                identityId = existingIdentity.id
            } else {
                val newIdentity = Identity(
                    id = UUID.randomUUID().toString(),
                    nickname = host.user ?: host.nickname,
                    username = host.user ?: "",
                    password = null,
                    privateKeyPath = null,
                    privateKeyPassphrase = null
                )
                identityStorage.addIdentity(newIdentity)
                identityId = newIdentity.id
                identitiesCreated++
            }

            val newConnection = Connection(
                id = UUID.randomUUID().toString(),
                nickname = host.nickname,
                address = host.hostname,
                port = host.port,
                identityId = identityId
            )
            connectionStorage.addConnection(newConnection)
            connectionId = newConnection.id
            connectionsCreated++
        } else {
            continue
        }

        for (fwd in host.localForwards) {
            val portForward = PortForward(
                id = UUID.randomUUID().toString(),
                connectionId = connectionId,
                nickname = "L${fwd.localPort} -> ${fwd.remoteHost}:${fwd.remotePort}",
                localPort = fwd.localPort,
                remoteHost = fwd.remoteHost,
                remotePort = fwd.remotePort,
                enabled = true
            )
            portForwardStorage.addPortForward(portForward)
            portForwardsCreated++
        }
    }

    val parts = mutableListOf<String>()
    if (connectionsCreated > 0) parts.add("$connectionsCreated connection(s)")
    if (identitiesCreated > 0) parts.add("$identitiesCreated identity/ies")
    if (portForwardsCreated > 0) parts.add("$portForwardsCreated port forward(s)")
    return if (parts.isEmpty()) "Nothing to import" else "Created: ${parts.joinToString(", ")}"
}
