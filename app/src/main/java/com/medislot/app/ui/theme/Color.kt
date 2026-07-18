package com.medislot.app.ui.theme

import androidx.compose.ui.graphics.Color

// Brand Colors
val Primary = Color(0xFF3B82F6)         // Sky Blue Primary
val OnPrimary = Color(0xFFFFFFFF)
val PrimaryContainer = Color(0xFFE0F2FE) // Light sky blue container
val OnPrimaryContainer = Color(0xFF0369A1)

val Secondary = Color(0xFF2EA7F8)       // Ocean Blue Secondary
val OnSecondary = Color(0xFFFFFFFF)
val SecondaryContainer = Color(0xFFF0F9FF)
val OnSecondaryContainer = Color(0xFF0284C7)

val PrimaryGradient = listOf(Color(0xFF3B82F6), Color(0xFF2EA7F8), Color(0xFF25C7E8))
val SecondaryGradient = listOf(Color(0xFF2EA7F8), Color(0xFF25C7E8))
val SOSGradient = listOf(Color(0xFFEF4444), Color(0xFFB91C1C))
val WarningGradient = listOf(Color(0xFFF59E0B), Color(0xFFD97706))
val InfoGradient = listOf(Color(0xFF06B6D4), Color(0xFF0891B2))
val CardGradient = listOf(Color(0xFFFFFFFF), Color(0xFFFFFFFF)) // White card background
val AccentGradient = listOf(Color(0xFF3B82F6), Color(0xFF25C7E8))

// Neutral & UI Colors (Light Theme)
val Background = Color(0xFFFFFFFF)      // Pure White Background
val OnBackground = Color(0xFF1E293B)    // Slate 800 (Primary Text)

val Surface = Color(0xFFFFFFFF)         // Pure White Surface
val OnSurface = Color(0xFF1E293B)
val SurfaceVariant = Color(0xFFF8FAFC)  // Slate 50 (Secondary Background)
val OnSurfaceVariant = Color(0xFF64748B) // Slate 500 (Secondary Text)

val Outline = Color(0xFFE2E8F0)         // Slate 200 (Border Color)
val OutlineVariant = Color(0xFFCBD5E1)  // Slate 300

// Feedback Colors
val Error = Color(0xFFEF4444)           // Red 500
val OnError = Color(0xFFFFFFFF)
val ErrorContainer = Color(0xFFFEE2E2)  // Light Red
val OnErrorContainer = Color(0xFF991B1B)

// Role specific accents
val DoctorAccent = Color(0xFF3B82F6)
val HospitalAccent = Color(0xFFF59E0B)
val SuccessColor = Color(0xFF22C55E)
val WarningColor = Color(0xFFF59E0B)
val DangerColor = Color(0xFFEF4444)
val InfoColor = Color(0xFF06B6D4)