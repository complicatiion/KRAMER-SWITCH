package de.sksdesign.kramerswitch

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Router
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.WifiTethering
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.view.WindowCompat
import de.sksdesign.kramerswitch.core.KramerClient
import de.sksdesign.kramerswitch.core.KramerProtocol
import de.sksdesign.kramerswitch.data.AppSettings
import de.sksdesign.kramerswitch.data.AppSettingsStore
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            KramerSwitchRoot()
        }
    }
}

private enum class ConnectionState {
    Idle,
    Busy,
    Connected,
    Error
}

private val AppBackground = Color(0xFF050816)
private val AppCard = Color(0xD9111930)
private val AppCardSoft = Color(0x99202A46)
private val LogoBlue = Color(0xFF4E73E8)
private val LogoBlueSoft = Color(0xFF9CB6FF)
private val ActiveGreen = Color(0xFF30E878)
private val InactiveRed = Color(0xFFFF5C6C)
private val TextPrimary = Color(0xFFF3F6FF)
private val TextSecondary = Color(0xFFB7C2E8)

@Composable
private fun KramerSwitchRoot() {
    val context = LocalContext.current
    var settings by remember { mutableStateOf(AppSettingsStore.load(context)) }
    var activeInput by remember { mutableStateOf(AppSettingsStore.loadActiveInput(context)) }
    var lastReply by remember { mutableStateOf(AppSettingsStore.loadLastReply(context)) }
    var lastMessage by remember { mutableStateOf("Ready") }
    var connectionState by remember { mutableStateOf(ConnectionState.Idle) }
    var showSettings by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun createClient(): KramerClient = KramerClient(
        host = settings.ipAddress,
        port = settings.port,
        timeoutMs = settings.timeoutMs,
        machineNumber = settings.machineNumber,
        requireReply = settings.requireReply
    )

    fun applyResult(result: KramerClient.ClientResult) {
        when (result) {
            is KramerClient.ClientResult.Success -> {
                connectionState = ConnectionState.Connected
                activeInput = result.activeInput
                lastReply = result.replyHex
                lastMessage = result.message
                AppSettingsStore.saveActiveInput(context, result.activeInput)
                AppSettingsStore.saveLastReply(context, result.replyHex)
            }
            is KramerClient.ClientResult.Failure -> {
                connectionState = ConnectionState.Error
                lastReply = result.replyHex
                lastMessage = result.message
                AppSettingsStore.saveLastReply(context, result.replyHex)
            }
        }
    }

    fun switchInput(input: Int) {
        connectionState = ConnectionState.Busy
        lastMessage = if (input == 0) "Disconnecting output..." else "Switching to HDMI $input..."
        scope.launch {
            applyResult(createClient().switchInput(input))
        }
    }

    fun testConnection() {
        connectionState = ConnectionState.Busy
        lastMessage = "Testing connection..."
        scope.launch {
            applyResult(createClient().testConnection())
        }
    }

    fun refreshStatus() {
        connectionState = ConnectionState.Busy
        lastMessage = "Refreshing active input..."
        scope.launch {
            applyResult(createClient().refreshActiveInput())
        }
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = LogoBlue,
            secondary = LogoBlueSoft,
            background = AppBackground,
            surface = AppCard,
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = TextPrimary,
            onSurface = TextPrimary
        )
    ) {
        Surface(color = AppBackground, modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0x664E73E8), Color.Transparent),
                            radius = 900f
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .verticalScroll(rememberScrollState())
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    HeaderCard(
                        settings = settings,
                        connectionState = connectionState,
                        activeInput = activeInput,
                        lastMessage = lastMessage,
                        onOpenSettings = { showSettings = true }
                    )

                    ControlPanel(
                        settings = settings,
                        activeInput = activeInput,
                        onSwitchInput = ::switchInput
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            SmallActionButton(
                                modifier = Modifier.weight(1f),
                                label = "Test Connection",
                                icon = Icons.Rounded.WifiTethering,
                                onClick = ::testConnection
                            )
                            SmallActionButton(
                                modifier = Modifier.weight(1f),
                                label = "Refresh Status",
                                icon = Icons.Rounded.Refresh,
                                onClick = ::refreshStatus
                            )
                        }
                        SmallActionButton(
                            modifier = Modifier.fillMaxWidth(),
                            label = "Power Off",
                            icon = Icons.Rounded.PowerSettingsNew,
                            onClick = { switchInput(0) }
                        )
                    }

                    DiagnosticsCard(settings = settings, lastReply = lastReply)
                }
            }
        }
    }

    if (showSettings) {
        SettingsDialog(
            settings = settings,
            onDismiss = { showSettings = false },
            onSave = { newSettings ->
                settings = newSettings
                AppSettingsStore.save(context, newSettings)
                lastMessage = "Settings saved"
                connectionState = ConnectionState.Idle
                showSettings = false
            },
            onClearCache = {
                val ok = clearAppCache(context)
                lastMessage = if (ok) "Cache cleared" else "Cache could not be fully cleared"
            },
            onResetApp = {
                AppSettingsStore.clearSettings(context)
                clearAppCache(context)
                settings = AppSettings()
                activeInput = null
                lastReply = null
                connectionState = ConnectionState.Idle
                lastMessage = "App reset completed"
                showSettings = false
            }
        )
    }
}

