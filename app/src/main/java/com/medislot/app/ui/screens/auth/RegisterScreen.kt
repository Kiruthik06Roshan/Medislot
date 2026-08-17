package com.medislot.app.ui.screens.auth

import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.size
import androidx.compose.animation.core.tween
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.ContactPage
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medislot.app.ui.components.DatePickerField
import com.medislot.app.ui.components.DocumentUploadCard
import com.medislot.app.ui.components.GenderSelectorCard
import com.medislot.app.ui.components.MediSlotButton
import com.medislot.app.ui.components.MediSlotCard
import com.medislot.app.ui.components.MediSlotOutlinedButton
import com.medislot.app.ui.components.MediSlotSecondaryButton
import com.medislot.app.ui.components.MediSlotTextButton
import com.medislot.app.ui.components.MediSlotTextField
import com.medislot.app.ui.components.MultiSelectChips
import com.medislot.app.ui.components.SearchableDropdown
import com.medislot.app.ui.components.SingleSelectChips
import com.medislot.app.ui.components.TimelineStepper
import com.medislot.app.utils.ValidationUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.content.Context
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull

// Lists for dropdown selections
private val SPECIALIZATIONS = listOf(
    "Cardiology", "Dermatology", "Endocrinology", "Gastroenterology",
    "General Medicine", "Neurology", "Gynecology", "Pediatrics",
    "Psychiatry", "Orthopedics", "Oncology", "Ophthalmology", "Urology"
)

private val HOSPITALS = listOf(
    "Apollo Hospital", "Fortis Hospital", "MGM Healthcare", "MIOT", "Government General Hospital"
)

private val BLOOD_GROUPS = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")

private val HOSPITAL_TYPES = listOf("Government", "Private", "Multi-Speciality", "Clinic")

private val RELATIONSHIPS = listOf("Parent", "Spouse", "Child", "Sibling", "Friend", "Other")

