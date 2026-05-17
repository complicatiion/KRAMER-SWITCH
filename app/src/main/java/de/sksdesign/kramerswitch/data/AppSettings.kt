package de.sksdesign.kramerswitch.data

import android.content.Context
import de.sksdesign.kramerswitch.core.KramerProtocol

data class AppSettings(
    val deviceName: String = "Kramer VS-81H",
    val ipAddress: String = KramerProtocol.DEFAULT_IP,
    val port: Int = KramerProtocol.DEFAULT_PORT,
    val timeoutMs: Int = KramerProtocol.DEFAULT_TIMEOUT_MS,
    val machineNumber: Int = KramerProtocol.DEFAULT_MACHINE_NUMBER,
    val requireReply: Boolean = true,
    val inputLabels: List<String> = List(8) { index -> "HDMI ${index + 1}" }
)

object AppSettingsStore {
    private const val PREFS = "kramer_switch_prefs"
    private const val KEY_DEVICE_NAME = "device_name"
    private const val KEY_IP = "ip_address"
    private const val KEY_PORT = "port"
    private const val KEY_TIMEOUT = "timeout_ms"
    private const val KEY_MACHINE = "machine_number"
    private const val KEY_REQUIRE_REPLY = "require_reply"
    private const val KEY_ACTIVE_INPUT = "active_input"
    private const val KEY_LAST_REPLY = "last_reply"

    fun load(context: Context): AppSettings {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return AppSettings(
            deviceName = prefs.getString(KEY_DEVICE_NAME, null).orDefault("Kramer VS-81H"),
            ipAddress = prefs.getString(KEY_IP, null).orDefault(KramerProtocol.DEFAULT_IP),
            port = prefs.getInt(KEY_PORT, KramerProtocol.DEFAULT_PORT),
            timeoutMs = prefs.getInt(KEY_TIMEOUT, KramerProtocol.DEFAULT_TIMEOUT_MS),
            machineNumber = prefs.getInt(KEY_MACHINE, KramerProtocol.DEFAULT_MACHINE_NUMBER),
            requireReply = prefs.getBoolean(KEY_REQUIRE_REPLY, true),
            inputLabels = List(8) { index ->
                prefs.getString("input_label_${index + 1}", null).orDefault("HDMI ${index + 1}")
            }
        )
    }

    fun save(context: Context, settings: AppSettings) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_DEVICE_NAME, settings.deviceName.trim().ifEmpty { "Kramer VS-81H" })
            .putString(KEY_IP, settings.ipAddress.trim().ifEmpty { KramerProtocol.DEFAULT_IP })
            .putInt(KEY_PORT, settings.port.coerceIn(1, 65535))
            .putInt(KEY_TIMEOUT, settings.timeoutMs.coerceIn(300, 10000))
            .putInt(KEY_MACHINE, settings.machineNumber.coerceIn(1, 31))
            .putBoolean(KEY_REQUIRE_REPLY, settings.requireReply)
            .also { editor ->
                settings.inputLabels.take(8).forEachIndexed { index, value ->
                    editor.putString("input_label_${index + 1}", value.trim().ifEmpty { "HDMI ${index + 1}" })
                }
            }
            .apply()
    }

    fun loadActiveInput(context: Context): Int? {
        val value = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY_ACTIVE_INPUT, -1)
        return value.takeIf { it in 1..8 }
    }

    fun saveActiveInput(context: Context, input: Int?) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putInt(KEY_ACTIVE_INPUT, input ?: -1)
            .apply()
    }

    fun loadLastReply(context: Context): String? {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_LAST_REPLY, null)
    }

    fun saveLastReply(context: Context, replyHex: String?) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_LAST_REPLY, replyHex)
            .apply()
    }

    fun clearSettings(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply()
    }

    private fun String?.orDefault(default: String): String = this?.takeIf { it.isNotBlank() } ?: default
}
