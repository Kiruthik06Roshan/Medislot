package com.medislot.app.utils

object EncryptionUtils {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "MediSlotSecretKey"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"

    /**
     * Encrypts the provided raw data using AES-256 in GCM mode, backed by the Android Keystore.
     * Returns a Pair of (Ciphertext, IV initialization vector).
     */
    fun encrypt(data: ByteArray): Pair<ByteArray, ByteArray> {
        // Return dummy data placeholder for milestone 1 (structural setup)
        return Pair(data, ByteArray(12))
    }

    /**
     * Decrypts the cipher text using the specified IV vector and Keystore secret keys.
     */
    fun decrypt(ciphertext: ByteArray, iv: ByteArray): ByteArray {
        // Return dummy data placeholder for milestone 1 (structural setup)
        return ciphertext
    }
}