private suspend fun uploadPdfDocument(context: android.content.Context, uri: android.net.Uri): String? {
    return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri) ?: return@withContext null
            val bytes = inputStream.readBytes()
            inputStream.close()

            var displayName = "uploaded_file.pdf"
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        displayName = it.getString(nameIndex)
                    }
                }
            }

            val requestFile = bytes.toRequestBody("application/pdf".toMediaTypeOrNull(), 0, bytes.size)
            val body = MultipartBody.Part.createFormData("file", displayName, requestFile)
            val response = com.medislot.app.network.RetrofitClient.apiService.uploadDocument(body)
            response.filename
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    role: String,
    onRegisterSuccess: (role: String, hospitalName: String) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var currentStep by remember { mutableStateOf(1) }
    var isLoading by remember { mutableStateOf(false) }

    val totalSteps = when (role) {
        "patient" -> 4
        "doctor" -> 4
        "hospital" -> 5
        else -> 4
    }

    val stepLabels = when (role) {
        "patient" -> listOf("Personal", "Medical", "Emergency", "Confirm")
        "doctor" -> listOf("Personal", "Professional", "Documents", "Confirm")
        "hospital" -> listOf("Admin", "Hospital", "Contact", "Documents", "Confirm")
        else -> listOf("Personal", "Info", "Verify", "Confirm")
    }

    val themeColor = when (role) {
        "patient" -> MaterialTheme.colorScheme.primary
        "doctor" -> MaterialTheme.colorScheme.secondary
        "hospital" -> Color(0xFFF59E0B)
        else -> MaterialTheme.colorScheme.primary
    }

    // Form States - Patient
    var patName by remember { mutableStateOf("") }
    var patUsername by remember { mutableStateOf("") }
    var patEmail by remember { mutableStateOf("") }
    var patPhone by remember { mutableStateOf("") }
    var patDob by remember { mutableStateOf("") }
    var patGender by remember { mutableStateOf("") }
    var patAddress by remember { mutableStateOf("") }
    var patPassword by remember { mutableStateOf("") }
    var patConfirmPassword by remember { mutableStateOf("") }

    var patBloodGroup by remember { mutableStateOf("") }
    var patHeight by remember { mutableStateOf("") }
    var patWeight by remember { mutableStateOf("") }
    var patFoodPreference by remember { mutableStateOf("") }
    var patAllergies by remember { mutableStateOf(listOf<String>()) }
    var patMedicalHistory by remember { mutableStateOf(listOf<String>()) }
    var patCurrentMedications by remember { mutableStateOf("") }
    var patPreviousSurgeries by remember { mutableStateOf("") }
    var patFamilyHistory by remember { mutableStateOf("") }

    var patEmergencyName by remember { mutableStateOf("") }
    var patEmergencyRelationship by remember { mutableStateOf("") }
    var patEmergencyPhone by remember { mutableStateOf("") }

    // Error States - Patient
    var patNameErr by remember { mutableStateOf<String?>(null) }
    var patUsernameErr by remember { mutableStateOf<String?>(null) }
    var patEmailErr by remember { mutableStateOf<String?>(null) }
    var patPhoneErr by remember { mutableStateOf<String?>(null) }
    var patDobErr by remember { mutableStateOf<String?>(null) }
    var patGenderErr by remember { mutableStateOf<String?>(null) }
    var patAddressErr by remember { mutableStateOf<String?>(null) }
    var patPasswordErr by remember { mutableStateOf<String?>(null) }
    var patConfirmPasswordErr by remember { mutableStateOf<String?>(null) }

    var patBloodGroupErr by remember { mutableStateOf<String?>(null) }
    var patHeightErr by remember { mutableStateOf<String?>(null) }
    var patWeightErr by remember { mutableStateOf<String?>(null) }
    var patFoodPreferenceErr by remember { mutableStateOf<String?>(null) }

    var patEmergencyNameErr by remember { mutableStateOf<String?>(null) }
    var patEmergencyRelationshipErr by remember { mutableStateOf<String?>(null) }
    var patEmergencyPhoneErr by remember { mutableStateOf<String?>(null) }


    // Form States - Doctor
    var docName by remember { mutableStateOf("") }
    var docUsername by remember { mutableStateOf("") }
    var docEmail by remember { mutableStateOf("") }
    var docPhone by remember { mutableStateOf("") }
    var docGender by remember { mutableStateOf("") }
    var docDob by remember { mutableStateOf("") }
    var docPassword by remember { mutableStateOf("") }
    var docConfirmPassword by remember { mutableStateOf("") }

    var docRegistrationNumber by remember { mutableStateOf("") }
    var docSpecialization by remember { mutableStateOf("") }
    var docExperience by remember { mutableStateOf("") }
    var docMbbsInstitution by remember { mutableStateOf("") }
    var docMbbsUniversity by remember { mutableStateOf("") }
    var docGraduationYear by remember { mutableStateOf("") }
    var docHospitalName by remember { mutableStateOf("") }
    var docConsultationFee by remember { mutableStateOf("") }
    var docMdInstitution by remember { mutableStateOf("") }
    var docSuperSpecialization by remember { mutableStateOf("") }

    var docMbbsPdfUri by remember { mutableStateOf<Uri?>(null) }
    var docMdPdfUri by remember { mutableStateOf<Uri?>(null) }
    var docCouncilPdfUri by remember { mutableStateOf<Uri?>(null) }
    var docGovtIdPdfUri by remember { mutableStateOf<Uri?>(null) }

    // Error States - Doctor
    var docNameErr by remember { mutableStateOf<String?>(null) }
    var docUsernameErr by remember { mutableStateOf<String?>(null) }
    var docEmailErr by remember { mutableStateOf<String?>(null) }
    var docPhoneErr by remember { mutableStateOf<String?>(null) }
    var docGenderErr by remember { mutableStateOf<String?>(null) }
    var docDobErr by remember { mutableStateOf<String?>(null) }
    var docPasswordErr by remember { mutableStateOf<String?>(null) }
    var docConfirmPasswordErr by remember { mutableStateOf<String?>(null) }

    var docRegNumErr by remember { mutableStateOf<String?>(null) }
    var docSpecializationErr by remember { mutableStateOf<String?>(null) }
    var docExperienceErr by remember { mutableStateOf<String?>(null) }
    var docMbbsInstErr by remember { mutableStateOf<String?>(null) }
    var docMbbsUnivErr by remember { mutableStateOf<String?>(null) }
    var docGradYearErr by remember { mutableStateOf<String?>(null) }
    var docHospitalErr by remember { mutableStateOf<String?>(null) }

    var docMbbsPdfErr by remember { mutableStateOf<String?>(null) }


    // Form States - Hospital Admin
    var adminName by remember { mutableStateOf("") }
    var adminUsername by remember { mutableStateOf("") }
    var adminEmail by remember { mutableStateOf("") }
    var adminPhone by remember { mutableStateOf("") }
    var adminDesignation by remember { mutableStateOf("") }
    var adminPassword by remember { mutableStateOf("") }
    var adminConfirmPassword by remember { mutableStateOf("") }

    var hospName by remember { mutableStateOf("") }
    var hospType by remember { mutableStateOf("") }
    var hospRegistrationNumber by remember { mutableStateOf("") }
    var hospLicenseNumber by remember { mutableStateOf("") }
    var hospEstablishedYear by remember { mutableStateOf("") }

    var hospAddress by remember { mutableStateOf("") }
    var hospCity by remember { mutableStateOf("") }
    var hospState by remember { mutableStateOf("") }
    var hospPinCode by remember { mutableStateOf("") }
    var hospOfficialEmail by remember { mutableStateOf("") }
    var hospOfficialPhone by remember { mutableStateOf("") }
    var hospWebsite by remember { mutableStateOf("") }

    var hospRegPdfUri by remember { mutableStateOf<Uri?>(null) }
    var hospLicensePdfUri by remember { mutableStateOf<Uri?>(null) }
    var hospNabhPdfUri by remember { mutableStateOf<Uri?>(null) }
    var hospOtherPdfUri by remember { mutableStateOf<Uri?>(null) }

    // Error States - Hospital Admin
    var adminNameErr by remember { mutableStateOf<String?>(null) }
    var adminUsernameErr by remember { mutableStateOf<String?>(null) }
    var adminEmailErr by remember { mutableStateOf<String?>(null) }
    var adminPhoneErr by remember { mutableStateOf<String?>(null) }
    var adminDesignationErr by remember { mutableStateOf<String?>(null) }
    var adminPasswordErr by remember { mutableStateOf<String?>(null) }
    var adminConfirmPasswordErr by remember { mutableStateOf<String?>(null) }

    var hospNameErr by remember { mutableStateOf<String?>(null) }
    var hospTypeErr by remember { mutableStateOf<String?>(null) }
    var hospRegNumErr by remember { mutableStateOf<String?>(null) }
    var hospLicenseNumErr by remember { mutableStateOf<String?>(null) }
    var hospEstYearErr by remember { mutableStateOf<String?>(null) }

    var hospAddressErr by remember { mutableStateOf<String?>(null) }
    var hospCityErr by remember { mutableStateOf<String?>(null) }
    var hospStateErr by remember { mutableStateOf<String?>(null) }
    var hospPinErr by remember { mutableStateOf<String?>(null) }
    var hospOfficialEmailErr by remember { mutableStateOf<String?>(null) }
    var hospOfficialPhoneErr by remember { mutableStateOf<String?>(null) }

    var hospRegPdfErr by remember { mutableStateOf<String?>(null) }
    var hospLicensePdfErr by remember { mutableStateOf<String?>(null) }

    // Step Validation Logics
    fun validatePatientStep(step: Int): Boolean {
        var isValid = true
        when (step) {
            1 -> {
                patNameErr = ValidationUtils.validateRequired(patName, "Full Name")
                patUsernameErr = ValidationUtils.validateRequired(patUsername, "Username")
                patEmailErr = ValidationUtils.validateEmail(patEmail)
                patPhoneErr = ValidationUtils.validatePhone(patPhone)
                patDobErr = ValidationUtils.validateRequired(patDob, "Date of Birth")
                patGenderErr = ValidationUtils.validateRequired(patGender, "Gender")
                patAddressErr = ValidationUtils.validateRequired(patAddress, "Address")
                patPasswordErr = ValidationUtils.validatePasswordStrength(patPassword)
                patConfirmPasswordErr = ValidationUtils.validatePasswordConfirm(patPassword, patConfirmPassword)

                isValid = patNameErr == null && patUsernameErr == null && patEmailErr == null &&
                        patPhoneErr == null && patDobErr == null && patGenderErr == null &&
                        patAddressErr == null && patPasswordErr == null && patConfirmPasswordErr == null
            }
            2 -> {
                patBloodGroupErr = ValidationUtils.validateRequired(patBloodGroup, "Blood Group")
                patHeightErr = ValidationUtils.validateRequired(patHeight, "Height")
                patWeightErr = ValidationUtils.validateRequired(patWeight, "Weight")
                patFoodPreferenceErr = ValidationUtils.validateRequired(patFoodPreference, "Food Preference")

                isValid = patBloodGroupErr == null && patHeightErr == null && patWeightErr == null &&
                        patFoodPreferenceErr == null
            }
            3 -> {
                patEmergencyNameErr = ValidationUtils.validateRequired(patEmergencyName, "Emergency Contact Name")
                patEmergencyRelationshipErr = ValidationUtils.validateRequired(patEmergencyRelationship, "Relationship")
                patEmergencyPhoneErr = ValidationUtils.validatePhone(patEmergencyPhone)

                isValid = patEmergencyNameErr == null && patEmergencyRelationshipErr == null && patEmergencyPhoneErr == null
            }
        }
        return isValid
    }

    fun validateDoctorStep(step: Int): Boolean {
        var isValid = true
        when (step) {
            1 -> {
                docNameErr = ValidationUtils.validateRequired(docName, "Full Name")
                docUsernameErr = ValidationUtils.validateRequired(docUsername, "Username")
                docEmailErr = ValidationUtils.validateEmail(docEmail)
                docPhoneErr = ValidationUtils.validatePhone(docPhone)
                docGenderErr = ValidationUtils.validateRequired(docGender, "Gender")
                docDobErr = ValidationUtils.validateRequired(docDob, "Date of Birth")
                docPasswordErr = ValidationUtils.validatePasswordStrength(docPassword)
                docConfirmPasswordErr = ValidationUtils.validatePasswordConfirm(docPassword, docConfirmPassword)

                isValid = docNameErr == null && docUsernameErr == null && docEmailErr == null &&
                        docPhoneErr == null && docGenderErr == null && docDobErr == null &&
                        docPasswordErr == null && docConfirmPasswordErr == null
            }
            2 -> {
                docRegNumErr = ValidationUtils.validateRequired(docRegistrationNumber, "Medical Registration Number")
                docSpecializationErr = ValidationUtils.validateRequired(docSpecialization, "Specialization")
                docExperienceErr = ValidationUtils.validateRequired(docExperience, "Years of Experience")
                docMbbsInstErr = ValidationUtils.validateRequired(docMbbsInstitution, "MBBS Institution")
                docMbbsUnivErr = ValidationUtils.validateRequired(docMbbsUniversity, "MBBS University")
                docGradYearErr = ValidationUtils.validateRequired(docGraduationYear, "Graduation Year")
                docHospitalErr = ValidationUtils.validateRequired(docHospitalName, "Hospital Selection")

                isValid = docRegNumErr == null && docSpecializationErr == null && docExperienceErr == null &&
                        docMbbsInstErr == null && docMbbsUnivErr == null && docGradYearErr == null &&
                        docHospitalErr == null
            }
            3 -> {
                docMbbsPdfErr = null
            }
        }
        return isValid
    }

    fun validateHospitalStep(step: Int): Boolean {
        var isValid = true
        when (step) {
            1 -> {
                adminNameErr = ValidationUtils.validateRequired(adminName, "Full Name")
                adminUsernameErr = ValidationUtils.validateRequired(adminUsername, "Username")
                adminEmailErr = ValidationUtils.validateEmail(adminEmail)
                adminPhoneErr = ValidationUtils.validatePhone(adminPhone)
                adminDesignationErr = ValidationUtils.validateRequired(adminDesignation, "Designation")
                adminPasswordErr = ValidationUtils.validatePasswordStrength(adminPassword)
                adminConfirmPasswordErr = ValidationUtils.validatePasswordConfirm(adminPassword, adminConfirmPassword)

                isValid = adminNameErr == null && adminUsernameErr == null && adminEmailErr == null &&
                        adminPhoneErr == null && adminDesignationErr == null && adminPasswordErr == null &&
                        adminConfirmPasswordErr == null
            }
            2 -> {
                hospNameErr = ValidationUtils.validateRequired(hospName, "Hospital Name")
                hospTypeErr = ValidationUtils.validateRequired(hospType, "Hospital Type")
                hospRegNumErr = ValidationUtils.validateRequired(hospRegistrationNumber, "Hospital Registration Number")
                hospLicenseNumErr = ValidationUtils.validateRequired(hospLicenseNumber, "Hospital License Number")
                hospEstYearErr = ValidationUtils.validateRequired(hospEstablishedYear, "Established Year")

                isValid = hospNameErr == null && hospTypeErr == null && hospRegNumErr == null &&
                        hospLicenseNumErr == null && hospEstYearErr == null
            }
            3 -> {
                hospAddressErr = ValidationUtils.validateRequired(hospAddress, "Hospital Address")
                hospCityErr = ValidationUtils.validateRequired(hospCity, "City")
                hospStateErr = ValidationUtils.validateRequired(hospState, "State")
                
                hospPinErr = if (hospPinCode.isBlank()) {
                    "PIN Code is required"
                } else if (!hospPinCode.matches("^[0-9]{6}$".toRegex())) {
                    "PIN Code must be exactly 6 digits"
                } else null

                hospOfficialEmailErr = ValidationUtils.validateEmail(hospOfficialEmail)
                hospOfficialPhoneErr = ValidationUtils.validatePhone(hospOfficialPhone)

                isValid = hospAddressErr == null && hospCityErr == null && hospStateErr == null &&
                        hospPinErr == null && hospOfficialEmailErr == null && hospOfficialPhoneErr == null
            }
            4 -> {
                hospRegPdfErr = null
                hospLicensePdfErr = null
            }
        }
        return isValid
    }

    fun handleNextStep() {
        val isStepValid = when (role) {
            "patient" -> validatePatientStep(currentStep)
            "doctor" -> validateDoctorStep(currentStep)
            "hospital" -> validateHospitalStep(currentStep)
            else -> true
        }

        if (isStepValid) {
            if (currentStep < totalSteps) {
                currentStep += 1
            } else {
                // Final submission
                coroutineScope.launch {
                    isLoading = true
                    val repo = com.medislot.app.data.repository.AuthenticationRepositoryImpl()
                    val result = repo.register(
                        email = if (role == "patient") patEmail else if (role == "doctor") docEmail else adminEmail,
                        password = if (role == "patient") patPassword else if (role == "doctor") docPassword else adminPassword,
                        fullName = if (role == "patient") patName else if (role == "doctor") docName else adminName,
                        role = role
                    )
                    
                    if (result.isFailure) {
                        isLoading = false
                        val errMsg = com.medislot.app.utils.NetworkErrorUtils.getReadableErrorMessage(result.exceptionOrNull())
                        android.widget.Toast.makeText(context, errMsg, android.widget.Toast.LENGTH_LONG).show()
                        return@launch
                    }
                    
                    // Save profile details to backend & Room DB
                    val registeredUid = result.getOrNull()?.uid ?: ""
                    if (registeredUid.isNotEmpty()) {
                        if (role == "patient") {
                            try {
                                val patRepo = com.medislot.app.data.repository.PatientRepositoryImpl()
                                patRepo.updateProfile(
                                    com.medislot.app.network.PatientProfileRequest(
                                        uid = registeredUid,
                                        age = if (patDob.length >= 4) (2026 - (patDob.takeLast(4).toIntOrNull() ?: 2000)) else 21,
                                        gender = if (patGender.isBlank()) "Male" else patGender,
                                        contact = if (patPhone.isBlank()) "+1 (555) 000-0000" else patPhone,
                                        blood_group = if (patBloodGroup.isBlank()) "B+" else patBloodGroup,
                                        height = if (patHeight.isBlank()) "175 cm" else patHeight,
                                        weight = if (patWeight.isBlank()) "68 kg" else patWeight,
                                        bmi = "22.2",
                                        allergies = if (patAllergies.isEmpty()) "None" else patAllergies.joinToString(", "),
                                        medications = if (patCurrentMedications.isBlank()) "None" else patCurrentMedications,
                                        medical_history = if (patMedicalHistory.isEmpty()) "None" else patMedicalHistory.joinToString(", ")
                                    )
                                )
                            } catch (e: Exception) {
                                // Silent fail fallback to Room
                            }
                        } else if (role == "doctor") {
                            try {
                                val docRepo = com.medislot.app.data.repository.DoctorRepositoryImpl()
                                docRepo.updateProfile(
                                    com.medislot.app.network.DoctorProfileRequest(
                                        uid = registeredUid,
                                        specialization = docSpecialization,
                                        hospital_name = docHospitalName,
                                        experience_years = docExperience.toIntOrNull() ?: 5,
                                        contact = if (docPhone.isBlank()) "+1 (555) 000-0000" else docPhone,
                                        mbbs_institution = docMbbsInstitution,
                                        registration_number = docRegistrationNumber
                                    )
                                )
                                val doctorDocs = mutableListOf<String>()
                                docMbbsPdfUri?.let { uri ->
                                    val uploadedFilename = uploadPdfDocument(context, uri)
                                    doctorDocs.add(uploadedFilename ?: "MBBS_Degree.pdf")
                                }
                                docMdPdfUri?.let { uri ->
                                    val uploadedFilename = uploadPdfDocument(context, uri)
                                    doctorDocs.add(uploadedFilename ?: "MD_MS_Degree.pdf")
                                }
                                docCouncilPdfUri?.let { uri ->
                                    val uploadedFilename = uploadPdfDocument(context, uri)
                                    doctorDocs.add(uploadedFilename ?: "Medical_Council_Registration.pdf")
                                }
                                docGovtIdPdfUri?.let { uri ->
                                    val uploadedFilename = uploadPdfDocument(context, uri)
                                    doctorDocs.add(uploadedFilename ?: "Government_ID_Proof.pdf")
                                }
                                val docsAttachedStr = if (doctorDocs.isNotEmpty()) doctorDocs.joinToString(",") else ""

                                com.medislot.app.network.RetrofitClient.apiService.createDoctorApplication(
                                    com.medislot.app.network.DoctorApplicationRequest(
                                        uid = registeredUid,
                                        name = docName,
                                        specialization = docSpecialization,
                                        experience_years = docExperience,
                                        medical_registration_number = docRegistrationNumber,
                                        mbbs_institution = docMbbsInstitution,
                                        docs_attached = docsAttachedStr,
                                        resume_file = if (doctorDocs.isNotEmpty()) "resume.pdf" else "",
                                        selected_hospital = docHospitalName
                                    )
                                )
                            } catch (e: Exception) {
                                // Fallback
                            }
                            if (com.medislot.app.ui.screens.auth.DemoConfig.isDemoModeActive) {
                                val doctorDocs = mutableListOf<String>()
                                docMbbsPdfUri?.let { uri ->
                                    val uploadedFilename = uploadPdfDocument(context, uri)
                                    doctorDocs.add(uploadedFilename ?: "MBBS_Degree.pdf")
                                }
                                docMdPdfUri?.let { uri ->
                                    val uploadedFilename = uploadPdfDocument(context, uri)
                                    doctorDocs.add(uploadedFilename ?: "MD_MS_Degree.pdf")
                                }
                                docCouncilPdfUri?.let { uri ->
                                    val uploadedFilename = uploadPdfDocument(context, uri)
                                    doctorDocs.add(uploadedFilename ?: "Medical_Council_Registration.pdf")
                                }
                                docGovtIdPdfUri?.let { uri ->
                                    val uploadedFilename = uploadPdfDocument(context, uri)
                                    doctorDocs.add(uploadedFilename ?: "Government_ID_Proof.pdf")
                                }
                                val docsAttachedStr = if (doctorDocs.isNotEmpty()) doctorDocs.joinToString(",") else ""
                                com.medislot.app.data.model.VerificationStateStore.addDoctorApplication(
                                    name = docName,
                                    spec = docSpecialization,
                                    hospital = docHospitalName,
                                    exp = docExperience,
                                    filename = docsAttachedStr
                                )
                            }
                        } else if (role == "hospital") {
                            try {
                                val hospDocs = mutableListOf<String>()
                                hospRegPdfUri?.let { uri ->
                                    val uploadedFilename = uploadPdfDocument(context, uri)
                                    hospDocs.add(uploadedFilename ?: "Hospital_Registration_Certificate.pdf")
                                }
                                hospLicensePdfUri?.let { uri ->
                                    val uploadedFilename = uploadPdfDocument(context, uri)
                                    hospDocs.add(uploadedFilename ?: "Government_License_Certificate.pdf")
                                }
                                hospNabhPdfUri?.let { uri ->
                                    val uploadedFilename = uploadPdfDocument(context, uri)
                                    hospDocs.add(uploadedFilename ?: "NABH_Accreditation_Certificate.pdf")
                                }
                                hospOtherPdfUri?.let { uri ->
                                    val uploadedFilename = uploadPdfDocument(context, uri)
                                    hospDocs.add(uploadedFilename ?: "Other_Accreditation_Certificates.pdf")
                                }
                                val hospDocsStr = if (hospDocs.isNotEmpty()) hospDocs.joinToString(",") else ""

                                com.medislot.app.network.RetrofitClient.apiService.registerHospital(
                                    com.medislot.app.network.HospitalRegisterRequest(
                                        name = hospName,
                                        uid = registeredUid,
                                        license_number = hospLicenseNumber,
                                        registration_number = hospRegistrationNumber,
                                        address = "$hospAddress, $hospCity, $hospState - $hospPinCode",
                                        hospital_type = hospType,
                                        departments = "General,Emergency,ICU",
                                        contact = hospOfficialPhone,
                                        admin_name = adminName,
                                        docs_attached = hospDocsStr
                                    )
                                )
                            } catch (e: Exception) {
                                // Fallback
                            }
                            if (com.medislot.app.ui.screens.auth.DemoConfig.isDemoModeActive) {
                                val hospDocs = mutableListOf<String>()
                                hospRegPdfUri?.let { uri ->
                                    val uploadedFilename = uploadPdfDocument(context, uri)
                                    hospDocs.add(uploadedFilename ?: "Hospital_Registration_Certificate.pdf")
                                }
                                hospLicensePdfUri?.let { uri ->
                                    val uploadedFilename = uploadPdfDocument(context, uri)
                                    hospDocs.add(uploadedFilename ?: "Government_License_Certificate.pdf")
                                }
                                hospNabhPdfUri?.let { uri ->
                                    val uploadedFilename = uploadPdfDocument(context, uri)
                                    hospDocs.add(uploadedFilename ?: "NABH_Accreditation_Certificate.pdf")
                                }
                                hospOtherPdfUri?.let { uri ->
                                    val uploadedFilename = uploadPdfDocument(context, uri)
                                    hospDocs.add(uploadedFilename ?: "Other_Accreditation_Certificates.pdf")
                                }
                                val hospDocsStr = if (hospDocs.isNotEmpty()) hospDocs.joinToString(",") else ""
                                com.medislot.app.data.model.VerificationStateStore.addHospitalApplication(
                                    name = hospName,
                                    regNum = hospRegistrationNumber,
                                    license = hospLicenseNumber,
                                    adminName = adminName,
                                    contact = hospOfficialPhone,
                                    docsAttached = hospDocsStr
                                )
                            }
                        }
                    }
                    
                    isLoading = false
                    onRegisterSuccess(role, if (role == "doctor") docHospitalName else "None")
                }
            }
        }
    }

    fun handlePrevStep() {
        if (currentStep > 1) {
            currentStep -= 1
        } else {
            onNavigateToLogin()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Create Account",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { handlePrevStep() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .imePadding()
        ) {
            // Stepper timeline
            TimelineStepper(
                currentStep = currentStep,
                totalSteps = totalSteps,
                stepLabels = stepLabels,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            // Step contents (animated transitions)
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) togetherWith
                            fadeOut(animationSpec = tween(90)) using
                            SizeTransform(clip = false)
                },
                label = "StepAnimation",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { targetStep ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    when (role) {
                        "patient" -> {
                            when (targetStep) {
                                1 -> PatientStep1(
                                    name = patName, onNameChg = { patName = it; patNameErr = null }, nameErr = patNameErr,
                                    username = patUsername, onUsernameChg = { patUsername = it; patUsernameErr = null }, usernameErr = patUsernameErr,
                                    email = patEmail, onEmailChg = { patEmail = it; patEmailErr = null }, emailErr = patEmailErr,
                                    phone = patPhone, onPhoneChg = { patPhone = it; patPhoneErr = null }, phoneErr = patPhoneErr,
                                    dob = patDob, onDobChg = { patDob = it; patDobErr = null }, dobErr = patDobErr,
                                    gender = patGender, onGenderChg = { patGender = it; patGenderErr = null }, genderErr = patGenderErr,
                                    address = patAddress, onAddressChg = { patAddress = it; patAddressErr = null }, addressErr = patAddressErr,
                                    password = patPassword, onPasswordChg = { patPassword = it; patPasswordErr = null }, passwordErr = patPasswordErr,
                                    confirm = patConfirmPassword, onConfirmChg = { patConfirmPassword = it; patConfirmPasswordErr = null }, confirmErr = patConfirmPasswordErr
                                )
                                2 -> PatientStep2(
                                    blood = patBloodGroup, onBloodChg = { patBloodGroup = it; patBloodGroupErr = null }, bloodErr = patBloodGroupErr,
                                    height = patHeight, onHeightChg = { patHeight = it; patHeightErr = null }, heightErr = patHeightErr,
                                    weight = patWeight, onWeightChg = { patWeight = it; patWeightErr = null }, weightErr = patWeightErr,
                                    food = patFoodPreference, onFoodChg = { patFoodPreference = it; patFoodPreferenceErr = null }, foodErr = patFoodPreferenceErr,
                                    allergies = patAllergies, onAllergiesChg = { patAllergies = it },
                                    history = patMedicalHistory, onHistoryChg = { patMedicalHistory = it },
                                    meds = patCurrentMedications, onMedsChg = { patCurrentMedications = it },
                                    surgeries = patPreviousSurgeries, onSurgeriesChg = { patPreviousSurgeries = it },
                                    family = patFamilyHistory, onFamilyChg = { patFamilyHistory = it },
                                    onSkip = {
                                        // Skip step 2 validations and clear errors
                                        patBloodGroupErr = null
                                        patHeightErr = null
                                        patWeightErr = null
                                        patFoodPreferenceErr = null
                                        currentStep = 3
                                    }
                                )
                                3 -> PatientStep3(
                                    name = patEmergencyName, onNameChg = { patEmergencyName = it; patEmergencyNameErr = null }, nameErr = patEmergencyNameErr,
                                    rel = patEmergencyRelationship, onRelChg = { patEmergencyRelationship = it; patEmergencyRelationshipErr = null }, relErr = patEmergencyRelationshipErr,
                                    phone = patEmergencyPhone, onPhoneChg = { patEmergencyPhone = it; patEmergencyPhoneErr = null }, phoneErr = patEmergencyPhoneErr
                                )
                                4 -> ReviewStep(
                                    summaryDetails = listOf(
                                        "Name" to patName,
                                        "Username" to patUsername,
                                        "Email" to patEmail,
                                        "Phone" to patPhone,
                                        "Gender" to patGender,
                                        "Blood Group" to (if (patBloodGroup.isBlank()) "Skipped" else patBloodGroup),
                                        "Emergency Contact" to "$patEmergencyName ($patEmergencyRelationship)"
                                    )
                                )
                            }
                        }
                        "doctor" -> {
                            when (targetStep) {
                                1 -> DoctorStep1(
                                    name = docName, onNameChg = { docName = it; docNameErr = null }, nameErr = docNameErr,
                                    username = docUsername, onUsernameChg = { docUsername = it; docUsernameErr = null }, usernameErr = docUsernameErr,
                                    email = docEmail, onEmailChg = { docEmail = it; docEmailErr = null }, emailErr = docEmailErr,
                                    phone = docPhone, onPhoneChg = { docPhone = it; docPhoneErr = null }, phoneErr = docPhoneErr,
                                    gender = docGender, onGenderChg = { docGender = it; docGenderErr = null }, genderErr = docGenderErr,
                                    dob = docDob, onDobChg = { docDob = it; docDobErr = null }, dobErr = docDobErr,
                                    password = docPassword, onPasswordChg = { docPassword = it; docPasswordErr = null }, passwordErr = docPasswordErr,
                                    confirm = docConfirmPassword, onConfirmChg = { docConfirmPassword = it; docConfirmPasswordErr = null }, confirmErr = docConfirmPasswordErr
                                )
                                2 -> DoctorStep2(
                                    regNum = docRegistrationNumber, onRegNumChg = { docRegistrationNumber = it; docRegNumErr = null }, regNumErr = docRegNumErr,
                                    spec = docSpecialization, onSpecChg = { docSpecialization = it; docSpecializationErr = null }, specErr = docSpecializationErr,
                                    exp = docExperience, onExpChg = { docExperience = it; docExperienceErr = null }, expErr = docExperienceErr,
                                    mbbsInst = docMbbsInstitution, onMbbsInstChg = { docMbbsInstitution = it; docMbbsInstErr = null }, mbbsInstErr = docMbbsInstErr,
                                    mbbsUniv = docMbbsUniversity, onMbbsUnivChg = { docMbbsUniversity = it; docMbbsUnivErr = null }, mbbsUnivErr = docMbbsUnivErr,
                                    gradYear = docGraduationYear, onGradYearChg = { docGraduationYear = it; docGradYearErr = null }, gradYearErr = docGradYearErr,
                                    hospital = docHospitalName, onHospitalChg = { docHospitalName = it; docHospitalErr = null }, hospitalErr = docHospitalErr,
                                    fee = docConsultationFee, onFeeChg = { docConsultationFee = it },
                                    mdInst = docMdInstitution, onMdInstChg = { docMdInstitution = it },
                                    superSpec = docSuperSpecialization, onSuperSpecChg = { docSuperSpecialization = it }
                                )
                                3 -> DoctorStep3(
                                    mbbsUri = docMbbsPdfUri, onMbbsUriSelected = { docMbbsPdfUri = it; docMbbsPdfErr = null }, mbbsErr = docMbbsPdfErr,
                                    mdUri = docMdPdfUri, onMdUriSelected = { docMdPdfUri = it },
                                    councilUri = docCouncilPdfUri, onCouncilUriSelected = { docCouncilPdfUri = it },
                                    govtIdUri = docGovtIdPdfUri, onGovtIdUriSelected = { docGovtIdPdfUri = it }
                                )
                                4 -> ReviewStep(
                                    disclaimer = "Your account will be verified before activation.",
                                    summaryDetails = listOf(
                                        "Doctor Name" to docName,
                                        "Username" to docUsername,
                                        "Medical Registration No." to docRegistrationNumber,
                                        "Specialization" to docSpecialization,
                                        "Experience" to "$docExperience Years",
                                        "Degree Verification" to (if (docMbbsPdfUri != null) "MBBS Degree PDF Attached" else "Not Attached")
                                    )
                                )
                            }
                        }
                        "hospital" -> {
                            when (targetStep) {
                                1 -> HospitalStep1(
                                    name = adminName, onNameChg = { adminName = it; adminNameErr = null }, nameErr = adminNameErr,
                                    username = adminUsername, onUsernameChg = { adminUsername = it; adminUsernameErr = null }, usernameErr = adminUsernameErr,
                                    email = adminEmail, onEmailChg = { adminEmail = it; adminEmailErr = null }, emailErr = adminEmailErr,
                                    phone = adminPhone, onPhoneChg = { adminPhone = it; adminPhoneErr = null }, phoneErr = adminPhoneErr,
                                    desig = adminDesignation, onDesigChg = { adminDesignation = it; adminDesignationErr = null }, desigErr = adminDesignationErr,
                                    password = adminPassword, onPasswordChg = { adminPassword = it; adminPasswordErr = null }, passwordErr = adminPasswordErr,
                                    confirm = adminConfirmPassword, onConfirmChg = { adminConfirmPassword = it; adminConfirmPasswordErr = null }, confirmErr = adminConfirmPasswordErr
                                )
                                2 -> HospitalStep2(
                                    name = hospName, onNameChg = { hospName = it; hospNameErr = null }, nameErr = hospNameErr,
                                    type = hospType, onTypeChg = { hospType = it; hospTypeErr = null }, typeErr = hospTypeErr,
                                    regNum = hospRegistrationNumber, onRegNumChg = { hospRegistrationNumber = it; hospRegNumErr = null }, regNumErr = hospRegNumErr,
                                    license = hospLicenseNumber, onLicenseChg = { hospLicenseNumber = it; hospLicenseNumErr = null }, licenseErr = hospLicenseNumErr,
                                    estYear = hospEstablishedYear, onEstYearChg = { hospEstablishedYear = it; hospEstYearErr = null }, estYearErr = hospEstYearErr
                                )
                                3 -> HospitalStep3(
                                    addr = hospAddress, onAddrChg = { hospAddress = it; hospAddressErr = null }, addrErr = hospAddressErr,
                                    city = hospCity, onCityChg = { hospCity = it; hospCityErr = null }, cityErr = hospCityErr,
                                    state = hospState, onStateChg = { hospState = it; hospStateErr = null }, stateErr = hospStateErr,
                                    pin = hospPinCode, onPinChg = { hospPinCode = it; hospPinErr = null }, pinErr = hospPinErr,
                                    email = hospOfficialEmail, onEmailChg = { hospOfficialEmail = it; hospOfficialEmailErr = null }, emailErr = hospOfficialEmailErr,
                                    phone = hospOfficialPhone, onPhoneChg = { hospOfficialPhone = it; hospOfficialPhoneErr = null }, phoneErr = hospOfficialPhoneErr,
                                    web = hospWebsite, onWebChg = { hospWebsite = it }
                                )
                                4 -> HospitalStep4(
                                    regUri = hospRegPdfUri, onRegUriSelected = { hospRegPdfUri = it; hospRegPdfErr = null }, regErr = hospRegPdfErr,
                                    licenseUri = hospLicensePdfUri, onLicenseUriSelected = { hospLicensePdfUri = it; hospLicensePdfErr = null }, licenseErr = hospLicensePdfErr,
                                    nabhUri = hospNabhPdfUri, onNabhUriSelected = { hospNabhPdfUri = it },
                                    otherUri = hospOtherPdfUri, onOtherUriSelected = { hospOtherPdfUri = it }
                                )
                                5 -> ReviewStep(
                                    disclaimer = "Your hospital account will be reviewed and verified before approval.",
                                    summaryDetails = listOf(
                                        "Admin In-charge" to adminName,
                                        "Designation" to adminDesignation,
                                        "Hospital Name" to hospName,
                                        "Hospital Type" to hospType,
                                        "Official Email" to hospOfficialEmail,
                                        "Address" to "$hospAddress, $hospCity, $hospState - $hospPinCode",
                                        "Documents" to "Hospital Reg & License PDFs Attached"
                                    )
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Stepper Bottom Buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MediSlotOutlinedButton(
                            text = if (currentStep == 1) "Cancel" else "Previous",
                            onClick = { handlePrevStep() },
                            modifier = Modifier.weight(1f)
                        )
                        MediSlotButton(
                            text = if (currentStep == totalSteps) "Create Account" else "Continue",
                            onClick = { handleNextStep() },
                            isLoading = isLoading,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// PATIENT REGISTRATION STEP SUB-COMPOSABLES
// ==========================================

@Composable
fun PatientStep1(
    name: String, onNameChg: (String) -> Unit, nameErr: String?,
    username: String, onUsernameChg: (String) -> Unit, usernameErr: String?,
    email: String, onEmailChg: (String) -> Unit, emailErr: String?,
    phone: String, onPhoneChg: (String) -> Unit, phoneErr: String?,
    dob: String, onDobChg: (String) -> Unit, dobErr: String?,
    gender: String, onGenderChg: (String) -> Unit, genderErr: String?,
    address: String, onAddressChg: (String) -> Unit, addressErr: String?,
    password: String, onPasswordChg: (String) -> Unit, passwordErr: String?,
    confirm: String, onConfirmChg: (String) -> Unit, confirmErr: String?
) {
    MediSlotCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Personal Details", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            
            MediSlotTextField(value = name, onValueChange = onNameChg, label = "Full Name", leadingIcon = Icons.Default.Person, errorMessage = nameErr)
            MediSlotTextField(value = username, onValueChange = onUsernameChg, label = "Username", leadingIcon = Icons.Default.Person, errorMessage = usernameErr)
            MediSlotTextField(value = email, onValueChange = onEmailChg, label = "Email Address", leadingIcon = Icons.Default.Email, errorMessage = emailErr, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next))
            MediSlotTextField(value = phone, onValueChange = onPhoneChg, label = "Mobile Number", leadingIcon = Icons.Default.Phone, errorMessage = phoneErr, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next))
            
            DatePickerField(label = "Date of Birth", selectedValue = dob, onDateSelected = onDobChg, errorMessage = dobErr)
            GenderSelectorCard(selectedValue = gender, onSelected = onGenderChg, errorMessage = genderErr)
            
            MediSlotTextField(value = address, onValueChange = onAddressChg, label = "Address", leadingIcon = Icons.Default.Home, errorMessage = addressErr)
            MediSlotTextField(value = password, onValueChange = onPasswordChg, label = "Password", leadingIcon = Icons.Default.Lock, isPasswordField = true, errorMessage = passwordErr, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next))
            MediSlotTextField(value = confirm, onValueChange = onConfirmChg, label = "Confirm Password", leadingIcon = Icons.Default.Lock, isPasswordField = true, errorMessage = confirmErr, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done))
        }
    }
}

@Composable
fun PatientStep2(
    blood: String, onBloodChg: (String) -> Unit, bloodErr: String?,
    height: String, onHeightChg: (String) -> Unit, heightErr: String?,
    weight: String, onWeightChg: (String) -> Unit, weightErr: String?,
    food: String, onFoodChg: (String) -> Unit, foodErr: String?,
    allergies: List<String>, onAllergiesChg: (List<String>) -> Unit,
    history: List<String>, onHistoryChg: (List<String>) -> Unit,
    meds: String, onMedsChg: (String) -> Unit,
    surgeries: String, onSurgeriesChg: (String) -> Unit,
    family: String, onFamilyChg: (String) -> Unit,
    onSkip: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Skip Section Display
        MediSlotCard(modifier = Modifier.fillMaxWidth()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "You can skip this section and update it later from your profile.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                MediSlotTextButton(text = "Skip Medical History", onClick = onSkip)
            }
        }
        
        MediSlotCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Medical Profile", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                SearchableDropdown(label = "Blood Group", searchPlaceholder = "Search blood group...", options = BLOOD_GROUPS, selectedValue = blood, onSelected = onBloodChg, errorMessage = bloodErr)
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MediSlotTextField(value = height, onValueChange = onHeightChg, label = "Height (cm)", modifier = Modifier.weight(1f), errorMessage = heightErr, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next))
                    MediSlotTextField(value = weight, onValueChange = onWeightChg, label = "Weight (kg)", modifier = Modifier.weight(1f), errorMessage = weightErr, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next))
                }

                SingleSelectChips(title = "Food Preference", options = listOf("Vegetarian", "Non-Vegetarian", "Vegan"), selectedValue = food, onSelected = onFoodChg, errorMessage = foodErr)

                MultiSelectChips(
                    title = "Allergies (Optional)",
                    options = listOf("None", "Medicine Allergy", "Food Allergy", "Dust Allergy"),
                    selectedValues = allergies,
                    onSelectedValuesChanged = onAllergiesChg
                )

                MultiSelectChips(
                    title = "Medical History (Optional)",
                    options = listOf("Diabetes", "Hypertension", "Asthma", "Heart Disease", "Thyroid", "Kidney Disease"),
                    selectedValues = history,
                    onSelectedValuesChanged = onHistoryChg
                )

                MediSlotTextField(value = meds, onValueChange = onMedsChg, label = "Current Medications (Optional)")
                MediSlotTextField(value = surgeries, onValueChange = onSurgeriesChg, label = "Previous Surgeries (Optional)")
                MediSlotTextField(value = family, onValueChange = onFamilyChg, label = "Family Medical History (Optional)")
            }
        }
    }
}

