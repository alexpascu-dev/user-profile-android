package com.example.myprofile.printer

import com.example.myprofile.models.User

object ZplFactory {

    // Constants for 4cm x 3cm label at 203 DPI
    private const val LABEL_WIDTH_DOTS = 320    // 4cm = ~320 dots at 203 DPI
    private const val LABEL_HEIGHT_DOTS = 240   // 3cm = ~240 dots at 203 DPI

    fun userLabel(user: User, barcode: String): String {
        // Clean the barcode value - remove spaces and special characters
        val cleanBarcode = barcode.trim().replace(" ", "").uppercase()

        // Truncate long text to fit on label
        val displayName = truncateText("${user.firstName} ${user.lastName}", 20)
        val displayEmail = truncateText(user.email, 25)
        val displayRole = truncateText(user.role, 15)
        val displayUsername = truncateText(user.userName, 20)

        return """
^XA
^CI28
^PW$LABEL_WIDTH_DOTS
^LL$LABEL_HEIGHT_DOTS
^LH0,0
^LS0

^CF0,18
^FO10,10^FDUser: $displayUsername^FS

^CF0,16
^FO10,35^FDName: $displayName^FS
^FO10,55^FDEmail: $displayEmail^FS
^FO10,75^FDRole: $displayRole^FS

^BY2,2,50
^FO30,110^BCN,50,Y,N,N
^FD$cleanBarcode^FS

^CF0,12
^FO10,180^FDPrinted: ${getCurrentDateTime()}^FS

^PQ1,0,1,Y
^XZ
""".trimIndent()
    }

    private fun truncateText(text: String, maxLength: Int): String {
        return if (text.length <= maxLength) text else text.take(maxLength - 3) + "..."
    }

    private fun getCurrentDateTime(): String {
        val sdf = java.text.SimpleDateFormat("dd/MM HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }
}