package com.rosi.nectarssh

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddToHomeScreen
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Anchor
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Api
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Battery5Bar
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.DeviceHub
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Domain
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.EmojiNature
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterVintage
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Mouse
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Sailing
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Skateboarding
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Sports
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Token
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Umbrella
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Webhook
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.IconCompat
import com.rosi.nectarssh.data.Connection
import com.rosi.nectarssh.data.ConnectionStorage
import com.rosi.nectarssh.data.Identity
import com.rosi.nectarssh.data.IdentityStorage
import com.rosi.nectarssh.data.PortForward
import com.rosi.nectarssh.data.PortForwardGroup
import com.rosi.nectarssh.data.PortForwardGroupStorage
import com.rosi.nectarssh.data.PortForwardStorage
import com.rosi.nectarssh.data.RecentStorage
import com.rosi.nectarssh.data.RecentType
import com.rosi.nectarssh.service.SSHTunnelService
import com.rosi.nectarssh.ui.theme.NectarSSHTheme
import com.rosi.nectarssh.util.ShortcutHelper
import com.rosi.nectarssh.util.ShortcutIconStorage
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
    val tabs = listOf("Groups", "Port Forwards", "Connections", "Identities")
    val context = LocalContext.current
    var groupsRefresh by remember { mutableStateOf(0) }
    var portForwardsRefresh by remember { mutableStateOf(0) }
    var connectionsRefresh by remember { mutableStateOf(0) }
    var identitiesRefresh by remember { mutableStateOf(0) }

    val recentStorage = remember { RecentStorage(context) }

    var showShortcutSheet by remember { mutableStateOf(false) }
    var shortcutSheetKey by remember { mutableStateOf(0) }
    var shortcutLabel by remember { mutableStateOf("") }
    var shortcutItemId by remember { mutableStateOf("") }
    var shortcutType by remember { mutableStateOf("") }
    var shortcutSelectedIconIndex by remember { mutableStateOf<Int?>(null) }
    var shortcutCustomBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val groupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            groupsRefresh++
        }
    }

    val portForwardLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            portForwardsRefresh++
        }
    }

    val batchPortForwardLauncher = rememberLauncherForActivityResult(
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

    val importSshConfigLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            portForwardsRefresh++
            connectionsRefresh++
            identitiesRefresh++
            groupsRefresh++
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
                },
                actions = {
                    if (selectedTabIndex == 1) {
                        IconButton(onClick = {
                            val intent = Intent(context, BatchPortForwardActivity::class.java)
                            batchPortForwardLauncher.launch(intent)
                        }) {
                            Icon(Icons.Default.PlaylistAdd, contentDescription = "Batch Add")
                        }
                    }
                    IconButton(onClick = {
                        val intent = Intent(context, ImportSshConfigActivity::class.java)
                        importSshConfigLauncher.launch(intent)
                    }) {
                        Icon(Icons.Default.FileUpload, contentDescription = "Import SSH Config")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                when (selectedTabIndex) {
                    0 -> { // Groups tab
                        val intent = Intent(context, PortForwardGroupActivity::class.java)
                        groupLauncher.launch(intent)
                    }
                    1 -> { // Port Forwards tab
                        val intent = Intent(context, PortForwardActivity::class.java)
                        portForwardLauncher.launch(intent)
                    }
                    2 -> { // Connections tab
                        val intent = Intent(context, ConnectionActivity::class.java)
                        connectionLauncher.launch(intent)
                    }
                    3 -> { // Identities tab
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
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                edgePadding = 0.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> GroupsTab(
                    refreshTrigger = groupsRefresh,
                    onGroupConnect = { group ->
                        recentStorage.addRecentItem(group.id, RecentType.PORT_FORWARD_GROUP)
                        val sessionId = UUID.randomUUID().toString()
                        val serviceIntent = Intent(context, SSHTunnelService::class.java).apply {
                            action = SSHTunnelService.ACTION_START_SESSION
                            putExtra(SSHTunnelService.EXTRA_CONNECTION_ID, group.connectionId)
                            putExtra(SSHTunnelService.EXTRA_SESSION_ID, sessionId)
                            putExtra(SSHTunnelService.EXTRA_PORT_FORWARD_GROUP_ID, group.id)
                        }
                        context.startService(serviceIntent)
                        val logIntent = Intent().apply {
                            setClassName(context, "com.rosi.nectarssh.ui.connection.ConnectionLogActivity")
                            putExtra("session_id", sessionId)
                        }
                        context.startActivity(logIntent)
                    },
                    onGroupEdit = { group ->
                        val intent = Intent(context, PortForwardGroupActivity::class.java).apply {
                            putExtra(PortForwardGroupActivity.EXTRA_GROUP_ID, group.id)
                        }
                        groupLauncher.launch(intent)
                    },
                    onGroupDelete = {
                        groupsRefresh++
                    },
                    onGroupShortcut = { group ->
                        shortcutLabel = group.nickname
                        shortcutItemId = group.id
                        shortcutType = ShortcutLaunchActivity.TYPE_PORT_FORWARD_GROUP
                        shortcutSelectedIconIndex = null
                        shortcutCustomBitmap = null
                        shortcutSheetKey++
                        showShortcutSheet = true
                    }
                )
                1 -> PortForwardsTab(
                    refreshTrigger = portForwardsRefresh,
                    onPortForwardConnect = { portForward, connection ->
                        recentStorage.addRecentItem(portForward.id, RecentType.PORT_FORWARD)
                        val sessionId = UUID.randomUUID().toString()
                        val serviceIntent = Intent(context, SSHTunnelService::class.java).apply {
                            action = SSHTunnelService.ACTION_START_SESSION
                            putExtra(SSHTunnelService.EXTRA_CONNECTION_ID, connection.id)
                            putExtra(SSHTunnelService.EXTRA_SESSION_ID, sessionId)
                            putExtra(SSHTunnelService.EXTRA_PORT_FORWARD_ID, portForward.id)
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
                    },
                    onPortForwardShortcut = { portForward ->
                        shortcutLabel = portForward.nickname
                        shortcutItemId = portForward.id
                        shortcutType = ShortcutLaunchActivity.TYPE_PORT_FORWARD
                        shortcutSelectedIconIndex = null
                        shortcutCustomBitmap = null
                        shortcutSheetKey++
                        showShortcutSheet = true
                    }
                )
                2 -> ConnectionsTab(
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
                    },
                    onConnectionShortcut = { connection ->
                        shortcutLabel = connection.nickname
                        shortcutItemId = connection.id
                        shortcutType = ShortcutLaunchActivity.TYPE_CONNECTION
                        shortcutSelectedIconIndex = null
                        shortcutCustomBitmap = null
                        shortcutSheetKey++
                        showShortcutSheet = true
                    }
                )
                3 -> IdentitiesTab(
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

    if (showShortcutSheet) {
        key(shortcutSheetKey) {
            AddToHomeScreenSheet(
                label = shortcutLabel,
                selectedIconIndex = shortcutSelectedIconIndex,
                customBitmap = shortcutCustomBitmap,
                onSelectedIconChanged = { shortcutSelectedIconIndex = it },
                onCustomBitmapChanged = { shortcutCustomBitmap = it },
                onAdd = { icon ->
                    val success = ShortcutHelper.requestPinShortcut(
                        context = context,
                        itemId = shortcutItemId,
                        shortcutType = shortcutType,
                        label = shortcutLabel,
                        icon = icon
                    )
                    if (!success) {
                        Toast.makeText(context, "Home screen shortcuts not supported", Toast.LENGTH_SHORT).show()
                    }
                    showShortcutSheet = false
                },
                onDismiss = { showShortcutSheet = false }
            )
        }
    }
}

data class ShortcutIcon(
    val imageVector: ImageVector,
    val label: String
)

private val SHORTCUT_ICONS = listOf(
    ShortcutIcon(Icons.Default.Terminal, "Terminal"),
    ShortcutIcon(Icons.Default.Cloud, "Cloud"),
    ShortcutIcon(Icons.Default.Dns, "Server"),
    ShortcutIcon(Icons.Default.Storage, "Database"),
    ShortcutIcon(Icons.Default.Router, "Router"),
    ShortcutIcon(Icons.Default.Lan, "Network"),
    ShortcutIcon(Icons.Default.Hub, "Hub"),
    ShortcutIcon(Icons.Default.VpnKey, "VPN Key"),
    ShortcutIcon(Icons.Default.Lock, "Lock"),
    ShortcutIcon(Icons.Default.Key, "Key"),
    ShortcutIcon(Icons.Default.Security, "Security"),
    ShortcutIcon(Icons.Default.Computer, "Computer"),
    ShortcutIcon(Icons.Default.Code, "Code"),
    ShortcutIcon(Icons.Default.Link, "Link"),
    ShortcutIcon(Icons.Default.Folder, "Folder"),
    ShortcutIcon(Icons.Default.Settings, "Settings"),
    ShortcutIcon(Icons.Default.Home, "Home"),
    ShortcutIcon(Icons.Default.Work, "Work"),
    ShortcutIcon(Icons.Default.Wifi, "WiFi"),
    ShortcutIcon(Icons.Default.Public, "Globe"),
    ShortcutIcon(Icons.Default.Shield, "Shield"),
    ShortcutIcon(Icons.Default.Cable, "Cable"),
    ShortcutIcon(Icons.Default.Speed, "Speed"),
    ShortcutIcon(Icons.Default.DeviceHub, "Device Hub"),
    ShortcutIcon(Icons.Default.Api, "API"),
    ShortcutIcon(Icons.Default.Bluetooth, "Bluetooth"),
    ShortcutIcon(Icons.Default.Cast, "Cast"),
    ShortcutIcon(Icons.Default.Domain, "Domain"),
    ShortcutIcon(Icons.Default.Star, "Star"),
    ShortcutIcon(Icons.Default.Person, "Person"),
    ShortcutIcon(Icons.Default.Dashboard, "Dashboard"),
    ShortcutIcon(Icons.Default.Gamepad, "Gamepad"),
    ShortcutIcon(Icons.Default.Language, "Language"),
    ShortcutIcon(Icons.Default.Science, "Science"),
    ShortcutIcon(Icons.Default.Build, "Build"),
    ShortcutIcon(Icons.Default.Memory, "Memory"),
    ShortcutIcon(Icons.Default.Phone, "Phone"),
    ShortcutIcon(Icons.Default.Laptop, "Laptop"),
    ShortcutIcon(Icons.Default.CloudQueue, "Cloud Queue"),
    ShortcutIcon(Icons.Default.CloudDone, "Cloud Done"),
    ShortcutIcon(Icons.Default.Backup, "Backup"),
    ShortcutIcon(Icons.Default.DataObject, "Data"),
    ShortcutIcon(Icons.Default.Token, "Token"),
    ShortcutIcon(Icons.Default.AdminPanelSettings, "Admin"),
    ShortcutIcon(Icons.Default.Policy, "Policy"),
    ShortcutIcon(Icons.Default.Inventory2, "Inventory"),
    ShortcutIcon(Icons.Default.Layers, "Layers"),
    ShortcutIcon(Icons.Default.Webhook, "Webhook"),
    ShortcutIcon(Icons.Default.Usb, "USB"),
    ShortcutIcon(Icons.Default.SdCard, "SD Card"),
    ShortcutIcon(Icons.Default.Power, "Power"),
    ShortcutIcon(Icons.Default.Battery5Bar, "Battery"),
    ShortcutIcon(Icons.Default.Watch, "Watch"),
    ShortcutIcon(Icons.Default.Headset, "Headset"),
    ShortcutIcon(Icons.Default.Speaker, "Speaker"),
    ShortcutIcon(Icons.Default.Tv, "TV"),
    ShortcutIcon(Icons.Default.Mouse, "Mouse"),
    ShortcutIcon(Icons.Default.Keyboard, "Keyboard"),
    ShortcutIcon(Icons.Default.RocketLaunch, "Rocket"),
    ShortcutIcon(Icons.Default.Bolt, "Bolt"),
    ShortcutIcon(Icons.Default.Palette, "Palette"),
    ShortcutIcon(Icons.Default.Favorite, "Heart"),
    ShortcutIcon(Icons.Default.MusicNote, "Music"),
    ShortcutIcon(Icons.Default.Pets, "Pets"),
    ShortcutIcon(Icons.Default.EmojiNature, "Nature"),
    ShortcutIcon(Icons.Default.Face, "Face"),
    ShortcutIcon(Icons.Default.Celebration, "Party"),
    ShortcutIcon(Icons.Default.LocalFireDepartment, "Fire"),
    ShortcutIcon(Icons.Default.CameraAlt, "Camera"),
    ShortcutIcon(Icons.Default.PhotoCamera, "Photo"),
    ShortcutIcon(Icons.Default.Videocam, "Video"),
    ShortcutIcon(Icons.Default.Visibility, "Eye"),
    ShortcutIcon(Icons.Default.Notifications, "Bell"),
    ShortcutIcon(Icons.Default.Map, "Map"),
    ShortcutIcon(Icons.Default.Explore, "Compass"),
    ShortcutIcon(Icons.Default.Flag, "Flag"),
    ShortcutIcon(Icons.Default.Anchor, "Anchor"),
    ShortcutIcon(Icons.Default.Flight, "Plane"),
    ShortcutIcon(Icons.Default.DirectionsCar, "Car"),
    ShortcutIcon(Icons.Default.Sailing, "Sailing"),
    ShortcutIcon(Icons.Default.Skateboarding, "Skate"),
    ShortcutIcon(Icons.Default.Sports, "Sports"),
    ShortcutIcon(Icons.Default.SportsEsports, "Gaming"),
    ShortcutIcon(Icons.Default.Restaurant, "Food"),
    ShortcutIcon(Icons.Default.SmartToy, "Robot"),
    ShortcutIcon(Icons.Default.BugReport, "Bug"),
    ShortcutIcon(Icons.Default.Extension, "Puzzle"),
    ShortcutIcon(Icons.Default.AutoAwesome, "Sparkle"),
    ShortcutIcon(Icons.Default.FlashOn, "Flash"),
    ShortcutIcon(Icons.Default.ElectricBolt, "Electric"),
    ShortcutIcon(Icons.Default.LightMode, "Sun"),
    ShortcutIcon(Icons.Default.Nightlight, "Moon"),
    ShortcutIcon(Icons.Default.Brightness7, "Bright"),
    ShortcutIcon(Icons.Default.WaterDrop, "Water"),
    ShortcutIcon(Icons.Default.Eco, "Leaf"),
    ShortcutIcon(Icons.Default.Park, "Tree"),
    ShortcutIcon(Icons.Default.FilterVintage, "Flower"),
    ShortcutIcon(Icons.Default.Thermostat, "Temp"),
    ShortcutIcon(Icons.Default.Umbrella, "Umbrella"),
    ShortcutIcon(Icons.Default.Book, "Book"),
    ShortcutIcon(Icons.Default.Storefront, "Store"),
    ShortcutIcon(Icons.Default.AccountBalance, "Bank"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToHomeScreenSheet(
    label: String,
    selectedIconIndex: Int?,
    customBitmap: Bitmap?,
    onSelectedIconChanged: (Int?) -> Unit,
    onCustomBitmapChanged: (Bitmap?) -> Unit,
    onAdd: (IconCompat?) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val bitmap = ShortcutIconStorage.saveIconFromUri(context, uri)
            if (bitmap != null) {
                onCustomBitmapChanged(bitmap)
                onSelectedIconChanged(null)
            } else {
                Toast.makeText(context, "Failed to load image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Add to Home Screen",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Choose an icon for \"$label\"",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(SHORTCUT_ICONS.size) { index ->
                    val icon = SHORTCUT_ICONS[index]
                    val isSelected = selectedIconIndex == index && customBitmap == null
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .then(
                                if (isSelected) Modifier.border(
                                    2.dp,
                                    MaterialTheme.colorScheme.primary,
                                    CircleShape
                                ) else Modifier
                            )
                            .clickable {
                                onSelectedIconChanged(index)
                                onCustomBitmapChanged(null)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon.imageVector,
                            contentDescription = icon.label,
                            modifier = Modifier.size(28.dp),
                            tint = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { imagePickerLauncher.launch("image/*") }
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Choose from Gallery")
                }
                if (customBitmap != null) {
                    Image(
                        bitmap = customBitmap!!.asImageBitmap(),
                        contentDescription = "Selected icon",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    val icon = when {
                        customBitmap != null -> IconCompat.createWithAdaptiveBitmap(customBitmap!!)
                        selectedIconIndex != null -> {
                            val vector = SHORTCUT_ICONS[selectedIconIndex!!].imageVector
                            val bitmap = imageVectorToBitmap(vector)
                            IconCompat.createWithAdaptiveBitmap(bitmap)
                        }
                        else -> null
                    }
                    onAdd(icon)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Shortcut")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private fun imageVectorToBitmap(imageVector: ImageVector): Bitmap {
    val size = 192
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)

    val bgPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.parseColor("#1B6EF3")
        isAntiAlias = true
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f, bgPaint)

    val iconPadding = size * 0.25f
    val iconSize = size - (iconPadding * 2)

    canvas.save()
    canvas.translate(iconPadding, iconPadding)
    val scale = iconSize / imageVector.defaultWidth.value
    canvas.scale(scale, scale)

    val pathPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.WHITE
        isAntiAlias = true
        style = android.graphics.Paint.Style.FILL
    }

    fun renderGroup(group: androidx.compose.ui.graphics.vector.VectorGroup) {
        for (i in 0 until group.size) {
            when (val node = group[i]) {
                is androidx.compose.ui.graphics.vector.VectorPath -> {
                    val composePath = androidx.compose.ui.graphics.Path()
                    androidx.compose.ui.graphics.vector.PathParser()
                        .parsePathString(node.pathData.toPathString())
                        .toPath(composePath)
                    val androidPath = composePath.asAndroidPath()
                    canvas.drawPath(androidPath, pathPaint)
                }
                is androidx.compose.ui.graphics.vector.VectorGroup -> {
                    renderGroup(node)
                }
            }
        }
    }

    renderGroup(imageVector.root)
    canvas.restore()
    return bitmap
}

private fun List<androidx.compose.ui.graphics.vector.PathNode>.toPathString(): String {
    return buildString {
        for (node in this@toPathString) {
            when (node) {
                is androidx.compose.ui.graphics.vector.PathNode.MoveTo ->
                    append("M${node.x},${node.y}")
                is androidx.compose.ui.graphics.vector.PathNode.LineTo ->
                    append("L${node.x},${node.y}")
                is androidx.compose.ui.graphics.vector.PathNode.CurveTo ->
                    append("C${node.x1},${node.y1} ${node.x2},${node.y2} ${node.x3},${node.y3}")
                is androidx.compose.ui.graphics.vector.PathNode.QuadTo ->
                    append("Q${node.x1},${node.y1} ${node.x2},${node.y2}")
                is androidx.compose.ui.graphics.vector.PathNode.Close ->
                    append("Z")
                is androidx.compose.ui.graphics.vector.PathNode.HorizontalTo ->
                    append("H${node.x}")
                is androidx.compose.ui.graphics.vector.PathNode.VerticalTo ->
                    append("V${node.y}")
                is androidx.compose.ui.graphics.vector.PathNode.RelativeMoveTo ->
                    append("m${node.dx},${node.dy}")
                is androidx.compose.ui.graphics.vector.PathNode.RelativeLineTo ->
                    append("l${node.dx},${node.dy}")
                is androidx.compose.ui.graphics.vector.PathNode.RelativeCurveTo ->
                    append("c${node.dx1},${node.dy1} ${node.dx2},${node.dy2} ${node.dx3},${node.dy3}")
                is androidx.compose.ui.graphics.vector.PathNode.RelativeQuadTo ->
                    append("q${node.dx1},${node.dy1} ${node.dx2},${node.dy2}")
                is androidx.compose.ui.graphics.vector.PathNode.RelativeHorizontalTo ->
                    append("h${node.dx}")
                is androidx.compose.ui.graphics.vector.PathNode.RelativeVerticalTo ->
                    append("v${node.dy}")
                is androidx.compose.ui.graphics.vector.PathNode.ArcTo ->
                    append("A${node.horizontalEllipseRadius},${node.verticalEllipseRadius} ${node.theta} ${if (node.isMoreThanHalf) 1 else 0},${if (node.isPositiveArc) 1 else 0} ${node.arcStartX},${node.arcStartY}")
                is androidx.compose.ui.graphics.vector.PathNode.RelativeArcTo ->
                    append("a${node.horizontalEllipseRadius},${node.verticalEllipseRadius} ${node.theta} ${if (node.isMoreThanHalf) 1 else 0},${if (node.isPositiveArc) 1 else 0} ${node.arcStartDx},${node.arcStartDy}")
                is androidx.compose.ui.graphics.vector.PathNode.ReflectiveCurveTo ->
                    append("S${node.x1},${node.y1} ${node.x2},${node.y2}")
                is androidx.compose.ui.graphics.vector.PathNode.RelativeReflectiveCurveTo ->
                    append("s${node.dx1},${node.dy1} ${node.dx2},${node.dy2}")
                is androidx.compose.ui.graphics.vector.PathNode.ReflectiveQuadTo ->
                    append("T${node.x},${node.y}")
                is androidx.compose.ui.graphics.vector.PathNode.RelativeReflectiveQuadTo ->
                    append("t${node.dx},${node.dy}")
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GroupsTab(
    refreshTrigger: Int,
    onGroupConnect: (PortForwardGroup) -> Unit,
    onGroupEdit: (PortForwardGroup) -> Unit,
    onGroupDelete: () -> Unit,
    onGroupShortcut: (PortForwardGroup) -> Unit
) {
    val context = LocalContext.current
    val groupStorage = remember { PortForwardGroupStorage(context) }
    val connectionStorage = remember { ConnectionStorage(context) }
    var groups by remember { mutableStateOf(groupStorage.loadGroups()) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var groupToDelete by remember { mutableStateOf<PortForwardGroup?>(null) }

    LaunchedEffect(refreshTrigger) {
        groups = groupStorage.loadGroups()
    }

    if (groups.isEmpty()) {
        EmptyListMessage("No groups")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(groups) { group ->
                val connection = connectionStorage.getConnection(group.connectionId)
                val portForwardCount = group.portForwardIds.size

                GroupListItem(
                    group = group,
                    connectionNickname = connection?.nickname ?: "Unknown",
                    portForwardCount = portForwardCount,
                    onClick = { onGroupConnect(group) },
                    onEdit = { onGroupEdit(group) },
                    onDelete = {
                        groupToDelete = group
                        showDeleteDialog = true
                    },
                    onAddShortcut = { onGroupShortcut(group) }
                )
            }
        }
    }

    if (showDeleteDialog && groupToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                groupToDelete = null
            },
            title = { Text("Delete Group") },
            text = { Text("Are you sure you want to delete \"${groupToDelete!!.nickname}\"? Port forwards will not be deleted.") },
            confirmButton = {
                TextButton(onClick = {
                    groupStorage.deleteGroup(groupToDelete!!.id)
                    groups = groupStorage.loadGroups()
                    showDeleteDialog = false
                    groupToDelete = null
                    onGroupDelete()
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    groupToDelete = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GroupListItem(
    group: PortForwardGroup,
    connectionNickname: String,
    portForwardCount: Int,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddShortcut: () -> Unit
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
                    text = group.nickname,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "$portForwardCount port forwards via $connectionNickname",
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
                    text = { Text("Add to Home Screen") },
                    onClick = {
                        showMenu = false
                        onAddShortcut()
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
fun PortForwardsTab(
    refreshTrigger: Int,
    onPortForwardConnect: (PortForward, Connection) -> Unit,
    onPortForwardEdit: (PortForward) -> Unit,
    onPortForwardDelete: () -> Unit,
    onPortForwardShortcut: (PortForward) -> Unit
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
                        },
                        onAddShortcut = { onPortForwardShortcut(portForward) }
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
    onDelete: () -> Unit,
    onAddShortcut: () -> Unit
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
                    text = { Text("Add to Home Screen") },
                    onClick = {
                        showMenu = false
                        onAddShortcut()
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
    onConnectionDelete: () -> Unit,
    onConnectionShortcut: (Connection) -> Unit
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
                    },
                    onAddShortcut = { onConnectionShortcut(connection) }
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
    onDelete: () -> Unit,
    onAddShortcut: () -> Unit
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
                    text = { Text("Add to Home Screen") },
                    onClick = {
                        showMenu = false
                        onAddShortcut()
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