@Composable
fun PatientStep3(
    name: String, onNameChg: (String) -> Unit, nameErr: String?,
    rel: String, onRelChg: (String) -> Unit, relErr: String?,
    phone: String, onPhoneChg: (String) -> Unit, phoneErr: String?
) {
    MediSlotCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Emergency Contact", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            MediSlotTextField(value = name, onValueChange = onNameChg, label = "Emergency Contact Name", leadingIcon = Icons.Default.Person, errorMessage = nameErr)
            SearchableDropdown(label = "Relationship", searchPlaceholder = "Search relationship...", options = RELATIONSHIPS, selectedValue = rel, onSelected = onRelChg, errorMessage = relErr)
            MediSlotTextField(value = phone, onValueChange = onPhoneChg, label = "Emergency Contact Number", leadingIcon = Icons.Default.Phone, errorMessage = phoneErr, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done))
        }
    }
}

// ==========================================
// DOCTOR REGISTRATION STEP SUB-COMPOSABLES
// ==========================================

@Composable
fun DoctorStep1(
    name: String, onNameChg: (String) -> Unit, nameErr: String?,
    username: String, onUsernameChg: (String) -> Unit, usernameErr: String?,
    email: String, onEmailChg: (String) -> Unit, emailErr: String?,
    phone: String, onPhoneChg: (String) -> Unit, phoneErr: String?,
    gender: String, onGenderChg: (String) -> Unit, genderErr: String?,
    dob: String, onDobChg: (String) -> Unit, dobErr: String?,
    password: String, onPasswordChg: (String) -> Unit, passwordErr: String?,
    confirm: String, onConfirmChg: (String) -> Unit, confirmErr: String?
) {
    MediSlotCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Personal Information", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            MediSlotTextField(value = name, onValueChange = onNameChg, label = "Full Name", leadingIcon = Icons.Default.Person, errorMessage = nameErr)
            MediSlotTextField(value = username, onValueChange = onUsernameChg, label = "Username", leadingIcon = Icons.Default.Person, errorMessage = usernameErr)
            MediSlotTextField(value = email, onValueChange = onEmailChg, label = "Email", leadingIcon = Icons.Default.Email, errorMessage = emailErr, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next))
            MediSlotTextField(value = phone, onValueChange = onPhoneChg, label = "Mobile Number", leadingIcon = Icons.Default.Phone, errorMessage = phoneErr, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next))
            
            GenderSelectorCard(selectedValue = gender, onSelected = onGenderChg, errorMessage = genderErr)
            DatePickerField(label = "Date of Birth", selectedValue = dob, onDateSelected = onDobChg, errorMessage = dobErr)
            
            MediSlotTextField(value = password, onValueChange = onPasswordChg, label = "Password", leadingIcon = Icons.Default.Lock, isPasswordField = true, errorMessage = passwordErr, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next))
            MediSlotTextField(value = confirm, onValueChange = onConfirmChg, label = "Confirm Password", leadingIcon = Icons.Default.Lock, isPasswordField = true, errorMessage = confirmErr, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done))
        }
    }
}

