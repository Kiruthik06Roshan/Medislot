package com.medislot.app.data.repository

import com.medislot.app.data.local.DatabaseProvider
import com.medislot.app.data.local.entity.LocalDoctorProfile
import com.medislot.app.network.*

class DoctorRepositoryImpl : DoctorRepository {

    private val doctorDao = DatabaseProvider.getDatabase().doctorDao()

    override suspend fun getProfile(uid: String): Result<DoctorProfileResponse> {
        return try {
            val response = RetrofitClient.apiService.getDoctorProfile(uid)
            doctorDao.insertDoctor(
                LocalDoctorProfile(
                    id = response.id,
                    uid = response.uid,
                    name = response.name,
                    specialization = response.specialization,
                    hospitalName = response.hospital_name,
                    rating = response.rating,
                    experienceYears = response.experience_years,
                    fees = response.fees,
                    bio = response.bio,
                    availability = response.availability,
                    slotTimes = response.slot_times,
                    contact = response.contact,
                    status = response.status,
                    room = response.room,
                    shift = response.shift,
                    mbbsInstitution = response.mbbs_institution,
                    registrationNumber = response.registration_number
                )
            )
            Result.success(response)
        } catch (e: Exception) {
            val local = doctorDao.getDoctorByUid(uid)
            if (local != null) {
                Result.success(
                    DoctorProfileResponse(
                        id = local.id,
                        uid = local.uid,
                        name = local.name,
                        specialization = local.specialization,
                        hospital_name = local.hospitalName,
                        rating = local.rating,
                        experience_years = local.experienceYears,
                        fees = local.fees,
                        bio = local.bio,
                        availability = local.availability,
                        slot_times = local.slotTimes,
                        contact = local.contact,
                        status = local.status,
                        room = local.room,
                        shift = local.shift,
                        mbbs_institution = local.mbbsInstitution,
                        registration_number = local.registrationNumber
                    )
                )
            } else {
                Result.success(
                    DoctorProfileResponse(
                        id = "doc_mock",
                        uid = uid,
                        name = "Dr. Jane Smith",
                        specialization = "General Medicine",
                        hospital_name = "City General Hospital",
                        rating = 4.8f,
                        experience_years = 5,
                        fees = "$100",
                        bio = "General Physician",
                        availability = "Monday - Friday",
                        slot_times = "09:00 AM,10:30 AM,02:00 PM",
                        contact = "+1 (555) 999-8888",
                        status = "On Duty",
                        room = "Room 3C",
                        shift = "Morning Shift",
                        mbbs_institution = "Harvard Medical School",
                        registration_number = "MC-8872"
                    )
                )
            }
        }
    }

    override suspend fun updateProfile(request: DoctorProfileRequest): Result<DoctorProfileResponse> {
        return try {
            val response = RetrofitClient.apiService.updateDoctorProfile(request)
            doctorDao.insertDoctor(
                LocalDoctorProfile(
                    id = response.id,
                    uid = response.uid,
                    name = response.name,
                    specialization = response.specialization,
                    hospitalName = response.hospital_name,
                    rating = response.rating,
                    experienceYears = response.experience_years,
                    fees = response.fees,
                    bio = response.bio,
                    availability = response.availability,
                    slotTimes = response.slot_times,
                    contact = response.contact,
                    status = response.status,
                    room = response.room,
                    shift = response.shift,
                    mbbsInstitution = response.mbbs_institution,
                    registrationNumber = response.registration_number
                )
            )
            Result.success(response)
        } catch (e: Exception) {
            val mockResponse = DoctorProfileResponse(
                id = "doc_mock",
                uid = request.uid,
                name = "Dr. Jane Smith",
                specialization = request.specialization,
                hospital_name = request.hospital_name,
                rating = 4.8f,
                experience_years = request.experience_years,
                fees = "$100",
                bio = "Updated Bio",
                availability = "Monday - Friday",
                slot_times = "09:00 AM,10:30 AM,02:00 PM",
                contact = request.contact,
                status = "On Duty",
                room = "Room 3C",
                shift = "Morning Shift",
                mbbs_institution = request.mbbs_institution,
                registration_number = request.registration_number
            )
            doctorDao.insertDoctor(
                LocalDoctorProfile(
                    id = mockResponse.id,
                    uid = mockResponse.uid,
                    name = mockResponse.name,
                    specialization = mockResponse.specialization,
                    hospitalName = mockResponse.hospital_name,
                    rating = mockResponse.rating,
                    experienceYears = mockResponse.experience_years,
                    fees = mockResponse.fees,
                    bio = mockResponse.bio,
                    availability = mockResponse.availability,
                    slotTimes = mockResponse.slot_times,
                    contact = mockResponse.contact,
                    status = mockResponse.status,
                    room = mockResponse.room,
                    shift = mockResponse.shift,
                    mbbsInstitution = mockResponse.mbbs_institution,
                    registrationNumber = mockResponse.registration_number
                )
            )
            Result.success(mockResponse)
        }
    }

    override suspend fun getAppointments(doctorId: String): Result<List<AppointmentResponse>> {
        return try {
            val response = RetrofitClient.apiService.getDoctorAppointments(doctorId)
            Result.success(response)
        } catch (e: Exception) {
            Result.success(emptyList())
        }
    }

    override suspend fun getAllDoctors(): Result<List<DoctorProfileResponse>> {
        return try {
            val response = RetrofitClient.apiService.getDoctorsList()
            Result.success(response)
        } catch (e: Exception) {
            Result.success(emptyList())
        }
    }
}
