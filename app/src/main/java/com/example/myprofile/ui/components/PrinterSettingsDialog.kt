package com.example.myprofile.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

@Composable
fun PrinterSettingsDialog(
    onDismiss: () -> Unit,
    onSaveMac: (String) -> Unit
) {
    var macTf by remember { mutableStateOf(TextFieldValue("")) }
    var macError by remember { mutableStateOf<String?>(null) }

    val macRegex = remember { Regex("^[0-9A-F]{2}(:[0-9A-F]{2}){5}\$") }

    fun formatMac(raw: String): String {
        // 1) păstrează doar hex, uppercase
        val hex = raw.filter { it.isLetterOrDigit() }
            .uppercase()
            .filter { it in "0123456789ABCDEF" }
            .take(12) // max 12 hex (6 octeți)

        // 2) inserează „:” din 2 în 2
        return hex.chunked(2).joinToString(":")
    }

    // calcul poziție cursor: mapează indexul de hex la indexul în string-ul cu „:”
    fun hexIndexToFormattedPos(hexIdx: Int): Int {
        val colonsBefore = (hexIdx / 2).coerceAtMost(5)
        return hexIdx + colonsBefore
    }

    fun formatMacInput(old: TextFieldValue, newVal: TextFieldValue): TextFieldValue {
        val formatted = formatMac(newVal.text)

        // câte caractere hex erau înainte de noul cursor
        val hexBeforeCursor = newVal.text
            .take(newVal.selection.end.coerceAtMost(newVal.text.length))
            .filter { it.isLetterOrDigit() }
            .uppercase()
            .count { it in "0123456789ABCDEF" }
            .coerceAtMost(12)

        val newCursorPos = hexIndexToFormattedPos(hexBeforeCursor)
            .coerceAtMost(formatted.length)

        return TextFieldValue(
            text = formatted,
            selection = TextRange(newCursorPos)
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Setări imprimantă") },
        text = {
            Column {
                Text("Introdu MAC-ul imprimantei Zebra (se formatează automat)")
                OutlinedTextField(
                    value = macTf,
                    onValueChange = {
                        macTf = formatMacInput(macTf, it)
                        macError = null
                    },
                    isError = macError != null,
                    supportingText = { if (macError != null) Text(macError!!) },
                    label = { Text("Ex: AA:BB:CC:DD:EE:FF") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val mac = macTf.text
                if (!macRegex.matches(mac)) {
                    macError = "Format MAC invalid (ex: AA:BB:CC:DD:EE:FF)"
                } else {
                    onSaveMac(mac) // deja în format corect
                }
            }) { Text("Salvează & Conectează") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Anulează") }
        }
    )
}