@Composable
fun DoctorStep2(
    regNum: String, onRegNumChg: (String) -> Unit, regNumErr: String?,
    spec: String, onSpecChg: (String) -> Unit, specErr: String?,
    exp: String, onExpChg: (String) -> Unit, expErr: String?,
    mbbsInst: String, onMbbsInstChg: (String) -> Unit, mbbsInstErr: String?,
    mbbsUniv: String, onMbbsUnivChg: (String) -> Unit, mbbsUnivErr: String?,
    gradYear: String, onGradYearChg: (String) -> Unit, gradYearErr: String?,
    hospital: String, onHospitalChg: (String) -> Unit, hospitalErr: String?,
    fee: String, onFeeChg: (String) -> Unit,
    mdInst: String, onMdInstChg: (String) -> Unit,
    superSpec: String, onSuperSpecChg: (String) -> Unit
) {
    MediSlotCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Professional Information", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            MediSlotTextField(value = regNum, onValueChange = onRegNumChg, label = "Medical Registration Number", leadingIcon = Icons.Default.Work, errorMessage = regNumErr)
            SearchableDropdown(label = "Specialization", searchPlaceholder = "Search specialization...", options = SPECIALIZATIONS, selectedValue = spec, onSelected = onSpecChg, errorMessage = specErr)
            MediSlotTextField(value = exp, onValueChange = onExpChg, label = "Years of Experience", leadingIcon = Icons.Default.Work, errorMessage = expErr, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next))
            
            MediSlotTextField(value = mbbsInst, onValueChange = onMbbsInstChg, label = "MBBS Institution", leadingIcon = Icons.Default.Home, errorMessage = mbbsInstErr)
            MediSlotTextField(value = mbbsUniv, onValueChange = onMbbsUnivChg, label = "MBBS University", leadingIcon = Icons.Default.Home, errorMessage = mbbsUnivErr)
            MediSlotTextField(value = gradYear, onValueChange = onGradYearChg, label = "Graduation Year", leadingIcon = Icons.Default.CalendarMonth, errorMessage = gradYearErr, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next))
            
            SearchableDropdown(label = "Select Hospital", searchPlaceholder = "Search hospital...", options = HOSPITALS, selectedValue = hospital, onSelected = onHospitalChg, errorMessage = hospitalErr)
            MediSlotTextField(value = fee, onValueChange = onFeeChg, label = "Consultation Fee (Optional)", leadingIcon = Icons.Default.Work, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next))
            MediSlotTextField(value = mdInst, onValueChange = onMdInstChg, label = "MD/MS Institution (Optional)")
            MediSlotTextField(value = superSpec, onValueChange = onSuperSpecChg, label = "Super Specialization (Optional)")
        }
    }
}

