package com.example.myprofile.ui.components

import android.R.attr.fontFamily
import android.R.attr.fontWeight
import android.R.attr.text
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myprofile.ui.theme.MyPrimary

@Composable
fun PrimaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color? = MyPrimary,
    shape: RoundedCornerShape? = RoundedCornerShape(30.dp),
    text: String? = null,
    icon: (@Composable (() -> Unit))? = null,
    contentPadding: PaddingValues? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = color ?: Color.Transparent,
            contentColor = if (color == null) Color.Unspecified else Color.White
            ),
        shape = shape ?: RoundedCornerShape(0.dp),
        contentPadding = contentPadding ?: ButtonDefaults.ContentPadding
    ) {
        when {
            icon != null -> icon()
            text != null -> Text(
                text = text,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                color = if (color == null) Color.Unspecified else Color.White
            )
        }
    }
}