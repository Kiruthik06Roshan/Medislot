package com.medislot.app.network

import retrofit2.http.*

interface ApiService {

    // --- AUTHENTICATION ---
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): TokenResponse

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): TokenResponse

    @POST("api/auth/refresh")
    suspend fun refresh(@Query("refresh_token") refreshToken: String): TokenResponse

    @GET("api/auth/status/{uid}")
    suspend fun getUserStatus(@Path("uid") uid: String): UserStatusResponse


    // --- PATIENTS ---
    @GET("api/patients/profile/{uid}")
    suspend fun getPatientProfile(@Path("uid") uid: String): PatientProfileResponse

    @PUT("api/patients/profile")
    suspend fun updatePatientProfile(@Body request: PatientProfileRequest): PatientProfileResponse

    @GET("api/patients/appointments/{patient_id}")
    suspend fun getPatientAppointments(@Path("patient_id") patientId: String): List<AppointmentResponse>

    @GET("api/patients/medical-records/{patient_id}")
    suspend fun getPatientMedicalRecords(@Path("patient_id") patientId: String): List<MedicalRecordResponse>

    @POST("api/patients/medical-records")
    suspend fun createMedicalRecord(@Body request: MedicalRecordRequest): MedicalRecordResponse


    // --- DOCTORS ---
    @GET("api/doctors/all")
    suspend fun getDoctorsList(): List<DoctorProfileResponse>

    @GET("api/doctors/profile/{uid}")
    suspend fun getDoctorProfile(@Path("uid") uid: String): DoctorProfileResponse

    @PUT("api/doctors/profile")
    suspend fun updateDoctorProfile(@Body request: DoctorProfileRequest): DoctorProfileResponse

    @GET("api/doctors/appointments/{doctor_id}")
    suspend fun getDoctorAppointments(@Path("doctor_id") doctorId: String): List<AppointmentResponse>


    // --- APPOINTMENTS ---
    @POST("api/appointments")
    suspend fun createAppointment(@Body request: AppointmentRequest): AppointmentResponse

    @PUT("api/appointments/{apt_id}/status")
    suspend fun updateAppointmentStatus(
        @Path("apt_id") aptId: String,
        @Query("status") status: String
    ): AppointmentResponse

    @PUT("api/appointments/{apt_id}/reschedule")
    suspend fun rescheduleAppointment(
        @Path("apt_id") aptId: String,
        @Query("date") date: String,
        @Query("time") time: String
    ): AppointmentResponse

    @PUT("api/appointments/{apt_id}/cancel")
    suspend fun cancelAppointment(
        @Path("apt_id") aptId: String
    ): AppointmentResponse



    // --- HOSPITAL OPERATIONS ---
    @GET("api/hospital/profile/{uid}")
    suspend fun getHospitalProfile(@Path("uid") uid: String): HospitalResponse

    @PUT("api/hospital/profile")
    suspend fun updateHospitalProfile(@Body request: HospitalRegisterRequest): HospitalResponse

    @POST("api/hospital/register")
    suspend fun registerHospital(@Body request: HospitalRegisterRequest): HospitalResponse

    @GET("api/hospital/all")
    suspend fun getAllHospitals(): List<HospitalResponse>

    @POST("api/hospital/{hosp_id}/status")
    suspend fun updateHospitalStatus(
        @Path("hosp_id") hospId: String,
        @Query("status") status: String,
        @Query("rejection_reason") rejectionReason: String? = null
    ): HospitalResponse

    @GET("api/hospital/inventory")
    suspend fun getHospitalInventory(): List<InventoryItemResponse>

    @PUT("api/hospital/inventory/{item_id}")
    suspend fun updateInventory(
        @Path("item_id") itemId: String,
        @Query("available") available: Int
    ): InventoryItemResponse

    @GET("api/hospital/alerts")
    suspend fun getHospitalAlerts(): List<OperationalAlertResponse>

    @POST("api/hospital/alerts/{alert_id}/resolve")
    suspend fun resolveAlert(@Path("alert_id") alertId: String): OperationalAlertResponse

    @GET("api/hospital/recruitment")
    suspend fun getRecruitmentApplications(): List<DoctorApplicationResponse>

    @POST("api/hospital/recruitment")
    suspend fun createDoctorApplication(@Body request: DoctorApplicationRequest): DoctorApplicationResponse

    @POST("api/hospital/recruitment/{app_id}/status")
    suspend fun updateApplicationStatus(
        @Path("app_id") appId: String,
        @Query("status") status: String,
        @Query("rejection_reason") rejectionReason: String? = null
    ): DoctorApplicationResponse

    @GET("api/hospital/scheduling")
    suspend fun getStaffScheduling(): List<StaffScheduleResponse>

    @POST("api/hospital/scheduling")
    suspend fun assignStaffShift(@Body request: StaffScheduleRequest): StaffScheduleResponse

    @PUT("api/hospital/scheduling/{sch_id}")
    suspend fun editStaffShift(
        @Path("sch_id") schId: String,
        @Body request: StaffScheduleRequest
    ): StaffScheduleResponse

    @DELETE("api/hospital/scheduling/{sch_id}")
    suspend fun deleteStaffShift(@Path("sch_id") schId: String): Map<String, String>

    @POST("api/hospital/scheduling/duplicate")
    suspend fun duplicateScheduling(): Map<String, String>

    @GET("api/hospital/staff")
    suspend fun getStaffMembers(): List<StaffMemberResponse>

    @GET("api/hospital/leaves")
    suspend fun getLeaveRequests(): List<LeaveRequestResponse>

    @POST("api/hospital/leaves/{lv_id}/status")
    suspend fun updateLeaveStatus(
        @Path("lv_id") lvId: String,
        @Query("status") status: String
    ): LeaveRequestResponse


    // --- AI LOGS & CACHE ---
    @POST("api/ai/log")
    suspend fun logAiRequest(@Body request: AiLogRequest): Map<String, String>

    @GET("api/ai/cache/{cache_key}")
    suspend fun getAiCache(@Path("cache_key") cacheKey: String): Map<String, String>

    @POST("api/ai/cache")
    suspend fun saveAiCache(
        @Query("cache_key") cacheKey: String,
        @Query("response_data") responseData: String
    ): Map<String, String>
}