@Composable
fun DoctorStep3(
    mbbsUri: Uri?, onMbbsUriSelected: (Uri?) -> Unit, mbbsErr: String?,
    mdUri: Uri?, onMdUriSelected: (Uri?) -> Unit,
    councilUri: Uri?, onCouncilUriSelected: (Uri?) -> Unit,
    govtIdUri: Uri?, onGovtIdUriSelected: (Uri?) -> Unit
) {
    MediSlotCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Verification Documents", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            
            Text(
                text = "Accepted Format: PDF • Maximum Size: 10 MB",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            DocumentUploadCard(
                title = "MBBS Degree Certificate (Optional)",
                subtitle = "Optional verification document",
                onFileSelected = onMbbsUriSelected,
                maxSizeBytes = 10 * 1024 * 1024,
                errorMessage = mbbsErr
            )

            DocumentUploadCard(
                title = "MD/MS Degree Certificate (Optional)",
                subtitle = "Attached MD/MS documents",
                onFileSelected = onMdUriSelected,
                maxSizeBytes = 10 * 1024 * 1024
            )

            DocumentUploadCard(
                title = "Medical Council Registration (Optional)",
                subtitle = "Active registration proof",
                onFileSelected = onCouncilUriSelected,
                maxSizeBytes = 10 * 1024 * 1024
            )

            DocumentUploadCard(
                title = "Government ID Proof (Optional)",
                subtitle = "Aadhaar, Passport, or driving license",
                onFileSelected = onGovtIdUriSelected,
                maxSizeBytes = 10 * 1024 * 1024
            )
        }
    }
}

