package com.example.myprofile.printer

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import com.zebra.sdk.comm.BluetoothConnection
import com.zebra.sdk.comm.Connection
import com.zebra.sdk.printer.PrinterStatus
import com.zebra.sdk.printer.ZebraPrinter
import com.zebra.sdk.printer.ZebraPrinterFactory
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

class ZebraPrinterService(private val context: Context) {

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var connection: Connection? = null
    private var printer: ZebraPrinter? = null

    companion object {
        private const val TAG = "ZebraPrinter"
        // For 4cm width at 203 DPI = ~320 dots
        private const val LABEL_WIDTH_DOTS = 320
        // For 3cm height at 203 DPI = ~240 dots
        private const val LABEL_HEIGHT_DOTS = 240
    }

    enum class PermissionStatus {
        READY,
        MISSING_PERMISSIONS,
        BLUETOOTH_NOT_AVAILABLE,
        BLUETOOTH_DISABLED
    }

    fun checkEnvironment(): PermissionStatus {
        return when {
            bluetoothAdapter == null -> PermissionStatus.BLUETOOTH_NOT_AVAILABLE
            !bluetoothAdapter.isEnabled -> PermissionStatus.BLUETOOTH_DISABLED
            !hasBluetoothPermissions() -> PermissionStatus.MISSING_PERMISSIONS
            else -> PermissionStatus.READY
        }
    }

    fun isConnected(): Boolean = connection?.isConnected == true

    @SuppressLint("MissingPermission")
    suspend fun connectBluetooth(mac: String): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Connecting to: $mac")

            // Disconnect if already connected
            disconnect()

            // Create and open connection
            connection = BluetoothConnection(mac).apply {
                maxTimeoutForRead = 10000
                timeToWaitForMoreData = 1000
            }
            connection?.open()

            if (connection?.isConnected == true) {
                // Get printer instance
                printer = ZebraPrinterFactory.getInstance(connection)

                // Wait a moment for connection to stabilize
                delay(500)

                // Configure printer properly
                configurePrinter()

                Log.d(TAG, "Connected and configured successfully")
                return@withContext true
            }

            Log.e(TAG, "Failed to connect")
            false
        } catch (e: Exception) {
            Log.e(TAG, "Connection error: ${e.message}")
            disconnect()
            false
        }
    }

    private suspend fun configurePrinter() = withContext(Dispatchers.IO) {
        try {
            connection?.let { conn ->
                Log.d(TAG, "Configuring printer...")

                // Clear any pending jobs first
                conn.write("~JA\n".toByteArray())
                delay(200)

                // Set to ZPL mode
                conn.write("! U1 setvar \"device.languages\" \"zpl\"\n".toByteArray())
                delay(200)

                // Configure label dimensions - this is crucial for proper printing
                val configZpl = """
                    ^XA
                    ^PW$LABEL_WIDTH_DOTS
                    ^LL$LABEL_HEIGHT_DOTS
                    ^LS0
                    ^LH0,0
                    ^XZ
                """.trimIndent()

                conn.write(configZpl.toByteArray())
                delay(300)

                // Set print width via SGD command as well
                conn.write("! U1 setvar \"zpl.print_width\" \"$LABEL_WIDTH_DOTS\"\n".toByteArray())
                delay(200)

                // Set label length
                conn.write("! U1 setvar \"zpl.label_length\" \"$LABEL_HEIGHT_DOTS\"\n".toByteArray())
                delay(200)

                // Ensure continuous media mode (not gap/notch detection)
                conn.write("! U1 setvar \"media.sense_mode\" \"bar\"\n".toByteArray())
                delay(200)

                // Set print speed (slower for better quality)
                conn.write("! U1 setvar \"print.tone\" \"0\"\n".toByteArray())
                delay(200)

                Log.d(TAG, "Printer configuration complete")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Configuration error: ${e.message}")
            throw e
        }
    }

    suspend fun printZpl(zpl: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val conn = connection
            val printerInstance = printer

            if (conn == null || !conn.isConnected || printerInstance == null) {
                Log.e(TAG, "No active connection or printer instance")
                return@withContext false
            }

            Log.d(TAG, "Checking printer status...")

            // Check printer status
            val status = try {
                printerInstance.currentStatus
            } catch (e: Exception) {
                Log.w(TAG, "Could not get printer status: ${e.message}")
                null
            }

            if (status != null) {
                when {
                    status.isHeadOpen -> {
                        Log.e(TAG, "Cannot print: Head is open")
                        return@withContext false
                    }
                    status.isPaperOut -> {
                        Log.e(TAG, "Cannot print: Paper out")
                        return@withContext false
                    }
                    status.isPaused -> {
                        Log.w(TAG, "Printer paused, attempting to resume...")
                        conn.write("~PS\n".toByteArray())
                        delay(500)
                    }
                    !status.isReadyToPrint -> {
                        Log.w(TAG, "Printer not ready, but attempting to print anyway")
                    }
                }
            }

            Log.d(TAG, "Sending ZPL data...")
            Log.d(TAG, "ZPL Content:\n$zpl")

            // Send the ZPL data
            conn.write(zpl.toByteArray())

            // Small delay to ensure data is sent
            delay(100)

            Log.d(TAG, "ZPL sent successfully, waiting for print completion...")

            // Wait a bit for printing to complete
            delay(1000)

            return@withContext true

        } catch (e: Exception) {
            Log.e(TAG, "Print error: ${e.message}", e)
            false
        }
    }

    suspend fun disconnect() = withContext(Dispatchers.IO) {
        try {
            connection?.close()
            connection = null
            printer = null
            Log.d(TAG, "Disconnected")
        } catch (e: Exception) {
            Log.e(TAG, "Disconnect error: ${e.message}")
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun ensurePaired(mac: String): Boolean = withContext(Dispatchers.IO) {
        if (!hasBluetoothPermissions()) return@withContext false

        val adapter = bluetoothAdapter ?: return@withContext false

        try {
            val device = adapter.getRemoteDevice(mac)

            if (device.bondState == BluetoothDevice.BOND_BONDED) {
                Log.d(TAG, "Device already paired")
                return@withContext true
            }

            Log.d(TAG, "Starting pairing process...")

            // Start pairing
            val result = CompletableDeferred<Boolean>()
            val filter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)

            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    val dev = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    val state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE)

                    if (dev?.address == mac) {
                        when (state) {
                            BluetoothDevice.BOND_BONDED -> {
                                Log.d(TAG, "Pairing successful")
                                result.complete(true)
                                try { context.unregisterReceiver(this) } catch (e: Exception) { }
                            }
                            BluetoothDevice.BOND_NONE -> {
                                Log.d(TAG, "Pairing failed")
                                result.complete(false)
                                try { context.unregisterReceiver(this) } catch (e: Exception) { }
                            }
                        }
                    }
                }
            }

            context.registerReceiver(receiver, filter)

            val bondResult = device.createBond()
            if (!bondResult) {
                try { context.unregisterReceiver(receiver) } catch (e: Exception) { }
                return@withContext false
            }

            withTimeoutOrNull(30000) { result.await() } ?: false

        } catch (e: Exception) {
            Log.e(TAG, "Pairing error: ${e.message}")
            false
        }
    }

    private fun getCurrentDateTime(): String {
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }

    private fun hasBluetoothPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}