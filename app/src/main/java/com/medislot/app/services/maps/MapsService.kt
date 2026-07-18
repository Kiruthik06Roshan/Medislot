package com.medislot.app.services.maps

interface MapsService {
    suspend fun getNearbyHospitals(latitude: Double, longitude: Double): Result<List<String>>
}
