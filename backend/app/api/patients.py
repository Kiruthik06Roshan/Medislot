from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from typing import List, Optional
import uuid
import datetime

from ..database.connection import get_db
from ..database.models import UserModel, PatientModel, AppointmentModel, MedicalRecordModel, QueueModel, HospitalModel
from ..database.schemas import (
    PatientRegister, PatientResponse, AppointmentResponse, MedicalRecordResponse, MedicalRecordCreate,
    QueueJoinRequest, QueueResponse, PatientQueueInfo, QueueUpdateList
)
from ..utils.security import get_current_user

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

@router.post("/queue/join", response_model=QueueResponse)
async def join_queue(
    payload: QueueJoinRequest,
    db: AsyncSession = Depends(get_db),
    current_user: dict = Depends(get_current_user)
):
    if current_user.get("role") != "patient":
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Only patients can join the queue."
        )
    
    # Resolve Patient
    pat_query = select(PatientModel).where(PatientModel.uid == current_user["sub"])
    pat_res = await db.execute(pat_query)
    patient = pat_res.scalars().first()
    if not patient:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Patient profile not found."
        )

    # Validate that payload.patient_id matches this authenticated patient
    if payload.patient_id != current_user["sub"] and payload.patient_id != patient.id:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Access denied. Cannot join queue on behalf of another patient."
        )

    # Validate Hospital
    hosp_query = select(HospitalModel).where(
        (HospitalModel.id == payload.hospital_id) | (HospitalModel.name == payload.hospital_id)
    )
    hosp_res = await db.execute(hosp_query)
    hospital = hosp_res.scalars().first()
    if not hospital:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Hospital '{payload.hospital_id}' not found."
        )

    # Validate Department
    hosp_depts = [d.strip().lower() for d in hospital.departments.split(",") if d.strip()]
    if payload.department_id.strip().lower() not in hosp_depts:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Department '{payload.department_id}' is not registered under hospital '{hospital.name}'."
        )

    # Prevent duplicate active queue entries
    patient_ids = [current_user["sub"], patient.id]
    dup_query = select(QueueModel).where(
        QueueModel.patient_id.in_(patient_ids),
        QueueModel.queue_status == "Active"
    )
    dup_res = await db.execute(dup_query)
    existing_active = dup_res.scalars().first()
    if existing_active:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Patient is already in an active queue."
        )

    # Calculate position
    pos_query = select(QueueModel).where(
        QueueModel.hospital_id == hospital.id,
        QueueModel.department_id == payload.department_id,
        QueueModel.queue_status == "Active"
    )
    if payload.doctor_id:
        pos_query = pos_query.where(QueueModel.doctor_id == payload.doctor_id)
    else:
        pos_query = pos_query.where(QueueModel.doctor_id.is_(None))
        
    pos_res = await db.execute(pos_query)
    active_in_dept = pos_res.scalars().all()
    next_position = len(active_in_dept) + 1

    queue_entry = QueueModel(
        id="q_" + str(uuid.uuid4())[:8],
        patient_id=patient.id,
        hospital_id=hospital.id,
        department_id=payload.department_id,
        doctor_id=payload.doctor_id,
        queue_status="Active",
        status="Active",
        queue_position=next_position,
        estimated_wait_time=next_position * 10,
        symptoms=payload.symptoms,
        created_at=datetime.datetime.utcnow(),
        joined_at=datetime.datetime.utcnow()
    )
    db.add(queue_entry)
    await db.commit()
    await db.refresh(queue_entry)
    return queue_entry

@router.get("/queue/active/{patient_id}", response_model=QueueResponse)
async def get_active_queue(
    patient_id: str,
    db: AsyncSession = Depends(get_db),
    current_user: dict = Depends(get_current_user)
):
    pat_query = select(PatientModel).where(PatientModel.uid == current_user["sub"])
    pat_res = await db.execute(pat_query)
    patient = pat_res.scalars().first()
    if not patient:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Patient profile not found."
        )

    if patient_id != current_user["sub"] and patient_id != patient.id:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Access denied. You can only view your own active queue status."
        )

    query = select(QueueModel).where(
        QueueModel.patient_id.in_([current_user["sub"], patient.id]),
        QueueModel.queue_status == "Active"
    )
    result = await db.execute(query)
    entry = result.scalars().first()
    if not entry:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="No active queue entry found for this patient."
        )
    return entry

