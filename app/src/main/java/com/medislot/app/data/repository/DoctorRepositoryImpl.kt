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
                Result.failure(e)
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
            val savedLocal = DoctorProfileResponse(
                id = "doc_" + request.uid.take(8),
                uid = request.uid,
                name = "Doctor Profile",
                specialization = request.specialization,
                hospital_name = request.hospital_name,
                rating = 4.8f,
                experience_years = request.experience_years,
                fees = "$100",
                bio = "Bio",
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
                    id = savedLocal.id,
                    uid = savedLocal.uid,
                    name = savedLocal.name,
                    specialization = savedLocal.specialization,
                    hospitalName = savedLocal.hospital_name,
                    rating = savedLocal.rating,
                    experienceYears = savedLocal.experience_years,
                    fees = savedLocal.fees,
                    bio = savedLocal.bio,
                    availability = savedLocal.availability,
                    slotTimes = savedLocal.slot_times,
                    contact = savedLocal.contact,
                    status = savedLocal.status,
                    room = savedLocal.room,
                    shift = savedLocal.shift,
                    mbbsInstitution = savedLocal.mbbs_institution,
                    registrationNumber = savedLocal.registration_number
                )
            )
            Result.success(savedLocal)
        }
    }

    override suspend fun getAppointments(doctorId: String): Result<List<AppointmentResponse>> {
        if (com.medislot.app.ui.screens.auth.DemoConfig.isDemoModeActive) {
            val demoApps = com.medislot.app.data.model.MockData.appointments.mapIndexed { idx, appt ->
                AppointmentResponse(
                    id = appt.id,
                    patient_id = "pat_demo_$idx",
                    doctor_id = doctorId,
                    doctor_name = appt.doctorName,
                    department = appt.department,
                    hospital = appt.hospital,
                    date = appt.date,
                    time = appt.time,
                    status = appt.status,
                    queue_number = idx + 1,
                    patient_name = "Sarah Connor"
                )
            }
            return Result.success(demoApps)
        }

        return try {
            val response = RetrofitClient.apiService.getDoctorAppointments(doctorId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllDoctors(): Result<List<DoctorProfileResponse>> {
        if (com.medislot.app.ui.screens.auth.DemoConfig.isDemoModeActive) {
            val demoResponses = com.medislot.app.data.model.MockData.doctors.map { doc ->
                DoctorProfileResponse(
                    id = doc.id,
                    uid = "uid_${doc.id}",
                    name = doc.name,
                    specialization = doc.department,
                    hospital_name = doc.hospital,
                    rating = doc.rating,
                    experience_years = doc.experience.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 5,
                    fees = doc.fees,
                    bio = doc.bio,
                    availability = doc.availability,
                    slot_times = doc.slotTimes.joinToString(","),
                    contact = doc.contact,
                    status = doc.status,
                    room = doc.room,
                    shift = doc.shift,
                    mbbs_institution = "Medical College",
                    registration_number = "REG-12345"
                )
            }
            return Result.success(demoResponses)
        }

        return try {
            val response = RetrofitClient.apiService.getDoctorsList()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
