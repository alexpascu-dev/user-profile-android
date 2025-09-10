package com.example.myprofile.ui.components

import android.content.Context
import android.os.Build
import android.content.pm.PackageManager

fun hasDataWedge(context: Context): Boolean {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(
                "com.symbol.datawedge",
                PackageManager.PackageInfoFlags.of(0)
            )
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo("com.symbol.datawedge", 0)
        }
        true
    } catch (_: Exception) {
        false
    }
}
