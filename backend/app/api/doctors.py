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

from typing import Optional

@router.get("/all", response_model=List[DoctorResponse])
async def get_all_doctors(
    specialization: Optional[str] = None,
    name: Optional[str] = None,
    hospital: Optional[str] = None,
    db: AsyncSession = Depends(get_db)
):
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

    filtered_doctors = []
    for doc in doctors:
        if specialization and specialization.strip() and specialization.lower() != "all":
            if specialization.lower() not in doc.specialization.lower():
                continue
        if hospital and hospital.strip() and hospital.lower() != "all":
            if hospital.lower() not in doc.hospital_name.lower():
                continue
        if name and name.strip():
            n_lower = name.strip().lower()
            if (n_lower not in doc.name.lower() and 
                n_lower not in doc.specialization.lower() and 
                n_lower not in doc.hospital_name.lower()):
                continue
        filtered_doctors.append(doc)

    return filtered_doctors

@router.get("/profile/{uid}", response_model=DoctorResponse)
async def get_doctor_profile(uid: str, db: AsyncSession = Depends(get_db)):
    query = select(DoctorModel).where(DoctorModel.uid == uid).options(selectinload(DoctorModel.user))
    result = await db.execute(query)
    doctor = result.scalars().first()
    if not doctor:
        u_query = select(UserModel).where(UserModel.uid == uid)
        u_res = await db.execute(u_query)
        user = u_res.scalars().first()
        if not user:
            raise HTTPException(status_code=404, detail="Doctor profile not found")
        doctor = DoctorModel(
            id="doc_" + str(uuid.uuid4())[:8],
            uid=uid,
            specialization="General Medicine",
            hospital_name="City General Hospital",
            experience_years=5,
            contact="+1 (555) 999-8888",
            mbbs_institution="Medical College",
            registration_number="MC-" + str(uuid.uuid4())[:6]
        )
        db.add(doctor)
        await db.commit()
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
            contact=payload.contact,
            slot_times=payload.slot_times
        )
        db.add(doctor)
    else:
        doctor.specialization = payload.specialization
        doctor.hospital_name = payload.hospital_name
        doctor.experience_years = payload.experience_years
        doctor.contact = payload.contact

    doctor.mbbs_institution = payload.mbbs_institution
    doctor.registration_number = payload.registration_number
    if payload.slot_times is not None:
        doctor.slot_times = payload.slot_times
    
    await db.commit()
    
    # Query again to load user relationship
    query = select(DoctorModel).where(DoctorModel.uid == payload.uid).options(selectinload(DoctorModel.user))
    res = await db.execute(query)
    doctor = res.scalars().first()
    return doctor

@router.get("/appointments/{doctor_id}", response_model=List[AppointmentResponse])
async def get_doctor_appointments(doctor_id: str, db: AsyncSession = Depends(get_db)):
    doc_query = select(DoctorModel).where((DoctorModel.uid == doctor_id) | (DoctorModel.id == doctor_id))
    doc_res = await db.execute(doc_query)
    doctor = doc_res.scalars().first()

    if doctor:
        target_ids = list({doctor.uid, doctor.id, doctor_id})
    else:
        target_ids = [doctor_id]

    apt_query = select(AppointmentModel).where(AppointmentModel.doctor_id.in_(target_ids))
    apt_res = await db.execute(apt_query)
    appointments = apt_res.scalars().all()

    # Sort appointments: Upcoming first, then date, time, queue_number
    status_order = {"Upcoming": 0, "Scheduled": 0, "Confirmed": 0, "Completed": 1, "Cancelled": 2}
    sorted_apts = sorted(appointments, key=lambda a: (status_order.get(a.status, 1), a.date, a.time, a.queue_number))

    response_list = []
    for apt in sorted_apts:
        u_query = select(UserModel).where(UserModel.uid == apt.patient_id)
        u_res = await db.execute(u_query)
        user = u_res.scalars().first()
        p_name = user.full_name if user else f"Patient ({apt.patient_id[:8]})"
        
        resp = AppointmentResponse(
            id=apt.id,
            patient_id=apt.patient_id,
            doctor_id=apt.doctor_id,
            doctor_name=apt.doctor_name,
            department=apt.department,
            hospital=apt.hospital,
            date=apt.date,
            time=apt.time,
            status=apt.status,
            queue_number=apt.queue_number,
            patient_name=p_name
        )
        response_list.append(resp)

    return response_list
