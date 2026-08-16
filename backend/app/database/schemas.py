from pydantic import BaseModel, EmailStr
from typing import List, Optional

class TokenResponse(BaseModel):
    access_token: str
    refresh_token: str
    token_type: str = "bearer"
    uid: str
    role: str
    email: str

class UserLogin(BaseModel):
    email: str
    password: str

class UserRegister(BaseModel):
    email: str
    password: str
    full_name: str
    role: str # patient, doctor, hospital_coordinator

class PatientRegister(BaseModel):
    uid: str
    age: Optional[int] = 0
    gender: Optional[str] = ""
    contact: Optional[str] = ""
    blood_group: Optional[str] = ""
    height: Optional[str] = ""
    weight: Optional[str] = ""
    bmi: Optional[str] = ""
    allergies: Optional[str] = None
    medications: Optional[str] = None
    medical_history: Optional[str] = None
    insurance_provider: Optional[str] = None
    insurance_plan: Optional[str] = None
    insurance_policy_number: Optional[str] = None
    insurance_expiry: Optional[str] = None
    emergency_contact_name: Optional[str] = None
    emergency_contact_phone: Optional[str] = None
    emergency_contact_relation: Optional[str] = None
    vitals_heart_rate: Optional[int] = None
    vitals_bp: Optional[str] = None
    vitals_spo2: Optional[int] = None
    vitals_temperature: Optional[float] = None
    vitals_blood_sugar: Optional[int] = None

class PatientResponse(BaseModel):
    id: str
    uid: str
    age: int
    gender: str
    contact: str
    blood_group: str
    height: str
    weight: str
    bmi: str
    allergies: Optional[str] = None
    medications: Optional[str] = None
    medical_history: Optional[str] = None
    insurance_provider: Optional[str] = None
    insurance_plan: Optional[str] = None
    insurance_policy_number: Optional[str] = None
    insurance_expiry: Optional[str] = None
    emergency_contact_name: Optional[str] = None
    emergency_contact_phone: Optional[str] = None
    emergency_contact_relation: Optional[str] = None
    vitals_heart_rate: Optional[int] = None
    vitals_bp: Optional[str] = None
    vitals_spo2: Optional[int] = None
    vitals_temperature: Optional[float] = None
    vitals_blood_sugar: Optional[int] = None
    class Config:
        orm_mode = True

class DoctorRegister(BaseModel):
    uid: str
    specialization: str
    hospital_name: str
    experience_years: int
    contact: str
    mbbs_institution: str
    registration_number: str
    slot_times: Optional[str] = None

class DoctorResponse(BaseModel):
    id: str
    uid: str
    name: str
    specialization: str
    hospital_name: str
    rating: float
    experience_years: int
    fees: str
    bio: Optional[str]
    availability: str
    slot_times: Optional[str]
    contact: str
    status: str
    room: str
    shift: str
    mbbs_institution: Optional[str] = None
    registration_number: Optional[str] = None
    class Config:
        orm_mode = True

class AppointmentCreate(BaseModel):
    patient_id: str
    doctor_id: str
    doctor_name: str
    department: str
    hospital: str
    date: str
    time: str

class AppointmentResponse(BaseModel):
    id: str
    patient_id: str
    doctor_id: str
    doctor_name: str
    department: str
    hospital: str
    date: str
    time: str
    status: str
    queue_number: int
    patient_name: Optional[str] = None
    class Config:
        orm_mode = True

class MedicalRecordCreate(BaseModel):
    patient_id: str
    title: str
    record_type: str
    date: str
    file_url: Optional[str] = None
    result_summary: Optional[str] = None
    doctor_id: Optional[str] = None

class MedicalRecordResponse(BaseModel):
    id: str
    patient_id: str
    title: str
    record_type: str
    date: str
    file_url: Optional[str]
    result_summary: Optional[str]
    doctor_id: Optional[str]
    class Config:
        orm_mode = True

class StaffMemberCreate(BaseModel):
    name: str
    role: str
    department: str
    room: str

class StaffMemberResponse(BaseModel):
    id: str
    name: str
    role: str
    department: str
    room: str
    status: str
    class Config:
        orm_mode = True

class StaffScheduleCreate(BaseModel):
    name: str
    role: str
    department: str
    date: str
    shift_type: str
    shift_time: str
    room: str
    status: str

class StaffScheduleResponse(BaseModel):
    id: str
    name: str
    role: str
    department: str
    date: str
    shift_type: str
    shift_time: str
    room: str
    status: str
    class Config:
        orm_mode = True

class LeaveRequestCreate(BaseModel):
    staff_id: str
    staff_name: str
    role: str
    department: str
    start_date: str
    end_date: str
    reason: str

class LeaveRequestResponse(BaseModel):
    id: str
    staff_id: str
    staff_name: str
    role: str
    department: str
    start_date: str
    end_date: str
    reason: str
    status: str
    class Config:
        orm_mode = True

class DoctorApplicationCreate(BaseModel):
    uid: Optional[str] = None
    name: str
    specialization: str
    experience_years: str
    medical_registration_number: str
    mbbs_institution: str
    docs_attached: Optional[str] = None
    resume_file: Optional[str] = None
    selected_hospital: str

class DoctorApplicationResponse(BaseModel):
    id: str
    uid: Optional[str] = None
    name: str
    specialization: str
    experience_years: str
    medical_registration_number: str
    mbbs_institution: str
    docs_attached: Optional[str]
    resume_file: Optional[str]
    selected_hospital: str
    status: str
    rejection_reason: Optional[str]
    class Config:
        orm_mode = True

class InventoryItemResponse(BaseModel):
    id: str
    name: str
    total: int
    available: int
    unit: str
    category: str
    last_updated: str
    trend: str
    is_trend_positive: bool
    class Config:
        orm_mode = True

class OperationalAlertResponse(BaseModel):
    id: str
    title: str
    message: str
    severity: str
    timestamp: str
    department: str
    is_resolved: bool
    class Config:
        orm_mode = True

class AiLogRequest(BaseModel):
    prompt_type: str
    prompt: str
    response: str
    latency_ms: int
    model_used: str
    was_cached: bool

class HospitalRegister(BaseModel):
    name: str
    uid: str
    license_number: str
    registration_number: str
    address: str
    hospital_type: str
    departments: str
    contact: str
    admin_name: str
    docs_attached: Optional[str] = None

class HospitalResponse(BaseModel):
    id: str
    name: str
    uid: Optional[str]
    license_number: str
    registration_number: str
    address: str
    hospital_type: str
    departments: str
    contact: str
    admin_name: str
    status: str
    rejection_reason: Optional[str]
    docs_attached: Optional[str]
    class Config:
        orm_mode = True

