package com.rosi.nectarssh

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.rosi.nectarssh.data.Connection
import com.rosi.nectarssh.data.ConnectionStorage
import com.rosi.nectarssh.data.PortForward
import com.rosi.nectarssh.data.PortForwardStorage
import com.rosi.nectarssh.data.RecentStorage
import com.rosi.nectarssh.data.RecentType
import com.rosi.nectarssh.service.SSHTunnelService
import com.rosi.nectarssh.ui.theme.NectarSSHTheme
import com.rosi.nectarssh.util.PermissionHelper
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Security
import java.util.UUID
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsEthernet

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize BouncyCastle security provider for SSH cryptography
        Security.removeProvider("BC")
        Security.insertProviderAt(BouncyCastleProvider(), 1)

        enableEdgeToEdge()
        setContent {
            NectarSSHTheme {
                MainDashboardScreen()
            }
        }
    }
}

@Composable
fun MainDashboardScreen() {
    val context = LocalContext.current
    val recentStorage = remember { RecentStorage(context) }
    val connectionStorage = remember { ConnectionStorage(context) }
    val portForwardStorage = remember { PortForwardStorage(context) }
    
    var recentItems by remember { mutableStateOf(recentStorage.loadRecentItems().take(5)) }
    
    // Check notification permission state
    var showPermissionBanner by remember { mutableStateOf(false) }
    var hasRequestedPermission by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            PermissionHelper.markPermissionRequestDeclined(context)
            showPermissionBanner = true
        } else {
            showPermissionBanner = false
        }
        hasRequestedPermission = true
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            PermissionHelper.shouldRequestNotificationPermission(context)) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                   !PermissionHelper.isNotificationPermissionGranted(context)) {
            showPermissionBanner = true
            hasRequestedPermission = true
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (hasRequestedPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    showPermissionBanner = !PermissionHelper.isNotificationPermissionGranted(context)
                }
                // Refresh recents when returning to activity
                recentItems = recentStorage.loadRecentItems().take(5)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Permission Banner
            if (showPermissionBanner) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Notifications disabled. Enable them to see tunnel status updates.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        androidx.compose.material3.Button(
                            onClick = {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                intent.data = Uri.fromParts("package", context.packageName, null)
                                context.startActivity(intent)
                            }
                        ) {
                            Text("Settings")
                        }
                    }
                }
            }

            Text(
                text = "NectarSSH",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                if (recentItems.isNotEmpty()) {
                    item {
                        CategoryHeader("Recently Used")
                    }
                    items(recentItems) { item ->
                        when (item.type) {
                            RecentType.CONNECTION -> {
                                val connection = connectionStorage.getConnection(item.id)
                                if (connection != null) {
                                    SettingsItem(
                                        title = connection.nickname,
                                        subtitle = "${connection.address}:${connection.port}",
                                        icon = Icons.Default.Devices,
                                        onClick = {
                                            connect(context, connection, recentStorage)
                                        }
                                    )
                                }
                            }
                            RecentType.PORT_FORWARD -> {
                                val pf = portForwardStorage.getPortForward(item.id)
                                val connection = pf?.let { connectionStorage.getConnection(it.connectionId) }
                                if (pf != null && connection != null) {
                                    SettingsItem(
                                        title = pf.nickname,
                                        subtitle = "Forwarding to ${pf.remoteHost}:${pf.remotePort}",
                                        icon = Icons.Default.SettingsEthernet,
                                        onClick = {
                                            connectPortForward(context, pf, connection, recentStorage)
                                        }
                                    )
                                }
                            }
                        }
                    }
                    item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }
                }

                item {
                    CategoryHeader("Management")
                    SettingsItem(
                        title = "Manage Connections",
                        subtitle = "Port forwards, SSH connections, and identities",
                        icon = Icons.Filled.Public,
                        onClick = {
                            context.startActivity(Intent(context, ConnectionManageActivity::class.java))
                        }
                    )
                    SettingsItem(
                        title = "Settings",
                        subtitle = "Application preferences, theme, and data",
                        icon = Icons.Default.Settings,
                        onClick = {
                            context.startActivity(Intent(context, SettingsActivity::class.java))
                        }
                    )
                }
            }
        }
    }
}

private fun connect(context: Context, connection: Connection, recentStorage: RecentStorage) {
    val sessionId = UUID.randomUUID().toString()
    recentStorage.addRecentItem(connection.id, RecentType.CONNECTION)
    
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
}

private fun connectPortForward(context: Context, pf: PortForward, connection: Connection, recentStorage: RecentStorage) {
    val sessionId = UUID.randomUUID().toString()
    recentStorage.addRecentItem(pf.id, RecentType.PORT_FORWARD)

    val serviceIntent = Intent(context, SSHTunnelService::class.java).apply {
        action = SSHTunnelService.ACTION_START_SESSION
        putExtra(SSHTunnelService.EXTRA_CONNECTION_ID, connection.id)
        putExtra(SSHTunnelService.EXTRA_SESSION_ID, sessionId)
        putExtra(SSHTunnelService.EXTRA_PORT_FORWARD_ID, pf.id)
    }
    context.startService(serviceIntent)

    val logIntent = Intent().apply {
        setClassName(context, "com.rosi.nectarssh.ui.connection.ConnectionLogActivity")
        putExtra("session_id", sessionId)
    }
    context.startActivity(logIntent)
}

@Composable
fun CategoryHeader(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun SettingsItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
