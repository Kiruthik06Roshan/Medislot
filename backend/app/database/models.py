from sqlalchemy import Column, String, Integer, Float, Boolean, ForeignKey, DateTime, Text
from sqlalchemy.orm import relationship
import datetime
from .connection import Base

class UserModel(Base):
    __tablename__ = "users"
    uid = Column(String, primary_key=True, index=True)
    email = Column(String, unique=True, index=True, nullable=False)
    password_hash = Column(String, nullable=False)
    role = Column(String, nullable=False) # patient, doctor, hospital_coordinator, super_admin
    full_name = Column(String, nullable=False)
    created_at = Column(DateTime, default=datetime.datetime.utcnow)

class PatientModel(Base):
    __tablename__ = "patients"
    id = Column(String, primary_key=True, index=True)
    uid = Column(String, ForeignKey("users.uid"), nullable=False)
    age = Column(Integer, nullable=False, default=0)
    gender = Column(String, nullable=False, default="")
    contact = Column(String, nullable=False, default="")
    blood_group = Column(String, nullable=False, default="")
    height = Column(String, nullable=False, default="")
    weight = Column(String, nullable=False, default="")
    bmi = Column(String, nullable=False, default="")
    allergies = Column(Text, nullable=True) # comma separated
    medications = Column(Text, nullable=True) # comma separated
    medical_history = Column(Text, nullable=True) # comma separated

    # Optional Insurance fields
    insurance_provider = Column(String, nullable=True)
    insurance_plan = Column(String, nullable=True)
    insurance_policy_number = Column(String, nullable=True)
    insurance_expiry = Column(String, nullable=True)

    # Optional Emergency contact fields
    emergency_contact_name = Column(String, nullable=True)
    emergency_contact_phone = Column(String, nullable=True)
    emergency_contact_relation = Column(String, nullable=True)

    # Optional Vitals fields
    vitals_heart_rate = Column(Integer, nullable=True)
    vitals_bp = Column(String, nullable=True)
    vitals_spo2 = Column(Integer, nullable=True)
    vitals_temperature = Column(Float, nullable=True)
    vitals_blood_sugar = Column(Integer, nullable=True)

class DoctorModel(Base):
    __tablename__ = "doctors"
    id = Column(String, primary_key=True, index=True)
    uid = Column(String, ForeignKey("users.uid"), nullable=False)
    specialization = Column(String, nullable=False)
    hospital_id = Column(String, nullable=True)
    hospital_name = Column(String, nullable=False)
    rating = Column(Float, default=4.8)
    experience_years = Column(Integer, nullable=False)
    fees = Column(String, default="$100")
    bio = Column(Text, nullable=True)
    availability = Column(String, default="Monday - Friday")
    slot_times = Column(Text, nullable=True) # comma separated
    contact = Column(String, nullable=False)
    status = Column(String, default="On Duty") # On Duty, Off Duty, With Patient
    room = Column(String, default="Room 2A")
    shift = Column(String, default="Day Shift")
    mbbs_institution = Column(String, nullable=True)
    registration_number = Column(String, nullable=True)

    user = relationship("UserModel")

    @property
    def name(self) -> str:
        return self.user.full_name if self.user else "Dr. Unknown"


class HospitalModel(Base):
    __tablename__ = "hospitals"
    id = Column(String, primary_key=True, index=True)
    name = Column(String, nullable=False)
    uid = Column(String, ForeignKey("users.uid"), nullable=True)
    license_number = Column(String, nullable=False)
    registration_number = Column(String, unique=True, nullable=False)
    address = Column(String, nullable=False)
    hospital_type = Column(String, nullable=False)
    departments = Column(Text, nullable=False) # comma separated
    contact = Column(String, nullable=False)
    admin_name = Column(String, nullable=False)
    status = Column(String, default="Pending") # Pending, Approved, Rejected
    rejection_reason = Column(String, nullable=True)
    docs_attached = Column(String, nullable=True)

    user = relationship("UserModel")

class AppointmentModel(Base):
    __tablename__ = "appointments"
    id = Column(String, primary_key=True, index=True)
    patient_id = Column(String, nullable=False)
    doctor_id = Column(String, nullable=False)
    doctor_name = Column(String, nullable=False)
    department = Column(String, nullable=False)
    hospital = Column(String, nullable=False)
    date = Column(String, nullable=False)
    time = Column(String, nullable=False)
    status = Column(String, default="Upcoming") # Upcoming, Completed, Cancelled
    queue_number = Column(Integer, default=0)