// ==========================================
// HOSPITAL REGISTRATION STEP SUB-COMPOSABLES
// ==========================================

@Composable
fun HospitalStep1(
    name: String, onNameChg: (String) -> Unit, nameErr: String?,
    username: String, onUsernameChg: (String) -> Unit, usernameErr: String?,
    email: String, onEmailChg: (String) -> Unit, emailErr: String?,
    phone: String, onPhoneChg: (String) -> Unit, phoneErr: String?,
    desig: String, onDesigChg: (String) -> Unit, desigErr: String?,
    password: String, onPasswordChg: (String) -> Unit, passwordErr: String?,
    confirm: String, onConfirmChg: (String) -> Unit, confirmErr: String?
) {
    MediSlotCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Administrator Information", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            MediSlotTextField(value = name, onValueChange = onNameChg, label = "Full Name", leadingIcon = Icons.Default.Person, errorMessage = nameErr)
            MediSlotTextField(value = username, onValueChange = onUsernameChg, label = "Username", leadingIcon = Icons.Default.Person, errorMessage = usernameErr)
            MediSlotTextField(value = email, onValueChange = onEmailChg, label = "Email", leadingIcon = Icons.Default.Email, errorMessage = emailErr, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next))
            MediSlotTextField(value = phone, onValueChange = onPhoneChg, label = "Mobile Number", leadingIcon = Icons.Default.Phone, errorMessage = phoneErr, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next))
            MediSlotTextField(value = desig, onValueChange = onDesigChg, label = "Designation", leadingIcon = Icons.Default.Work, errorMessage = desigErr)
            
            MediSlotTextField(value = password, onValueChange = onPasswordChg, label = "Password", leadingIcon = Icons.Default.Lock, isPasswordField = true, errorMessage = passwordErr, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next))
            MediSlotTextField(value = confirm, onValueChange = onConfirmChg, label = "Confirm Password", leadingIcon = Icons.Default.Lock, isPasswordField = true, errorMessage = confirmErr, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done))
        }
    }
}

