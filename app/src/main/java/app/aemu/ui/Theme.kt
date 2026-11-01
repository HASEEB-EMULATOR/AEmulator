package com.haseeb.emulator.ui

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// base colours painted behind the glass layers
private val BaseLight = Color(0xFFF2F0FA)
private val BaseDark = Color(0xFF080A12)

private val Glass = Color.White

private val Light = lightColorScheme(
    primary = Color(0xFF5B6CFF), onPrimary = Color.White,
    primaryContainer = Color(0xFFE3E6FF), onPrimaryContainer = Color(0xFF0E1747),
    secondary = Color(0xFF6E7291), onSecondary = Color.White,
    secondaryContainer = Color(0xFFE6E7F6), onSecondaryContainer = Color(0xFF1A1C2E),
    tertiary = Color(0xFF9C63C8), onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF1E2FF), onTertiaryContainer = Color(0xFF2C1040),
    background = Color.Transparent, surface = Color.Transparent,
    surfaceContainerLow = Glass.copy(alpha = 0.55f),
    surfaceContainer = Glass.copy(alpha = 0.68f),
    surfaceContainerHigh = Glass.copy(alpha = 0.80f),
    surfaceContainerHighest = Glass.copy(alpha = 0.92f),
    error = Color(0xFFBA1A1A),
)

private val Dark = darkColorScheme(
    primary = Color(0xFFB9C1FF), onPrimary = Color(0xFF0A1A78),
    primaryContainer = Color(0xFF3141C9), onPrimaryContainer = Color(0xFFE3E6FF),
    secondary = Color(0xFFC5C6DC), onSecondary = Color(0xFF2C2E44),
    secondaryContainer = Color(0xFF3E4159), onSecondaryContainer = Color(0xFFE6E7F6),
    tertiary = Color(0xFFDDB3F5), onTertiary = Color(0xFF3F1E56),
    tertiaryContainer = Color(0xFF57336F), onTertiaryContainer = Color(0xFFF1E2FF),
    background = Color.Transparent, surface = Color.Transparent,
    surfaceContainerLow = Glass.copy(alpha = 0.06f),
    surfaceContainer = Glass.copy(alpha = 0.10f),
    surfaceContainerHigh = Glass.copy(alpha = 0.14f),
    surfaceContainerHighest = Glass.copy(alpha = 0.19f),
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(18.dp),
    medium = RoundedCornerShape(26.dp),
    large = RoundedCornerShape(34.dp),
    extraLarge = RoundedCornerShape(44.dp),
)

private val base = Typography()
private val AppType = base.copy(
    displaySmall = base.displaySmall.copy(fontWeight = FontWeight.Bold, letterSpacing = (-1).sp),
    headlineLarge = base.headlineLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.8).sp),
    headlineMedium = base.headlineMedium.copy(fontWeight = FontWeight.SemiBold, letterSpacing = (-0.5).sp),
    headlineSmall = base.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
    titleLarge = base.titleLarge.copy(fontWeight = FontWeight.SemiBold),
    titleMedium = base.titleMedium.copy(fontWeight = FontWeight.Medium),
    labelLarge = base.labelLarge.copy(fontWeight = FontWeight.SemiBold),
)

/** soft gradient, kept for screens that want it on their own surface */
@Composable
fun GlassBrush(): Brush = Brush.linearGradient(
    listOf(
        MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
        Color.Transparent,
        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.20f),
        Color.Transparent,
        MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
    )
)

/** coloured light blobs behind everything: this is what makes the cards read as glass */
@Composable
fun GlassBackdrop() {
    val c = MaterialTheme.colorScheme
    Box(Modifier.fillMaxSize()) {
        Box(
            Modifier.matchParentSize().background(
                Brush.radialGradient(
                    listOf(c.primary.copy(alpha = 0.50f), Color.Transparent),
                    center = Offset(0f, 0f),
                    radius = 900f
                )
            )
        )
        Box(
            Modifier.matchParentSize().background(
                Brush.radialGradient(
                    listOf(c.tertiary.copy(alpha = 0.45f), Color.Transparent),
                    center = Offset(1100f, 250f),
                    radius = 800f
                )
            )
        )
        Box(
            Modifier.matchParentSize().background(
                Brush.radialGradient(
                    listOf(c.secondary.copy(alpha = 0.35f), Color.Transparent),
                    center = Offset(450f, 1900f),
                    radius = 950f
                )
            )
        )
    }
}

@Composable
fun AemuTheme(forceDark: Boolean? = null, content: @Composable () -> Unit) {
    val ctx = LocalContext.current
    val rev = androidx.compose.runtime.remember { androidx.compose.runtime.mutableIntStateOf(0) }
    androidx.compose.runtime.DisposableEffect(ctx) {
        val sp = com.haseeb.emulator.AppPrefs.prefs(ctx)
        val l = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> rev.intValue++ }
        sp.registerOnSharedPreferenceChangeListener(l)
        onDispose { sp.unregisterOnSharedPreferenceChangeListener(l) }
    }
    rev.intValue
    val dark = forceDark ?: when (com.haseeb.emulator.AppPrefs.theme(ctx)) {
        com.haseeb.emulator.AppPrefs.THEME_LIGHT -> false
        com.haseeb.emulator.AppPrefs.THEME_DARK -> true
        else -> isSystemInDarkTheme()
    }
    val scheme = when {
        Build.VERSION.SDK_INT >= 31 && com.haseeb.emulator.AppPrefs.dynamicColor(ctx) -> if (dark) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        dark -> Dark
        else -> Light
    }
    MaterialExpressiveTheme(
        colorScheme = scheme,
        motionScheme = MotionScheme.expressive(),
        shapes = AppShapes,
        typography = AppType,
    ) {
        Box(Modifier.fillMaxSize().background(if (dark) BaseDark else BaseLight)) {
            GlassBackdrop()
            content()
        }
    }
}

val Mono = TextStyle(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontSize = 11.sp, lineHeight = 14.sp)
