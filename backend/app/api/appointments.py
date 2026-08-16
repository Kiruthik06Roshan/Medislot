from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from typing import List
import uuid

from ..database.connection import get_db
from ..database.models import AppointmentModel
from ..database.schemas import AppointmentCreate, AppointmentResponse

router = APIRouter(prefix="/api/appointments", tags=["Appointments"])

@router.post("", response_model=AppointmentResponse)
async def create_appointment(payload: AppointmentCreate, db: AsyncSession = Depends(get_db)):
    # Conflict check: same doctor, date, and time
    query = select(AppointmentModel).where(
        AppointmentModel.doctor_id == payload.doctor_id,
        AppointmentModel.date == payload.date,
        AppointmentModel.time == payload.time,
        AppointmentModel.status == "Upcoming"
    )
    result = await db.execute(query)
    conflict = result.scalars().first()
    if conflict:
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="The selected time slot is already booked for this doctor."
        )

    # Compute queue number for the day
    queue_query = select(AppointmentModel).where(
        AppointmentModel.doctor_id == payload.doctor_id,
        AppointmentModel.date == payload.date
    )
    queue_result = await db.execute(queue_query)
    day_bookings = queue_result.scalars().all()
    queue_num = len(day_bookings) + 1

    appointment = AppointmentModel(
        id="apt_" + str(uuid.uuid4())[:8],
        patient_id=payload.patient_id,
        doctor_id=payload.doctor_id,
        doctor_name=payload.doctor_name,
        department=payload.department,
        hospital=payload.hospital,
        date=payload.date,
        time=payload.time,
        status="Upcoming",
        queue_number=queue_num
    )
    db.add(appointment)
    await db.commit()
    await db.refresh(appointment)
    return appointment

@router.put("/{apt_id}/status", response_model=AppointmentResponse)
async def update_appointment_status(apt_id: str, status: str, db: AsyncSession = Depends(get_db)):
    query = select(AppointmentModel).where(AppointmentModel.id == apt_id)
    result = await db.execute(query)
    appointment = result.scalars().first()
    if not appointment:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Appointment not found"
        )
    appointment.status = status
    await db.commit()
    await db.refresh(appointment)
    return appointment

@router.put("/{apt_id}/reschedule", response_model=AppointmentResponse)
async def reschedule_appointment(apt_id: str, date: str, time: str, db: AsyncSession = Depends(get_db)):
    query = select(AppointmentModel).where(AppointmentModel.id == apt_id)
    result = await db.execute(query)
    appointment = result.scalars().first()
    if not appointment:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Appointment not found"
        )
    
    # Conflict check: same doctor, date, and time (except itself)
    conflict_query = select(AppointmentModel).where(
        AppointmentModel.doctor_id == appointment.doctor_id,
        AppointmentModel.date == date,
        AppointmentModel.time == time,
        AppointmentModel.status == "Upcoming",
        AppointmentModel.id != apt_id
    )
    conflict_result = await db.execute(conflict_query)
    conflict = conflict_result.scalars().first()
    if conflict:
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="The selected time slot is already booked for this doctor."
        )

    # Compute new queue number for the day
    queue_query = select(AppointmentModel).where(
        AppointmentModel.doctor_id == appointment.doctor_id,
        AppointmentModel.date == date
    )
    queue_result = await db.execute(queue_query)
    day_bookings = queue_result.scalars().all()
    queue_num = len(day_bookings) + 1

    appointment.date = date
    appointment.time = time
    appointment.queue_number = queue_num
    
    await db.commit()
    await db.refresh(appointment)
    return appointment

@router.put("/{apt_id}/cancel", response_model=AppointmentResponse)
async def cancel_appointment(apt_id: str, db: AsyncSession = Depends(get_db)):
    query = select(AppointmentModel).where(AppointmentModel.id == apt_id)
    result = await db.execute(query)
    appointment = result.scalars().first()
    if not appointment:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Appointment not found"
        )
    appointment.status = "Cancelled"
    await db.commit()
    await db.refresh(appointment)
    return appointment

