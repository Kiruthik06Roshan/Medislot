package com.medislot.app.utils

object ValidationUtils {
    
    fun validateEmail(email: String): String? {
        if (email.isBlank()) return "Email is required"
        val emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        if (!email.matches(emailPattern.toRegex())) {
            return "Invalid email format"
        }
        return null
    }

    fun validatePhone(phone: String): String? {
        if (phone.isBlank()) return "Mobile number is required"
        // Validates standard 10 digit phone number format
        if (!phone.matches("^[0-9]{10}$".toRegex())) {
            return "Mobile number must be exactly 10 digits"
        }
        return null
    }

    fun validatePasswordStrength(password: String): String? {
        if (password.isBlank()) return "Password is required"
        if (password.length < 8) return "Password must be at least 8 characters"
        if (!password.any { it.isDigit() }) return "Password must contain at least one digit"
        if (!password.any { it.isLetter() }) return "Password must contain at least one letter"
        return null
    }

    fun validatePasswordConfirm(password: String, confirm: String): String? {
        if (confirm.isBlank()) return "Password confirmation is required"
        if (password != confirm) return "Passwords do not match"
        return null
    }

    fun validateRequired(value: String, fieldName: String): String? {
        if (value.isBlank() || value == "Select" || value == "None Selected") return "$fieldName is required"
        return null
    }
}
