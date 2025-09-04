package com.example.myprofile.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.myprofile.R

@Composable
fun SettingsWheelButton(
    onSettings: () -> Unit,
    modifier: Modifier = Modifier,
    iconSize: Dp,
    contentPadding: PaddingValues = PaddingValues(10.dp)

) {
    PrimaryButton(
        onClick = onSettings,
        color = null,
        shape = null,
        modifier = modifier,
        contentPadding = contentPadding,
        icon = {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(R.drawable.outline_settings),
                    contentDescription = "Settings",
                    modifier = Modifier.size(iconSize)
                )
            }
        }
    )
}