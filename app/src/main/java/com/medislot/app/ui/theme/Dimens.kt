package com.medislot.app.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class MediSlotDimens(
    val spaceNone: Dp = 0.dp,
    val spaceXXS: Dp = 4.dp,
    val spaceExtraSmall: Dp = 8.dp,
    val spaceSmall: Dp = 12.dp,
    val spaceMedium: Dp = 16.dp,
    val spaceLarge: Dp = 20.dp,
    val spaceXL: Dp = 24.dp,
    val spaceExtraLarge: Dp = 32.dp,
    val spaceXXL: Dp = 40.dp,
    val spaceDoubleExtraLarge: Dp = 48.dp,
    
    val paddingSmall: Dp = 8.dp,
    val paddingMedium: Dp = 16.dp,
    val paddingLarge: Dp = 24.dp,
    
    // Rounded Card standard (18-22dp as requested)
    val cornerSmall: Dp = 8.dp,
    val cornerMedium: Dp = 20.dp,
    val cornerLarge: Dp = 24.dp,
    val cornerExtraLarge: Dp = 32.dp,
    val cornerFull: Dp = 9999.dp,
    
    // Soft Shadows / Elevations
    val elevationNone: Dp = 0.dp,
    val elevationLow: Dp = 2.dp,
    val elevationMedium: Dp = 4.dp,
    val elevationHigh: Dp = 8.dp
)

val LocalDimens = compositionLocalOf { MediSlotDimens() }
