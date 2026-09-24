package com.example.audio

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class BluetoothStatus(
    val isConnected: Boolean = false,
    val deviceName: String = "No Bluetooth Device",
    val deviceType: String = "Internal Audio",
    val isHeadsetOrA2dp: Boolean = false
)

class BluetoothDeviceHelper(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val _status = MutableStateFlow(checkCurrentStatus())
    val status: StateFlow<BluetoothStatus> = _status.asStateFlow()

    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            _status.value = checkCurrentStatus()
        }
    }

    fun startListening() {
        _status.value = checkCurrentStatus()
        val filter = IntentFilter().apply {
            addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
            addAction("android.bluetooth.adapter.action.CONNECTION_STATE_CHANGED")
            addAction("android.bluetooth.adapter.action.STATE_CHANGED")
            addAction("android.bluetooth.device.action.ACL_CONNECTED")
            addAction("android.bluetooth.device.action.ACL_DISCONNECTED")
            addAction(AudioManager.ACTION_HEADSET_PLUG)
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(bluetoothReceiver, filter, Context.RECEIVER_EXPORTED)
            } else {
                context.registerReceiver(bluetoothReceiver, filter)
            }
        } catch (_: Exception) {
            // Fallback if broadcast receiver registration fails
        }
    }

    fun stopListening() {
        try {
            context.unregisterReceiver(bluetoothReceiver)
        } catch (_: Exception) {
        }
    }

    fun checkCurrentStatus(): BluetoothStatus {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            for (device in devices) {
                when (device.type) {
                    AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
                    AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
                    AudioDeviceInfo.TYPE_BLE_HEADSET,
                    AudioDeviceInfo.TYPE_BLE_SPEAKER -> {
                        val name = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            device.productName.toString().ifBlank { "Bluetooth Audio Device" }
                        } else {
                            "Bluetooth Audio Device"
                        }
                        val typeStr = when (device.type) {
                            AudioDeviceInfo.TYPE_BLUETOOTH_A2DP -> "Bluetooth Earbuds / A2DP"
                            AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> "Bluetooth Headset / SCO"
                            AudioDeviceInfo.TYPE_BLE_HEADSET -> "BLE Headset"
                            else -> "Bluetooth Audio"
                        }
                        return BluetoothStatus(
                            isConnected = true,
                            deviceName = name,
                            deviceType = typeStr,
                            isHeadsetOrA2dp = true
                        )
                    }
                    AudioDeviceInfo.TYPE_WIRED_HEADSET,
                    AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
                    AudioDeviceInfo.TYPE_USB_HEADSET -> {
                        return BluetoothStatus(
                            isConnected = true,
                            deviceName = "Wired / USB Headphones",
                            deviceType = "Headphones",
                            isHeadsetOrA2dp = true
                        )
                    }
                }
            }
        }

        @Suppress("DEPRECATION")
        val isA2dp = audioManager.isBluetoothA2dpOn || audioManager.isWiredHeadsetOn
        return if (isA2dp) {
            BluetoothStatus(
                isConnected = true,
                deviceName = "Connected Audio Device",
                deviceType = "Headphones / Earbuds",
                isHeadsetOrA2dp = true
            )
        } else {
            BluetoothStatus(
                isConnected = false,
                deviceName = "Phone Speaker (No Headphones)",
                deviceType = "Built-in Speaker",
                isHeadsetOrA2dp = false
            )
        }
    }
}