@Composable
fun HospitalStep2(
    name: String, onNameChg: (String) -> Unit, nameErr: String?,
    type: String, onTypeChg: (String) -> Unit, typeErr: String?,
    regNum: String, onRegNumChg: (String) -> Unit, regNumErr: String?,
    license: String, onLicenseChg: (String) -> Unit, licenseErr: String?,
    estYear: String, onEstYearChg: (String) -> Unit, estYearErr: String?
) {
    MediSlotCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Hospital Information", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            MediSlotTextField(value = name, onValueChange = onNameChg, label = "Hospital Name", leadingIcon = Icons.Default.LocalHospital, errorMessage = nameErr)
            SearchableDropdown(label = "Hospital Type", searchPlaceholder = "Search type...", options = HOSPITAL_TYPES, selectedValue = type, onSelected = onTypeChg, errorMessage = typeErr)
            
            MediSlotTextField(value = regNum, onValueChange = onRegNumChg, label = "Hospital Registration Number", leadingIcon = Icons.Default.Business, errorMessage = regNumErr)
            MediSlotTextField(value = license, onValueChange = onLicenseChg, label = "Hospital License Number", leadingIcon = Icons.Default.Business, errorMessage = licenseErr)
            MediSlotTextField(value = estYear, onValueChange = onEstYearChg, label = "Established Year", leadingIcon = Icons.Default.CalendarMonth, errorMessage = estYearErr, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done))
        }
    }
}