class MedicalRecordModel(Base):
    __tablename__ = "medical_records"
    id = Column(String, primary_key=True, index=True)
    patient_id = Column(String, nullable=False)
    title = Column(String, nullable=False)
    record_type = Column(String, nullable=False) # Lab Report, Prescription, Diagnosis
    date = Column(String, nullable=False)
    file_url = Column(String, nullable=True)
    result_summary = Column(Text, nullable=True)
    doctor_id = Column(String, nullable=True)

class StaffMemberModel(Base):
    __tablename__ = "staff_members"
    id = Column(String, primary_key=True, index=True)
    hospital_name = Column(String, nullable=True)
    name = Column(String, nullable=False)
    role = Column(String, nullable=False) # Doctor, Nurse, Receptionist, Lab Technician, Pharmacist
    department = Column(String, nullable=False)
    room = Column(String, nullable=False)
    status = Column(String, default="On Duty") # On Duty, Off Duty, Leave

class StaffScheduleModel(Base):
    __tablename__ = "staff_schedules"
    id = Column(String, primary_key=True, index=True)
    hospital_name = Column(String, nullable=True)
    name = Column(String, nullable=False)
    role = Column(String, nullable=False)
    department = Column(String, nullable=False)
    date = Column(String, nullable=False) # Day of week (e.g. Monday)
    shift_type = Column(String, nullable=False) # Morning, Afternoon, Night, Emergency, Custom
    shift_time = Column(String, nullable=False)
    room = Column(String, nullable=False)
    status = Column(String, default="On Duty")

class LeaveRequestModel(Base):
    __tablename__ = "leave_requests"
    id = Column(String, primary_key=True, index=True)
    hospital_name = Column(String, nullable=True)
    staff_id = Column(String, nullable=False)
    staff_name = Column(String, nullable=False)
    role = Column(String, nullable=False)
    department = Column(String, nullable=False)
    start_date = Column(String, nullable=False)
    end_date = Column(String, nullable=False)
    reason = Column(String, nullable=False)
    status = Column(String, default="Pending") # Pending, Approved, Rejected

class DoctorApplicationModel(Base):
    __tablename__ = "doctor_applications"
    id = Column(String, primary_key=True, index=True)
    uid = Column(String, ForeignKey("users.uid"), nullable=True)
    name = Column(String, nullable=False)
    specialization = Column(String, nullable=False)
    experience_years = Column(String, nullable=False)
    medical_registration_number = Column(String, unique=True, nullable=False)
    mbbs_institution = Column(String, nullable=False)
    docs_attached = Column(String, nullable=True)
    resume_file = Column(String, nullable=True)
    selected_hospital = Column(String, nullable=False)
    status = Column(String, default="Pending") # Pending, Approved, Rejected, Waiting Documents
    rejection_reason = Column(String, nullable=True)

    user = relationship("UserModel")

class InventoryModel(Base):
    __tablename__ = "inventory"
    id = Column(String, primary_key=True, index=True)
    hospital_name = Column(String, nullable=True)
    name = Column(String, nullable=False)
    total = Column(Integer, nullable=False)
    available = Column(Integer, nullable=False)
    unit = Column(String, nullable=False)
    category = Column(String, nullable=False) # ICU, Staff, Equipment, Gas
    last_updated = Column(String, default="Updated just now")
    trend = Column(String, default="Stable")
    is_trend_positive = Column(Boolean, default=True)

class OperationalAlertModel(Base):
    __tablename__ = "operational_alerts"
    id = Column(String, primary_key=True, index=True)
    hospital_name = Column(String, nullable=True)
    title = Column(String, nullable=False)
    message = Column(Text, nullable=False)
    severity = Column(String, nullable=False) # Critical, High, Medium, Low
    timestamp = Column(String, nullable=False)
    department = Column(String, nullable=False)
    is_resolved = Column(Boolean, default=False)

class AiRequestLogModel(Base):
    __tablename__ = "ai_request_logs"
    id = Column(Integer, primary_key=True, autoincrement=True)
    user_id = Column(String, nullable=True)
    prompt_type = Column(String, nullable=False)
    prompt = Column(Text, nullable=False)
    response = Column(Text, nullable=False)
    latency_ms = Column(Integer, nullable=False)
    model_used = Column(String, nullable=False)
    was_cached = Column(Boolean, default=False)
    timestamp = Column(DateTime, default=datetime.datetime.utcnow)

class AiCacheModel(Base):
    __tablename__ = "ai_cache"
    id = Column(Integer, primary_key=True, autoincrement=True)
    cache_key = Column(String, unique=True, index=True, nullable=False)
    response_data = Column(Text, nullable=False)
    timestamp = Column(DateTime, default=datetime.datetime.utcnow)