@router.get("/queue/list/{hospital_id}/{department_id}", response_model=List[PatientQueueInfo])
async def get_department_queue(
    hospital_id: str,
    department_id: str,
    doctor_id: Optional[str] = None,
    db: AsyncSession = Depends(get_db),
    current_user: dict = Depends(get_current_user)
):
    if current_user.get("role") == "patient":
        pat_query = select(PatientModel).where(PatientModel.uid == current_user["sub"])
        pat_res = await db.execute(pat_query)
        patient = pat_res.scalars().first()
        patient_ids = [current_user["sub"]]
        if patient:
            patient_ids.append(patient.id)
            
        hosp_query = select(HospitalModel).where(
            (HospitalModel.id == hospital_id) | (HospitalModel.name == hospital_id)
        )
        hosp_res = await db.execute(hosp_query)
        hospital = hosp_res.scalars().first()
        target_hosp_ids = [hospital_id]
        if hospital:
            target_hosp_ids.append(hospital.id)
            target_hosp_ids.append(hospital.name)

        q_check = select(QueueModel).where(
            QueueModel.patient_id.in_(patient_ids),
            QueueModel.hospital_id.in_(target_hosp_ids),
            QueueModel.department_id == department_id,
            QueueModel.queue_status == "Active"
        )
        q_check_res = await db.execute(q_check)
        if not q_check_res.scalars().first():
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Access denied. You can only view active queues you are currently checked into."
            )

    hosp_query = select(HospitalModel).where(
        (HospitalModel.id == hospital_id) | (HospitalModel.name == hospital_id)
    )
    hosp_res = await db.execute(hosp_query)
    hospital = hosp_res.scalars().first()
    target_hosp_ids = [hospital_id]
    if hospital:
        target_hosp_ids.append(hospital.id)
        target_hosp_ids.append(hospital.name)

    query = select(QueueModel).where(
        QueueModel.hospital_id.in_(target_hosp_ids),
        QueueModel.department_id == department_id,
        QueueModel.queue_status == "Active"
    )
    if doctor_id:
        query = query.where(QueueModel.doctor_id == doctor_id)
    else:
        query = query.where(QueueModel.doctor_id.is_(None))
    
    query = query.order_by(QueueModel.queue_position)
    result = await db.execute(query)
    entries = result.scalars().all()

    enriched = []
    for entry in entries:
        pat_query = select(PatientModel).where((PatientModel.id == entry.patient_id) | (PatientModel.uid == entry.patient_id))
        pat_res = await db.execute(pat_query)
        patient = pat_res.scalars().first()
        
        patient_name = "Patient"
        age = 0
        gender = ""
        vitals_heart_rate = None
        vitals_bp = None
        vitals_spo2 = None
        vitals_temperature = None
        
        if patient:
            age = patient.age
            gender = patient.gender
            vitals_heart_rate = patient.vitals_heart_rate
            vitals_bp = patient.vitals_bp
            vitals_spo2 = patient.vitals_spo2
            vitals_temperature = patient.vitals_temperature
            
            user_query = select(UserModel).where(UserModel.uid == patient.uid)
            user_res = await db.execute(user_query)
            user = user_res.scalars().first()
            if user:
                patient_name = user.full_name
        
        enriched.append(
            PatientQueueInfo(
                id=entry.id,
                patient_id=entry.patient_id,
                patient_name=patient_name,
                age=age,
                gender=gender,
                vitals_heart_rate=vitals_heart_rate,
                vitals_bp=vitals_bp,
                vitals_spo2=vitals_spo2,
                vitals_temperature=vitals_temperature,
                symptoms=entry.symptoms,
                queue_status=entry.queue_status,
                queue_position=entry.queue_position,
                estimated_wait_time=entry.estimated_wait_time,
                created_at=entry.created_at
            )
        )
    return enriched

