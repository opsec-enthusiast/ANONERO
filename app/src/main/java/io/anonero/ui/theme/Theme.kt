package io.anonero.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = primaryColor,
    secondary = secondaryColor,
)

@Composable
fun AnonTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
        shapes = Shapes(
            medium = RoundedCornerShape(8.0.dp ),
            small = RoundedCornerShape(
                topStart = 6.0.dp,
                topEnd = 6.0.dp,
                bottomEnd = 6.0.dp,
                bottomStart = 6.0.dp
            ),
            extraSmall = RoundedCornerShape(
                topStart = 2.0.dp,
                topEnd = 2.0.dp,
                bottomEnd = 2.0.dp,
                bottomStart = 2.0.dp
            ),
            large = RoundedCornerShape(12.0.dp ),
        )
    )
}