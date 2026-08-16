from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from typing import List
import uuid

from ..database.connection import get_db
from ..database.models import UserModel, PatientModel, AppointmentModel, MedicalRecordModel
from ..database.schemas import PatientRegister, PatientResponse, AppointmentResponse, MedicalRecordResponse, MedicalRecordCreate

router = APIRouter(prefix="/api/patients", tags=["Patients"])

@router.get("/profile/{uid}", response_model=PatientResponse)
async def get_patient_profile(uid: str, db: AsyncSession = Depends(get_db)):
    query = select(PatientModel).where(PatientModel.uid == uid)
    result = await db.execute(query)
    patient = result.scalars().first()
    if not patient:
        u_query = select(UserModel).where(UserModel.uid == uid)
        u_res = await db.execute(u_query)
        user = u_res.scalars().first()
        if not user:
            raise HTTPException(status_code=404, detail="Patient profile not found")
        patient = PatientModel(
            id="pat_" + str(uuid.uuid4())[:8],
            uid=uid,
            age=0,
            gender="",
            contact="",
            blood_group="",
            height="",
            weight="",
            bmi="",
            allergies="",
            medications="",
            medical_history=""
        )
        db.add(patient)
        await db.commit()
        await db.refresh(patient)
    return patient

@router.put("/profile", response_model=PatientResponse)
async def update_patient_profile(payload: PatientRegister, db: AsyncSession = Depends(get_db)):
    query = select(PatientModel).where(PatientModel.uid == payload.uid)
    result = await db.execute(query)
    patient = result.scalars().first()
    if not patient:
        patient = PatientModel(
            id="pat_" + str(uuid.uuid4())[:8],
            uid=payload.uid
        )
        db.add(patient)
    
    if payload.age is not None: patient.age = payload.age
    if payload.gender is not None: patient.gender = payload.gender
    if payload.contact is not None: patient.contact = payload.contact
    if payload.blood_group is not None: patient.blood_group = payload.blood_group
    if payload.height is not None: patient.height = payload.height
    if payload.weight is not None: patient.weight = payload.weight
    if payload.bmi is not None: patient.bmi = payload.bmi
    if payload.allergies is not None: patient.allergies = payload.allergies
    if payload.medications is not None: patient.medications = payload.medications
    if payload.medical_history is not None: patient.medical_history = payload.medical_history

    if payload.insurance_provider is not None: patient.insurance_provider = payload.insurance_provider
    if payload.insurance_plan is not None: patient.insurance_plan = payload.insurance_plan
    if payload.insurance_policy_number is not None: patient.insurance_policy_number = payload.insurance_policy_number
    if payload.insurance_expiry is not None: patient.insurance_expiry = payload.insurance_expiry

    if payload.emergency_contact_name is not None: patient.emergency_contact_name = payload.emergency_contact_name
    if payload.emergency_contact_phone is not None: patient.emergency_contact_phone = payload.emergency_contact_phone
    if payload.emergency_contact_relation is not None: patient.emergency_contact_relation = payload.emergency_contact_relation

    if payload.vitals_heart_rate is not None: patient.vitals_heart_rate = payload.vitals_heart_rate
    if payload.vitals_bp is not None: patient.vitals_bp = payload.vitals_bp
    if payload.vitals_spo2 is not None: patient.vitals_spo2 = payload.vitals_spo2
    if payload.vitals_temperature is not None: patient.vitals_temperature = payload.vitals_temperature
    if payload.vitals_blood_sugar is not None: patient.vitals_blood_sugar = payload.vitals_blood_sugar
    
    await db.commit()
    await db.refresh(patient)
    return patient

@router.get("/appointments/{patient_id}", response_model=List[AppointmentResponse])
async def get_patient_appointments(patient_id: str, db: AsyncSession = Depends(get_db)):
    pat_query = select(PatientModel).where((PatientModel.uid == patient_id) | (PatientModel.id == patient_id))
    pat_res = await db.execute(pat_query)
    patient = pat_res.scalars().first()

    if patient:
        target_ids = list({patient.uid, patient.id, patient_id})
    else:
        target_ids = [patient_id]

    query = select(AppointmentModel).where(AppointmentModel.patient_id.in_(target_ids))
    result = await db.execute(query)
    return result.scalars().all()

@router.get("/medical-records/{patient_id}", response_model=List[MedicalRecordResponse])
async def get_patient_medical_records(patient_id: str, db: AsyncSession = Depends(get_db)):
    pat_query = select(PatientModel).where((PatientModel.uid == patient_id) | (PatientModel.id == patient_id))
    pat_res = await db.execute(pat_query)
    patient = pat_res.scalars().first()

    if patient:
        target_ids = list({patient.uid, patient.id, patient_id})
    else:
        target_ids = [patient_id]

    query = select(MedicalRecordModel).where(MedicalRecordModel.patient_id.in_(target_ids))
    result = await db.execute(query)
    return result.scalars().all()

@router.post("/medical-records", response_model=MedicalRecordResponse)
async def create_medical_record(payload: MedicalRecordCreate, db: AsyncSession = Depends(get_db)):
    record = MedicalRecordModel(
        id="rec_" + str(uuid.uuid4())[:8],
        patient_id=payload.patient_id,
        title=payload.title,
        record_type=payload.record_type,
        date=payload.date,
        file_url=payload.file_url,
        result_summary=payload.result_summary,
        doctor_id=payload.doctor_id
    )
    db.add(record)
    await db.commit()
    await db.refresh(record)
    return record