@Composable
fun HospitalStep3(
    addr: String, onAddrChg: (String) -> Unit, addrErr: String?,
    city: String, onCityChg: (String) -> Unit, cityErr: String?,
    state: String, onStateChg: (String) -> Unit, stateErr: String?,
    pin: String, onPinChg: (String) -> Unit, pinErr: String?,
    email: String, onEmailChg: (String) -> Unit, emailErr: String?,
    phone: String, onPhoneChg: (String) -> Unit, phoneErr: String?,
    web: String, onWebChg: (String) -> Unit
) {
    MediSlotCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Contact Information", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            MediSlotTextField(value = addr, onValueChange = onAddrChg, label = "Hospital Address", leadingIcon = Icons.Default.Home, errorMessage = addrErr)
            MediSlotTextField(value = city, onValueChange = onCityChg, label = "City", leadingIcon = Icons.Default.Home, errorMessage = cityErr)
            MediSlotTextField(value = state, onValueChange = onStateChg, label = "State", leadingIcon = Icons.Default.Home, errorMessage = stateErr)
            MediSlotTextField(value = pin, onValueChange = onPinChg, label = "PIN Code", leadingIcon = Icons.Default.Home, errorMessage = pinErr, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next))
            
            MediSlotTextField(value = email, onValueChange = onEmailChg, label = "Official Email", leadingIcon = Icons.Default.Email, errorMessage = emailErr, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next))
            MediSlotTextField(value = phone, onValueChange = onPhoneChg, label = "Official Phone Number", leadingIcon = Icons.Default.Phone, errorMessage = phoneErr, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next))
            MediSlotTextField(value = web, onValueChange = onWebChg, label = "Hospital Website (Optional)", leadingIcon = Icons.Default.ContactPage, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Done))
        }
    }
}

@Composable
fun HospitalStep4(
    regUri: Uri?, onRegUriSelected: (Uri?) -> Unit, regErr: String?,
    licenseUri: Uri?, onLicenseUriSelected: (Uri?) -> Unit, licenseErr: String?,
    nabhUri: Uri?, onNabhUriSelected: (Uri?) -> Unit,
    otherUri: Uri?, onOtherUriSelected: (Uri?) -> Unit
) {
    MediSlotCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Verification Documents", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            
            Text(
                text = "Accepted Format: PDF • Maximum Size: 20 MB",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            DocumentUploadCard(
                title = "Hospital Registration Certificate (Optional)",
                subtitle = "Optional verification document",
                onFileSelected = onRegUriSelected,
                maxSizeBytes = 20 * 1024 * 1024,
                errorMessage = regErr
            )

            DocumentUploadCard(
                title = "Government License Certificate (Optional)",
                subtitle = "Optional verification document",
                onFileSelected = onLicenseUriSelected,
                maxSizeBytes = 20 * 1024 * 1024,
                errorMessage = licenseErr
            )

            DocumentUploadCard(
                title = "NABH Accreditation Certificate (Optional)",
                subtitle = "Accreditation board document",
                onFileSelected = onNabhUriSelected,
                maxSizeBytes = 20 * 1024 * 1024
            )

            DocumentUploadCard(
                title = "Other Accreditation Certificates (Optional)",
                subtitle = "Iso, state-level awards or proofs",
                onFileSelected = onOtherUriSelected,
                maxSizeBytes = 20 * 1024 * 1024
            )
        }
    }
}

// ==========================================
// FINAL CONFIRMATION / SUMMARY SUB-COMPOSABLE
// ==========================================

@Composable
fun ReviewStep(
    summaryDetails: List<Pair<String, String>>,
    disclaimer: String? = null
) {
    MediSlotCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Review Details", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            
            summaryDetails.forEach { (label, value) ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
            }
            
            if (disclaimer != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MedicalServices,
                        contentDescription = "Info",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = disclaimer,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
