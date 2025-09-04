package com.example.myprofile.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.myprofile.R
import com.example.myprofile.ui.responsive.UiMetrics

@Composable
fun BottomActionButtons(
    m: UiMetrics,
    onScan: () -> Unit,
    onPrint: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val basePad = modifier
        .fillMaxWidth()
        .windowInsetsPadding(WindowInsets.navigationBars)
        .padding(horizontal = m.outerPadding, vertical = 12.dp)

    if (m.buttonFullWidth) {
        // Zebra
        Row(basePad.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val zebraBtnMod = Modifier.defaultMinSize(minWidth = 64.dp, minHeight = m.buttonMinHeight)

            PrimaryButton(
//                text = stringResource(R.string.scan_btn),
                onClick = onScan,
                color = null,
                shape = null,
                contentPadding = PaddingValues(10.dp),
                modifier = zebraBtnMod,
                icon = {
                    Box(
                        modifier = Modifier.size(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.outline_barcode_scanner),
                            contentDescription = "Scan"
                        )
                    }
                }
            )

            PrimaryButton(
//                text = stringResource(R.string.print_btn),
                onClick = onPrint,
                color = null,
                shape = null,
                contentPadding = PaddingValues(10.dp),
                modifier = zebraBtnMod,
                icon = {
                    Box(
                        modifier = Modifier.size(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.outline_print),
                            contentDescription = "Print"
                        )
                    }
                }
            )

            PrimaryButton(
//                text = stringResource(R.string.logout_btn),
                onClick = onLogout,
                color = null,
                shape = null,
                contentPadding = PaddingValues(10.dp),
                modifier = zebraBtnMod,
                icon = {
                    Box(
                        modifier = Modifier.size(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.outline_logout),
                            contentDescription = "Logout"
                        )
                    }
                }
            )
        }

    } else {
        // Mobile / Tablets - Horizontal
        Row(basePad, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            val phoneBtnMod = Modifier
                .weight(1f)
                .heightIn(min = m.buttonMinHeight)

            PrimaryButton(
                onClick = onScan,
                color = null,
                shape = null,
                modifier = phoneBtnMod,
                icon = {
                    Box(
                        Modifier.size(54.dp), contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.outline_barcode_scanner),
                            contentDescription = "Scan"
                        )
                    }
                }
            )
            PrimaryButton(
                onClick = onPrint,
                color = null,
                shape = null,
                modifier = phoneBtnMod,
                icon = {
                    Box(
                        Modifier.size(54.dp), contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.outline_print),
                            contentDescription = "Print"
                        )
                    }
                }
            )

            PrimaryButton(
                onClick = onLogout,
                color = null,
                shape = null,
                modifier = phoneBtnMod,
                icon = {
                    Box(
                        Modifier.size(54.dp), contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.outline_logout),
                            contentDescription = "Logout"
                        )
                    }
                }
            )
        }
    }
}