package com.example.myprofile.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.myprofile.R
import com.example.myprofile.ui.responsive.UiMetrics
import com.example.myprofile.ui.theme.MySecondary

@Composable
fun BottomActions(
    m: UiMetrics,
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
        Column(basePad, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val zebraBtnMod = Modifier
                .fillMaxWidth(0.7f)
                .align(Alignment.CenterHorizontally)
                .heightIn(min = m.buttonMinHeight)

            PrimaryButton(
                text = stringResource(R.string.print_btn),
                onClick = onPrint,
                color = MySecondary,
                modifier = zebraBtnMod
            )
            PrimaryButton(
                text = stringResource(R.string.logout_btn),
                onClick = onLogout,
                color = Color.Red,
                modifier = zebraBtnMod
            )
        }

    } else {
        // Mobile / Tablets - Horizontal
        Row(basePad, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            val phoneBtnMod = Modifier
                .weight(1f)
                .heightIn(min = m.buttonMinHeight)

            PrimaryButton(
                text = stringResource(R.string.print_btn),
                onClick = onPrint,
                color = MySecondary,
                modifier = phoneBtnMod
            )
            PrimaryButton(
                text = stringResource(R.string.logout_btn),
                onClick = onLogout,
                color = Color.Red,
                modifier = phoneBtnMod
            )
        }
    }
}