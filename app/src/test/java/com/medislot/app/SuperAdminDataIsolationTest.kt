package com.medislot.app

import com.medislot.app.data.model.*
import com.medislot.app.data.repository.SuperAdminRepositoryImpl
import com.medislot.app.network.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import kotlinx.coroutines.runBlocking

class SuperAdminDataIsolationTest {

    private open class FakeApiService : ApiService {
        override suspend fun register(request: RegisterRequest): TokenResponse = TODO()
        override suspend fun login(request: LoginRequest): TokenResponse = TODO()
        override suspend fun refresh(refreshToken: String): TokenResponse = TODO()
        override suspend fun getUserStatus(uid: String): UserStatusResponse = TODO()
        override suspend fun getPatientProfile(uid: String): PatientProfileResponse = TODO()
        override suspend fun updatePatientProfile(request: PatientProfileRequest): PatientProfileResponse = TODO()
        override suspend fun getPatientAppointments(patientId: String): List<AppointmentResponse> = TODO()
        override suspend fun getPatientMedicalRecords(patientId: String): List<MedicalRecordResponse> = TODO()
        override suspend fun createMedicalRecord(request: MedicalRecordRequest): MedicalRecordResponse = TODO()
        override suspend fun getDoctorsList(): List<DoctorProfileResponse> = TODO()
        override suspend fun getDoctorProfile(uid: String): DoctorProfileResponse = TODO()
        override suspend fun updateDoctorProfile(request: DoctorProfileRequest): DoctorProfileResponse = TODO()
        override suspend fun getDoctorAppointments(doctorId: String): List<AppointmentResponse> = TODO()
        override suspend fun createAppointment(request: AppointmentRequest): AppointmentResponse = TODO()
        override suspend fun updateAppointmentStatus(aptId: String, status: String): AppointmentResponse = TODO()
        override suspend fun rescheduleAppointment(aptId: String, date: String, time: String): AppointmentResponse = TODO()
        override suspend fun cancelAppointment(aptId: String): AppointmentResponse = TODO()
        override suspend fun getHospitalProfile(uid: String): HospitalResponse = TODO()
        override suspend fun updateHospitalProfile(request: HospitalRegisterRequest): HospitalResponse = TODO()
        override suspend fun registerHospital(request: HospitalRegisterRequest): HospitalResponse = TODO()
        
        override suspend fun getAllHospitals(): List<HospitalResponse> = emptyList()
        override suspend fun updateHospitalStatus(hospId: String, status: String, rejectionReason: String?): HospitalResponse = TODO()
        
        override suspend fun getHospitalInventory(): List<InventoryItemResponse> = TODO()
        override suspend fun updateInventory(itemId: String, available: Int): InventoryItemResponse = TODO()
        override suspend fun getHospitalAlerts(): List<OperationalAlertResponse> = TODO()
        override suspend fun resolveAlert(alertId: String): OperationalAlertResponse = TODO()
        
        override suspend fun getRecruitmentApplications(): List<DoctorApplicationResponse> = emptyList()
        override suspend fun createDoctorApplication(request: DoctorApplicationRequest): DoctorApplicationResponse = TODO()
        override suspend fun updateApplicationStatus(appId: String, status: String, rejectionReason: String?): DoctorApplicationResponse = TODO()
        
        override suspend fun getStaffScheduling(): List<StaffScheduleResponse> = TODO()
        override suspend fun assignStaffShift(request: StaffScheduleRequest): StaffScheduleResponse = TODO()
        override suspend fun deleteStaffShift(schId: String): Map<String, String> = TODO()
        override suspend fun duplicateScheduling(): Map<String, String> = TODO()
        override suspend fun getLeaveRequests(): List<LeaveRequestResponse> = TODO()
        override suspend fun updateLeaveStatus(lvId: String, status: String): LeaveRequestResponse = TODO()
        override suspend fun logAiRequest(request: AiLogRequest): Map<String, String> = TODO()
        override suspend fun getAiCache(cacheKey: String): Map<String, String> = TODO()
        override suspend fun saveAiCache(cacheKey: String, responseData: String): Map<String, String> = TODO()
    }