@Composable
private fun HeaderCard(
    settings: AppSettings,
    connectionState: ConnectionState,
    activeInput: Int?,
    lastMessage: String,
    onOpenSettings: () -> Unit
) {
    GlassCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.kramer_logo),
                contentDescription = "KRAMER-SWITCH Logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(58.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "KRAMER-SWITCH",
                    color = TextPrimary,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = settings.deviceName,
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Rounded.Settings, contentDescription = "Settings", tint = TextPrimary)
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatePill(
                label = stateLabel(connectionState),
                color = stateColor(connectionState),
                modifier = Modifier.weight(1f)
            )
            StatePill(
                label = activeInput?.let { "HDMI $it active" } ?: "No active input",
                color = if (activeInput != null) ActiveGreen else InactiveRed,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(text = lastMessage, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ControlPanel(
    settings: AppSettings,
    activeInput: Int?,
    onSwitchInput: (Int) -> Unit
) {
    GlassCard {
        Text(
            text = "HDMI Inputs",
            color = TextPrimary,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Choose the input that should be routed to the HDMI output.",
            color = TextSecondary,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(16.dp))

        settings.inputLabels.take(8).chunked(2).forEachIndexed { rowIndex, rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEachIndexed { columnIndex, label ->
                    val inputNumber = rowIndex * 2 + columnIndex + 1
                    InputButton(
                        modifier = Modifier.weight(1f),
                        input = inputNumber,
                        label = label,
                        isActive = activeInput == inputNumber,
                        onClick = { onSwitchInput(inputNumber) }
                    )
                }
                if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
            if (rowIndex < 3) Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun InputButton(
    modifier: Modifier,
    input: Int,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val color = if (isActive) ActiveGreen else InactiveRed
    Card(
        modifier = modifier
            .height(96.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) Color(0x332FE878) else Color(0x22141B33)
        ),
        border = BorderStroke(1.dp, if (isActive) ActiveGreen.copy(alpha = 0.75f) else Color.White.copy(alpha = 0.10f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatusDot(color = color)
                Text(
                    text = "IN $input",
                    color = TextSecondary,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = label.ifBlank { "HDMI $input" },
                color = TextPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DiagnosticsCard(settings: AppSettings, lastReply: String?) {
    GlassCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Router, contentDescription = null, tint = LogoBlueSoft)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Connection",
                color = TextPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        InfoLine(label = "Target", value = "${settings.ipAddress}:${settings.port}")
        InfoLine(label = "Machine", value = "#${settings.machineNumber}")
        InfoLine(label = "Timeout", value = "${settings.timeoutMs} ms")
        InfoLine(label = "Reply validation", value = if (settings.requireReply) "Enabled" else "Send only")
        InfoLine(label = "Last reply", value = lastReply ?: "-", monospace = true)
    }
}

@Composable
private fun InfoLine(label: String, value: String, monospace: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = value,
            color = TextPrimary,
            style = if (monospace) MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum") else MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun GlassCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = AppCard),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f))
    ) {
        Column(modifier = Modifier.padding(18.dp), content = content)
    }
}

@Composable
private fun StatePill(label: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = AppCardSoft,
        border = BorderStroke(1.dp, color.copy(alpha = 0.55f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusDot(color = color, size = 9)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                color = TextPrimary,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun StatusDot(color: Color, size: Int = 12) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .shadow(10.dp, CircleShape, clip = false)
            .clip(CircleShape)
            .background(color)
    )
}

@Composable
private fun SmallActionButton(
    modifier: Modifier,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    ElevatedButton(
        modifier = modifier.heightIn(min = 58.dp),
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = Color(0xFF18213A),
            contentColor = TextPrimary
        ),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsDialog(
    settings: AppSettings,
    onDismiss: () -> Unit,
    onSave: (AppSettings) -> Unit,
    onClearCache: () -> Unit,
    onResetApp: () -> Unit
) {
    var deviceName by remember { mutableStateOf(settings.deviceName) }
    var ipAddress by remember { mutableStateOf(settings.ipAddress) }
    var port by remember { mutableStateOf(settings.port.toString()) }
    var timeout by remember { mutableStateOf(settings.timeoutMs.toString()) }
    var machine by remember { mutableStateOf(settings.machineNumber.toString()) }
    var requireReply by remember { mutableStateOf(settings.requireReply) }
    var labels by remember { mutableStateOf(settings.inputLabels.take(8)) }
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        disabledTextColor = TextSecondary,
        focusedContainerColor = Color(0xFF121C33),
        unfocusedContainerColor = Color(0xFF121C33),
        disabledContainerColor = Color(0xFF121C33),
        cursorColor = Color.White,
        focusedBorderColor = LogoBlueSoft,
        unfocusedBorderColor = Color.White.copy(alpha = 0.22f),
        focusedLabelColor = LogoBlueSoft,
        unfocusedLabelColor = TextSecondary,
        focusedPlaceholderColor = TextSecondary,
        unfocusedPlaceholderColor = TextSecondary,
        focusedSupportingTextColor = TextSecondary,
        unfocusedSupportingTextColor = TextSecondary
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0E1528)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Settings",
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = deviceName,
                    onValueChange = { deviceName = it },
                    label = { Text("Device name") },
                    singleLine = true,
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = ipAddress,
                    onValueChange = { ipAddress = it.trim() },
                    label = { Text("IP address") },
                    placeholder = { Text(KramerProtocol.DEFAULT_IP) },
                    singleLine = true,
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = port,
                        onValueChange = { port = it.filter(Char::isDigit).take(5) },
                        label = { Text("Port") },
                        singleLine = true,
                        colors = fieldColors,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = timeout,
                        onValueChange = { timeout = it.filter(Char::isDigit).take(5) },
                        label = { Text("Timeout (ms)") },
                        singleLine = true,
                        colors = fieldColors,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = machine,
                    onValueChange = { machine = it.filter(Char::isDigit).take(2) },
                    label = { Text("Machine Number") },
                    supportingText = { Text("VS-81H default: 1") },
                    singleLine = true,
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Validate replies", color = TextPrimary, fontWeight = FontWeight.Bold)
                        Text("Recommended: enabled. The switch confirms valid commands with a 4-byte reply.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(checked = requireReply, onCheckedChange = { requireReply = it })
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.10f))
                Text("Input labels", color = TextPrimary, fontWeight = FontWeight.Bold)
                labels.forEachIndexed { index, label ->
                    OutlinedTextField(
                        value = label,
                        onValueChange = { value -> labels = labels.toMutableList().also { it[index] = value } },
                        label = { Text("HDMI ${index + 1}") },
                        singleLine = true,
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.10f))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = onClearCache,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                    ) {
                        Icon(Icons.Rounded.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Clear Cache")
                    }
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = onResetApp,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                    ) {
                        Icon(Icons.Rounded.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset App")
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = onDismiss,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                    ) {
                        Text("Cancel")
                    }
                    ElevatedButton(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onSave(
                                AppSettings(
                                    deviceName = deviceName.trim().ifEmpty { "Kramer VS-81H" },
                                    ipAddress = ipAddress.trim().ifEmpty { KramerProtocol.DEFAULT_IP },
                                    port = port.toIntOrNull()?.coerceIn(1, 65535) ?: KramerProtocol.DEFAULT_PORT,
                                    timeoutMs = timeout.toIntOrNull()?.coerceIn(300, 10000) ?: KramerProtocol.DEFAULT_TIMEOUT_MS,
                                    machineNumber = machine.toIntOrNull()?.coerceIn(1, 31) ?: KramerProtocol.DEFAULT_MACHINE_NUMBER,
                                    requireReply = requireReply,
                                    inputLabels = labels.mapIndexed { index, value -> value.trim().ifEmpty { "HDMI ${index + 1}" } }
                                )
                            )
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.elevatedButtonColors(containerColor = LogoBlue, contentColor = Color.White)
                    ) {
                        Icon(Icons.Rounded.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save")
                    }
                }
            }
        }
    }
}

private fun stateLabel(state: ConnectionState): String = when (state) {
    ConnectionState.Idle -> "Ready"
    ConnectionState.Busy -> "Working"
    ConnectionState.Connected -> "Connected"
    ConnectionState.Error -> "Error"
}

private fun stateColor(state: ConnectionState): Color = when (state) {
    ConnectionState.Idle -> LogoBlueSoft
    ConnectionState.Busy -> LogoBlue
    ConnectionState.Connected -> ActiveGreen
    ConnectionState.Error -> InactiveRed
}

private fun clearAppCache(context: Context): Boolean {
    return runCatching {
        context.cacheDir.deleteRecursively()
        context.externalCacheDir?.deleteRecursively()
    }.isSuccess
}
