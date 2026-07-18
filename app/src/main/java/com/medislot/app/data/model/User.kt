package com.medislot.app.data.model

data class User(
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val role: String = "" // patient, doctor, hospital
)
