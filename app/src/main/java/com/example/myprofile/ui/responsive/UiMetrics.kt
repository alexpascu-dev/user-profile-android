package com.example.myprofile.ui.responsive

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Stable

@Stable
data class UiMetrics (
    val outerPadding: Dp,
    val spacerHeaderCard: Dp,
    val headerImageSize: Dp,
    val headerHeight: Dp,
    val titleSize: TextUnit,
    val infoTextSize: TextUnit,
    val labelTextSize: TextUnit,
    val fieldMinHeight: Dp,
    val cardCorner: Dp,
    val cardInnerPadding: Dp,
    val buttonMinHeight: Dp,
    val buttonFullWidth: Boolean,
    val smallIconSize: Dp
)

fun computeUiMetrics(maxWidth: Dp): UiMetrics = when {
    //ZEBRA
    maxWidth < 400.dp -> UiMetrics(
        outerPadding = 12.dp,
        spacerHeaderCard = 12.dp,
        headerImageSize = 72.dp,
        headerHeight = 48.dp,
        titleSize = 20.sp,
        infoTextSize = 12.sp,
        labelTextSize = 12.sp,
        fieldMinHeight = 44.dp,
        cardCorner = 20.dp,
        cardInnerPadding = 16.dp,
        buttonMinHeight = 36.dp,
        buttonFullWidth = true,
        smallIconSize = 18.dp
    )
    //COMPACT
    maxWidth < 600.dp -> {
        val gap = when {
            maxWidth < 480.dp -> 20.dp   // small phones
            else -> 28.dp   // medium phones
        }
        UiMetrics(
            outerPadding = 24.dp,
            spacerHeaderCard = gap,
            headerImageSize = 100.dp,
            headerHeight = 60.dp,
            titleSize = 28.sp,
            infoTextSize = 14.sp,
            labelTextSize = 13.sp,
            fieldMinHeight = 56.dp,
            cardCorner = 28.dp,
            cardInnerPadding = 24.dp,
            buttonMinHeight = 52.dp,
            buttonFullWidth = false,
            smallIconSize = 24.dp
        )
    }
    //TABLETS/ EXPANDED
    else -> UiMetrics(
        outerPadding = 32.dp,
        spacerHeaderCard = 28.dp,
        headerImageSize = 120.dp,
        headerHeight = 72.dp,
        titleSize = 32.sp,
        infoTextSize = 16.sp,
        labelTextSize = 14.sp,
        fieldMinHeight = 56.dp,
        cardCorner = 32.dp,
        cardInnerPadding = 28.dp,
        buttonMinHeight = 56.dp,
        buttonFullWidth = false,
        smallIconSize = 24.dp
    )
}

@Composable
fun WithUiMetrics(
    modifier: Modifier = Modifier.fillMaxSize(),
    content: @Composable (UiMetrics) -> Unit
) {
    BoxWithConstraints(modifier) {
        val m = remember(maxWidth) { computeUiMetrics(maxWidth) }
        content(m)
    }
}