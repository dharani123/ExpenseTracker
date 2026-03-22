package com.example.expensetracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary            = Blue500,
    onPrimary          = Color.White,
    primaryContainer   = Blue500Container,
    onPrimaryContainer = Color(0xFF001C3B),
    secondary          = BlueA400,
    onSecondary        = Color.White,
    secondaryContainer = NeutralGreyLight,
    onSecondaryContainer = Color(0xFF1C1B1F),
)

private val DarkColorScheme = darkColorScheme(
    primary            = Blue500Dark,
    onPrimary          = Color(0xFF003060),
    primaryContainer   = Blue500ContainerDark,
    onPrimaryContainer = Blue500Container,
    secondary          = BlueA400Dark,
    onSecondary        = Color(0xFF002D6E),
    secondaryContainer = NeutralGrey,
    onSecondaryContainer = NeutralGreyLight,
)

@Composable
fun ExpenseTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
