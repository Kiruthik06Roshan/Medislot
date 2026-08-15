from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from typing import List
import uuid

from ..database.connection import get_db
from ..database.models import PatientModel, AppointmentModel, MedicalRecordModel
from ..database.schemas import PatientRegister, PatientResponse, AppointmentResponse, MedicalRecordResponse, MedicalRecordCreate

router = APIRouter(prefix="/api/patients", tags=["Patients"])

@router.get("/profile/{uid}", response_model=PatientResponse)
async def get_patient_profile(uid: str, db: AsyncSession = Depends(get_db)):
    query = select(PatientModel).where(PatientModel.uid == uid)
    result = await db.execute(query)
    patient = result.scalars().first()
    if not patient:
        # Create a default patient profile to support onboarding
        patient = PatientModel(
            id="pat_" + str(uuid.uuid4())[:8],
            uid=uid,
            age=29,
            gender="Female",
            contact="+1 (555) 019-2834",
            blood_group="O-Positive (O+)",
            height="168 cm",
            weight="58 kg",
            bmi="20.5",
            allergies="Penicillin",
            medications="Multivitamin Active",
            medical_history="Mild Hypertension"
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
    
    patient.age = payload.age
    patient.gender = payload.gender
    patient.contact = payload.contact
    patient.blood_group = payload.blood_group
    patient.height = payload.height
    patient.weight = payload.weight
    patient.bmi = payload.bmi
    patient.allergies = payload.allergies
    patient.medications = payload.medications
    patient.medical_history = payload.medical_history
    
    await db.commit()
    await db.refresh(patient)
    return patient

@router.get("/appointments/{patient_id}", response_model=List[AppointmentResponse])
async def get_patient_appointments(patient_id: str, db: AsyncSession = Depends(get_db)):
    query = select(AppointmentModel).where(AppointmentModel.patient_id == patient_id)
    result = await db.execute(query)
    return result.scalars().all()

@router.get("/medical-records/{patient_id}", response_model=List[MedicalRecordResponse])
async def get_patient_medical_records(patient_id: str, db: AsyncSession = Depends(get_db)):
    query = select(MedicalRecordModel).where(MedicalRecordModel.patient_id == patient_id)
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