    @Before
    fun setUp() {
        // Reset state store before each test
        VerificationStateStore.reset()
    }

    @Test
    fun testRealKalkiCredentialsRemovedFromDemoMode() {
        // Ensure Kalki does not exist in VerificationStateStore demo data
        val hasKalkiHospital = VerificationStateStore.hospitalApplications.any {
            it.name.contains("kalki", ignoreCase = true) ||
            it.adminName.contains("kalki", ignoreCase = true) ||
            it.contact.contains("kalki", ignoreCase = true)
        }
        assertFalse("Kalki coordinator must NOT be in the demo hospital application list", hasKalkiHospital)

        val hasApolloHospital = VerificationStateStore.hospitalApplications.any {
            it.name.contains("Apollo Hospital", ignoreCase = true)
        }
        assertFalse("Apollo Hospital must NOT be in the demo list since it belongs to Kalki's real registration data", hasApolloHospital)
        
        val hasDemoHospital = VerificationStateStore.hospitalApplications.any {
            it.name == "Demo Hospital" && it.adminName == "Demo Coordinator" && it.contact == "demo@hospital.com"
        }
        assertTrue("Demo Hospital must be seeded as synthetic demo data instead of Apollo", hasDemoHospital)
    }

    @Test
    fun testNormalModeGetHospitalsDoesNotAccessVerificationStateStore() = runBlocking {
        // Prepare API Service returning fake hospital
        val fakeApi = object : FakeApiService() {
            override suspend fun getAllHospitals(): List<HospitalResponse> {
                return listOf(
                    HospitalResponse(
                        id = "hosp_4cc21ab0",
                        name = "Apollo Hospital",
                        uid = "499b195c-5bb1-4b17-a00e-c595ec582cd9",
                        license_number = "LIC-992",
                        registration_number = "REG-991",
                        address = "Address A",
                        hospital_type = "Multi-Specialty",
                        departments = "General",
                        contact = "kalki@gmail.com",
                        admin_name = "kalki",
                        status = "Pending",
                        rejection_reason = "",
                        docs_attached = "License_Doc.pdf"
                    )
                )
            }
        }

        val repository = SuperAdminRepositoryImpl(fakeApi)
        
        // Save initial state of VerificationStateStore
        val initialHospitalCount = VerificationStateStore.hospitalApplications.size
        
        // Execute Normal Mode repository call
        val result = repository.getHospitals()
        
        // Assertions
        assertTrue(result.isSuccess)
        val hospitalsList = result.getOrThrow()
        assertEquals(1, hospitalsList.size)
        assertEquals("Apollo Hospital", hospitalsList[0].name)
        assertEquals("kalki", hospitalsList[0].adminName)
        
        // Check that VerificationStateStore remains UNTOUCHED
        assertEquals("VerificationStateStore.hospitalApplications must not be written/changed", initialHospitalCount, VerificationStateStore.hospitalApplications.size)
        
        val statusLookup = VerificationStateStore.userVerificationStatus["Apollo Hospital"]
        assertNull("Apollo Hospital status must not leak into VerificationStateStore maps", statusLookup)
    }

    @Test
    fun testNormalModeApiFailureDoesNotFallbackToMockData() = runBlocking {
        val fakeApi = object : FakeApiService() {
            override suspend fun getAllHospitals(): List<HospitalResponse> {
                throw Exception("FastAPI is offline")
            }
        }

        val repository = SuperAdminRepositoryImpl(fakeApi)
        val result = repository.getHospitals()

        // Assert failure is produced correctly
        assertTrue(result.isFailure)
        assertEquals("FastAPI is offline", result.exceptionOrNull()?.message)
        
        // Assert VerificationStateStore was not touched
        val hasDemoHospital = VerificationStateStore.hospitalApplications.any { it.name == "Demo Hospital" }
        assertTrue("VerificationStateStore must still have its seeded Demo Hospital", hasDemoHospital)
    }
}