@router.put("/queue/leave/{queue_id}")
async def leave_queue(
    queue_id: str,
    db: AsyncSession = Depends(get_db),
    current_user: dict = Depends(get_current_user)
):
    query = select(QueueModel).where(QueueModel.id == queue_id)
    result = await db.execute(query)
    entry = result.scalars().first()
    if not entry:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Queue entry not found."
        )
    
    pat_query = select(PatientModel).where(PatientModel.uid == current_user["sub"])
    pat_res = await db.execute(pat_query)
    patient = pat_res.scalars().first()
    if not patient:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Patient profile not found."
        )

    if entry.patient_id != current_user["sub"] and entry.patient_id != patient.id:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Access denied. You cannot cancel another patient's queue entry."
        )

    entry.queue_status = "Cancelled"
    entry.status = "Cancelled"
    entry.updated_at = datetime.datetime.utcnow()
    
    reindex_query = select(QueueModel).where(
        QueueModel.hospital_id == entry.hospital_id,
        QueueModel.department_id == entry.department_id,
        QueueModel.queue_status == "Active"
    ).order_by(QueueModel.queue_position)
    
    if entry.doctor_id:
        reindex_query = reindex_query.where(QueueModel.doctor_id == entry.doctor_id)
    else:
        reindex_query = reindex_query.where(QueueModel.doctor_id.is_(None))
    
    reindex_res = await db.execute(reindex_query)
    remaining = reindex_res.scalars().all()
    
    for idx, rem in enumerate(remaining):
        rem.queue_position = idx + 1
        rem.estimated_wait_time = (idx + 1) * 10
        rem.updated_at = datetime.datetime.utcnow()
        
    await db.commit()
    return {"status": "success", "message": "Successfully left the queue."}

@router.post("/queue/update-order")
async def update_queue_order(
    payload: QueueUpdateList,
    db: AsyncSession = Depends(get_db),
    current_user: dict = Depends(get_current_user)
):
    if not payload.items:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Update items list cannot be empty."
        )

    entries = []
    hospital_id = None
    department_id = None
    doctor_id = None
    
    for item in payload.items:
        query = select(QueueModel).where(QueueModel.id == item.queue_id)
        result = await db.execute(query)
        entry = result.scalars().first()
        if not entry:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Queue entry {item.queue_id} not found."
            )
        if entry.queue_status != "Active":
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=f"Queue entry {item.queue_id} is not active."
            )
        
        if hospital_id is None:
            hospital_id = entry.hospital_id
            department_id = entry.department_id
            doctor_id = entry.doctor_id
        else:
            if entry.hospital_id != hospital_id or entry.department_id != department_id:
                raise HTTPException(
                    status_code=status.HTTP_400_BAD_REQUEST,
                    detail="All queue entries in the update must belong to the same hospital and department."
                )
            if entry.doctor_id != doctor_id:
                raise HTTPException(
                    status_code=status.HTTP_400_BAD_REQUEST,
                    detail="All queue entries in the update must belong to the same doctor queue."
                )
        
        entries.append(entry)

    db_active_query = select(QueueModel).where(
        QueueModel.hospital_id == hospital_id,
        QueueModel.department_id == department_id,
        QueueModel.queue_status == "Active"
    )
    if doctor_id:
        db_active_query = db_active_query.where(QueueModel.doctor_id == doctor_id)
    else:
        db_active_query = db_active_query.where(QueueModel.doctor_id.is_(None))
        
    db_active_res = await db.execute(db_active_query)
    db_active_entries = db_active_res.scalars().all()
    
    db_active_ids = {q.id for q in db_active_entries}
    payload_ids = {item.queue_id for item in payload.items}
    if db_active_ids != payload_ids:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="The update list must contain exactly all active queue entries for the department queue."
        )

    positions = {item.queue_position for item in payload.items}
    expected_positions = set(range(1, len(payload.items) + 1))
    if positions != expected_positions:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Queue positions must be a valid sequence from 1 to N without duplicates or gaps."
        )

    id_to_item = {item.queue_id: item for item in payload.items}
    for entry in entries:
        update_item = id_to_item[entry.id]
        entry.queue_position = update_item.queue_position
        entry.estimated_wait_time = update_item.queue_position * 10
        entry.updated_at = datetime.datetime.utcnow()

    await db.commit()
    return {"status": "success", "message": "Queue order updated successfully."}

