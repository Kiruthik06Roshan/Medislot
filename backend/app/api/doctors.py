from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from sqlalchemy.orm import selectinload
from typing import List
import uuid

from ..database.connection import get_db
from ..database.models import DoctorModel, AppointmentModel, UserModel
from ..database.schemas import DoctorRegister, DoctorResponse, AppointmentResponse

router = APIRouter(prefix="/api/doctors", tags=["Doctors"])

@router.get("/all", response_model=List[DoctorResponse])
async def get_all_doctors(db: AsyncSession = Depends(get_db)):
    query = select(DoctorModel).options(selectinload(DoctorModel.user))
    result = await db.execute(query)
    doctors = result.scalars().all()
    if not doctors:
        # Seed user profiles first
        user_1 = UserModel(uid="uid_doc_1", email="john.doe@medislot.com", hashed_password="seed_password", full_name="Dr. John Doe", role="doctor")
        user_2 = UserModel(uid="uid_doc_2", email="helen.cho@medislot.com", hashed_password="seed_password", full_name="Dr. Helen Cho", role="doctor")
        user_3 = UserModel(uid="uid_doc_3", email="marcus.vance@medislot.com", hashed_password="seed_password", full_name="Dr. Marcus Vance", role="doctor")
        
        # Check if they exist to avoid duplicate key violations
        for u in [user_1, user_2, user_3]:
            exist_res = await db.execute(select(UserModel).where(UserModel.uid == u.uid))
            if not exist_res.scalars().first():
                db.add(u)
        await db.commit()

        # Seeding a default list of doctors to ensure search works
        seed_data = [
            DoctorModel(
                id="doc_1", uid="uid_doc_1", specialization="Cardiology",
                hospital_name="City General Hospital", rating=4.9, experience_years=14,
                fees="$100", bio="Cardiology specialist", availability="Monday - Friday",
                slot_times="09:00 AM,10:30 AM,11:00 AM,02:30 PM,04:00 PM", contact="+1 (555) 123-4567"
            ),
            DoctorModel(
                id="doc_2", uid="uid_doc_2", specialization="Neurology",
                hospital_name="Metro Health Medical Center", rating=4.8, experience_years=10,
                fees="$120", bio="Neurology specialist", availability="Tuesday & Thursday",
                slot_times="09:30 AM,10:00 AM,01:30 PM,03:00 PM", contact="+1 (555) 234-5678"
            ),
            DoctorModel(
                id="doc_3", uid="uid_doc_3", specialization="Orthopedics",
                hospital_name="City General Hospital", rating=4.7, experience_years=12,
                fees="$90", bio="Orthopedics specialist", availability="Monday, Wednesday, Friday",
                slot_times="10:00 AM,11:30 AM,03:30 PM,04:30 PM", contact="+1 (555) 345-6789"
            )
        ]
        for d in seed_data:
            db.add(d)
        await db.commit()
        
        query = select(DoctorModel).options(selectinload(DoctorModel.user))
        result = await db.execute(query)
        doctors = result.scalars().all()
    return doctors

@router.get("/profile/{uid}", response_model=DoctorResponse)
async def get_doctor_profile(uid: str, db: AsyncSession = Depends(get_db)):
    query = select(DoctorModel).where(DoctorModel.uid == uid).options(selectinload(DoctorModel.user))
    result = await db.execute(query)
    doctor = result.scalars().first()
    if not doctor:
        doctor = DoctorModel(
            id="doc_" + str(uuid.uuid4())[:8],
            uid=uid,
            specialization="General Medicine",
            hospital_name="City General Hospital",
            experience_years=5,
            contact="+1 (555) 999-8888"
        )
        db.add(doctor)
        await db.commit()
        # Query again to load user relationship
        query = select(DoctorModel).where(DoctorModel.uid == uid).options(selectinload(DoctorModel.user))
        res = await db.execute(query)
        doctor = res.scalars().first()
    return doctor

@router.put("/profile", response_model=DoctorResponse)
async def update_doctor_profile(payload: DoctorRegister, db: AsyncSession = Depends(get_db)):
    query = select(DoctorModel).where(DoctorModel.uid == payload.uid).options(selectinload(DoctorModel.user))
    result = await db.execute(query)
    doctor = result.scalars().first()
    if not doctor:
        doctor = DoctorModel(
            id="doc_" + str(uuid.uuid4())[:8],
            uid=payload.uid,
            specialization=payload.specialization,
            hospital_name=payload.hospital_name,
            experience_years=payload.experience_years,
            contact=payload.contact
        )
        db.add(doctor)
    else:
        doctor.specialization = payload.specialization
        doctor.hospital_name = payload.hospital_name
        doctor.experience_years = payload.experience_years
        doctor.contact = payload.contact

    doctor.mbbs_institution = payload.mbbs_institution
    doctor.registration_number = payload.registration_number
    
    await db.commit()
    
    # Query again to load user relationship
    query = select(DoctorModel).where(DoctorModel.uid == payload.uid).options(selectinload(DoctorModel.user))
    res = await db.execute(query)
    doctor = res.scalars().first()
    return doctor

@router.get("/appointments/{doctor_id}", response_model=List[AppointmentResponse])
async def get_doctor_appointments(doctor_id: str, db: AsyncSession = Depends(get_db)):
    query = select(AppointmentModel).where(AppointmentModel.doctor_id == doctor_id)
    result = await db.execute(query)
    return result.scalars().all()
