package com.example.myprofile.ui.components

import android.content.Context
import android.widget.Toast
import com.google.android.gms.common.moduleinstall.ModuleInstall
import com.google.android.gms.common.moduleinstall.ModuleInstallRequest
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

class BarcodeScanner(private val context: Context) {

    private val options = GmsBarcodeScannerOptions.Builder()
        .setBarcodeFormats(
            Barcode.FORMAT_EAN_8,
            Barcode.FORMAT_EAN_13,
            Barcode.FORMAT_UPC_A,
            Barcode.FORMAT_UPC_E,
            Barcode.FORMAT_CODE_128,
            Barcode.FORMAT_CODE_39,
            Barcode.FORMAT_ITF,
            Barcode.FORMAT_QR_CODE,
            Barcode.FORMAT_DATA_MATRIX,
            Barcode.FORMAT_PDF417
        )
        .enableAutoZoom()
        .build()

    private val scanner: GmsBarcodeScanner by lazy {
        GmsBarcodeScanning.getClient(context, options)
    }

    //Optional
    fun ensureInstalled(onReady: () -> Unit, onError: (Throwable) -> Unit) {
        val moduleInstall = ModuleInstall.getClient(context)
        val req = ModuleInstallRequest
            .newBuilder()
            .addApi(scanner)
            .build()

        moduleInstall.installModules(req)
            .addOnSuccessListener { onReady() }
            .addOnFailureListener { onError(it) }
    }

    fun startScan(
        onResult: (String?) -> Unit,
        onCancel: () -> Unit = {},
        onError: (Throwable) -> Unit = { e ->
            Toast.makeText(context, e.message ?: "Scan error", Toast.LENGTH_SHORT).show()
        }
    ) {
        scanner.startScan()
            .addOnSuccessListener { response ->
                response.rawValue?.let(onResult)
            }
            .addOnCanceledListener { onCancel() }
            .addOnFailureListener { onError(it) }
    }
}