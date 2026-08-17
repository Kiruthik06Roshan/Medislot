from fastapi import APIRouter, Depends, HTTPException, status, File, UploadFile
from fastapi.responses import FileResponse
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from typing import List, Optional
import uuid
import os
import shutil

from ..database.connection import get_db
from ..database.models import (
    InventoryModel, OperationalAlertModel, DoctorApplicationModel,
    StaffMemberModel, StaffScheduleModel, LeaveRequestModel, DoctorModel,
    HospitalModel, UserModel
)
from ..database.schemas import (
    InventoryItemResponse, OperationalAlertResponse, DoctorApplicationResponse,
    StaffScheduleResponse, StaffScheduleCreate, LeaveRequestResponse, DoctorApplicationCreate,
    HospitalRegister, HospitalResponse, StaffMemberResponse
)
from ..utils.security import get_current_user

router = APIRouter(prefix="/api/hospital", tags=["Hospital Operations"])

# --- HOSPITAL REGISTRATION & SUPER ADMIN VERIFICATION ---
@router.post("/register", response_model=HospitalResponse)
async def register_hospital(
    payload: HospitalRegister,
    current_user: dict = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    if current_user.get("role") not in ["hospital", "hospital_coordinator"]:
        raise HTTPException(status_code=403, detail="Hospital coordinator role required.")
    # Check if registration number already exists (excluding current user's profile if updating)
    query = select(HospitalModel).where(HospitalModel.registration_number == payload.registration_number)
    res = await db.execute(query)
    existing = res.scalars().all()
    for e in existing:
        if e.uid != payload.uid:
            raise HTTPException(status_code=400, detail="Hospital registration number already registered.")
            
    # Check if we already have a profile for this coordinator uid
    u_query = select(HospitalModel).where(HospitalModel.uid == payload.uid)
    u_res = await db.execute(u_query)
    existing_hosp = u_res.scalars().first()
    
    if existing_hosp:
        existing_hosp.name = payload.name
        existing_hosp.license_number = payload.license_number
        existing_hosp.registration_number = payload.registration_number
        existing_hosp.address = payload.address
        existing_hosp.hospital_type = payload.hospital_type
        existing_hosp.departments = payload.departments
        existing_hosp.contact = payload.contact
        existing_hosp.admin_name = payload.admin_name
        existing_hosp.docs_attached = payload.docs_attached
        existing_hosp.status = "Pending"
        await db.commit()
        await db.refresh(existing_hosp)
        return existing_hosp
        
    hospital = HospitalModel(
        id="hosp_" + str(uuid.uuid4())[:8],
        name=payload.name,
        uid=payload.uid,
        license_number=payload.license_number,
        registration_number=payload.registration_number,
        address=payload.address,
        hospital_type=payload.hospital_type,
        departments=payload.departments,
        contact=payload.contact,
        admin_name=payload.admin_name,
        docs_attached=payload.docs_attached,
        status="Pending"
    )
    db.add(hospital)
    await db.commit()
    await db.refresh(hospital)
    return hospital

@router.get("/all", response_model=List[HospitalResponse])
async def get_all_hospitals(
    current_user: dict = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    if current_user.get("role") != "super_admin":
        raise HTTPException(status_code=403, detail="Super Admin role required.")
    query = select(HospitalModel)
    res = await db.execute(query)
    return res.scalars().all()

@router.get("/active", response_model=List[HospitalResponse])
async def get_active_hospitals(
    current_user: dict = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    # Allow any authenticated user (patient, doctor, coordinator) to see clinics
    query = select(HospitalModel)
    res = await db.execute(query)
    return res.scalars().all()

@router.post("/{hosp_id}/status", response_model=HospitalResponse)
async def update_hospital_status(
    hosp_id: str,
    status: str,
    rejection_reason: Optional[str] = None,
    current_user: dict = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    if current_user.get("role") != "super_admin":
        raise HTTPException(status_code=403, detail="Super Admin role required.")
    query = select(HospitalModel).where(HospitalModel.id == hosp_id)
    res = await db.execute(query)
    hospital = res.scalars().first()
    if not hospital:
        raise HTTPException(status_code=404, detail="Hospital not found")
    hospital.status = status
    if rejection_reason:
        hospital.rejection_reason = rejection_reason
    await db.commit()
    await db.refresh(hospital)
    return hospital

@router.get("/profile/{uid}", response_model=HospitalResponse)
async def get_hospital_profile(uid: str, db: AsyncSession = Depends(get_db)):
    query = select(HospitalModel).where(HospitalModel.uid == uid)
    res = await db.execute(query)
    hospital = res.scalars().first()
    if not hospital:
        u_query = select(UserModel).where(UserModel.uid == uid)
        u_res = await db.execute(u_query)
        user = u_res.scalars().first()
        if not user:
            raise HTTPException(status_code=404, detail="Hospital account not found")
        hospital = HospitalModel(
            id="hosp_" + str(uuid.uuid4())[:8],
            name=user.full_name,
            uid=uid,
            license_number="LIC-PENDING",
            registration_number="REG-" + str(uuid.uuid4())[:6],
            address="Address pending update",
            hospital_type="General",
            departments="General, Emergency",
            contact="+1 (555) 000-0000",
            admin_name=user.full_name,
            status="Pending"
        )
        db.add(hospital)
        await db.commit()
        await db.refresh(hospital)
    return hospital

@router.put("/profile", response_model=HospitalResponse)
async def update_hospital_profile(payload: HospitalRegister, db: AsyncSession = Depends(get_db)):
    query = select(HospitalModel).where(HospitalModel.uid == payload.uid)
    res = await db.execute(query)
    hospital = res.scalars().first()
    if not hospital:
        hospital = HospitalModel(
            id="hosp_" + str(uuid.uuid4())[:8],
            uid=payload.uid,
            name=payload.name,
            license_number=payload.license_number,
            registration_number=payload.registration_number,
            address=payload.address,
            hospital_type=payload.hospital_type,
            departments=payload.departments,
            contact=payload.contact,
            admin_name=payload.admin_name,
            docs_attached=payload.docs_attached,
            status="Pending"
        )
        db.add(hospital)
    else:
        hospital.name = payload.name
        hospital.license_number = payload.license_number
        hospital.registration_number = payload.registration_number
        hospital.address = payload.address
        hospital.hospital_type = payload.hospital_type
        hospital.departments = payload.departments
        hospital.contact = payload.contact
        hospital.admin_name = payload.admin_name
        if payload.docs_attached:
            hospital.docs_attached = payload.docs_attached

    await db.commit()
    await db.refresh(hospital)
    return hospital



# --- RESOURCES & INVENTORY ---
@router.get("/inventory", response_model=List[InventoryItemResponse])
async def get_inventory(
    current_user: dict = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    role = current_user.get("role")
    uid = current_user.get("sub")
    if role in ["hospital", "hospital_coordinator"]:
        h_query = select(HospitalModel).where(HospitalModel.uid == uid)
        h_res = await db.execute(h_query)
        hospital = h_res.scalars().first()
        if not hospital:
            return []
        query = select(InventoryModel).where(InventoryModel.hospital_name == hospital.name)
    else:
        query = select(InventoryModel)
    result = await db.execute(query)
    items = result.scalars().all()
    if not items and (role not in ["hospital", "hospital_coordinator"] or hospital.name == "City General Hospital"):
        # Seed initial resource metrics (only for City General Hospital by default if empty)
        seed = [
            InventoryModel(id="inv_1", hospital_name="City General Hospital", name="ICU Beds Available", total=20, available=8, unit="Beds", category="ICU"),
            InventoryModel(id="inv_2", hospital_name="City General Hospital", name="Ward Beds Available", total=150, available=45, unit="Beds", category="Beds"),
            InventoryModel(id="inv_3", hospital_name="City General Hospital", name="Oxygen Reserves", total=1000, available=720, unit="Liters", category="Gas"),
            InventoryModel(id="inv_4", hospital_name="City General Hospital", name="Emergency Ambulances", total=12, available=4, unit="Ambulances", category="Ambulances"),
            InventoryModel(id="inv_5", hospital_name="City General Hospital", name="O-Negative Blood Units", total=50, available=15, unit="Bags", category="Blood"),
            InventoryModel(id="inv_6", hospital_name="City General Hospital", name="Paracetamol 500mg", total=5000, available=1200, unit="Tablets", category="Medicine"),
            InventoryModel(id="inv_7", hospital_name="City General Hospital", name="Defibrillators Active", total=15, available=12, unit="Units", category="Equipment")
        ]
        for item in seed:
            db.add(item)
        await db.commit()
        if role in ["hospital", "hospital_coordinator"]:
            query = select(InventoryModel).where(InventoryModel.hospital_name == hospital.name)
        else:
            query = select(InventoryModel)
        result = await db.execute(query)
        items = result.scalars().all()
    return items

@router.put("/inventory/{item_id}", response_model=InventoryItemResponse)
async def update_inventory_levels(
    item_id: str,
    available: int,
    current_user: dict = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    query = select(InventoryModel).where(InventoryModel.id == item_id)
    result = await db.execute(query)
    item = result.scalars().first()
    if not item:
        raise HTTPException(status_code=404, detail="Item not found")
    item.available = available
    await db.commit()
    await db.refresh(item)
    return item


# --- OPERATIONAL ALERTS ---
@router.get("/alerts", response_model=List[OperationalAlertResponse])
async def get_alerts(
    current_user: dict = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    role = current_user.get("role")
    uid = current_user.get("sub")
    if role in ["hospital", "hospital_coordinator"]:
        h_query = select(HospitalModel).where(HospitalModel.uid == uid)
        h_res = await db.execute(h_query)
        hospital = h_res.scalars().first()
        if not hospital:
            return []
        query = select(OperationalAlertModel).where(
            (OperationalAlertModel.is_resolved == False) &
            (OperationalAlertModel.hospital_name == hospital.name)
        )
    else:
        query = select(OperationalAlertModel).where(OperationalAlertModel.is_resolved == False)
    result = await db.execute(query)
    alerts = result.scalars().all()
    if not alerts and (role not in ["hospital", "hospital_coordinator"] or hospital.name == "City General Hospital"):
        # Seed initial active operational alerts (only for City General Hospital by default)
        seed = [
            OperationalAlertModel(id="alt_1", hospital_name="City General Hospital", title="Low Oxygen Level Warning", message="Oxygen reserves dropped below 30% safety threshold.", severity="Critical", timestamp="10 mins ago", department="ICU"),
            OperationalAlertModel(id="alt_2", hospital_name="City General Hospital", title="OPD Patient Overload", message="Waiting time in General Medicine exceeds 60 minutes.", severity="Medium", timestamp="25 mins ago", department="Outpatient"),
            OperationalAlertModel(id="alt_3", hospital_name="City General Hospital", title="Defibrillator Maintenance", message="Defibrillator in ER Room 3 needs hardware diagnostics.", severity="High", timestamp="1 hour ago", department="ER")
        ]
        for a in seed:
            db.add(a)
        await db.commit()
        if role in ["hospital", "hospital_coordinator"]:
            query = select(OperationalAlertModel).where(
                (OperationalAlertModel.is_resolved == False) &
                (OperationalAlertModel.hospital_name == hospital.name)
            )
        else:
            query = select(OperationalAlertModel).where(OperationalAlertModel.is_resolved == False)
        result = await db.execute(query)
        alerts = result.scalars().all()
    return alerts

@router.post("/alerts/{alert_id}/resolve", response_model=OperationalAlertResponse)
async def resolve_alert(alert_id: str, db: AsyncSession = Depends(get_db)):
    query = select(OperationalAlertModel).where(OperationalAlertModel.id == alert_id)
    result = await db.execute(query)
    alert = result.scalars().first()
    if not alert:
        raise HTTPException(status_code=404, detail="Alert not found")
    alert.is_resolved = True
    await db.commit()
    await db.refresh(alert)
    return alert


# --- DOCTOR RECRUITMENT & VERIFICATION ---
@router.get("/recruitment", response_model=List[DoctorApplicationResponse])
async def get_recruitment_applications(
    current_user: dict = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    role = current_user.get("role")
    uid = current_user.get("sub")
    if role not in ["hospital", "hospital_coordinator"]:
        raise HTTPException(status_code=403, detail="Unauthorized role.")
        
    h_query = select(HospitalModel).where(HospitalModel.uid == uid)
    h_res = await db.execute(h_query)
    hospital = h_res.scalars().first()
    if not hospital:
        return []
    query = select(DoctorApplicationModel).where(DoctorApplicationModel.selected_hospital == hospital.name)
    result = await db.execute(query)
    apps = result.scalars().all()
    return apps

@router.post("/recruitment", response_model=DoctorApplicationResponse)
async def create_doctor_application(
    payload: DoctorApplicationCreate,
    current_user: dict = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    if current_user.get("role") != "doctor" or current_user.get("sub") != payload.uid:
         raise HTTPException(status_code=403, detail="Unauthorized to create application.")
         
    app = DoctorApplicationModel(
        id="app_" + str(uuid.uuid4())[:8],
        uid=payload.uid,
        name=payload.name,
        specialization=payload.specialization,
        experience_years=payload.experience_years,
        medical_registration_number=payload.medical_registration_number,
        mbbs_institution=payload.mbbs_institution,
        docs_attached=payload.docs_attached,
        resume_file=payload.resume_file,
        selected_hospital=payload.selected_hospital,
        status="Pending"
    )
    db.add(app)
    await db.commit()
    await db.refresh(app)
    return app

@router.post("/recruitment/{app_id}/status", response_model=DoctorApplicationResponse)
async def update_application_status(
    app_id: str,
    status: str,
    rejection_reason: str = None,
    current_user: dict = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    role = current_user.get("role")
    uid = current_user.get("sub")
    if role not in ["hospital", "hospital_coordinator"]:
        raise HTTPException(status_code=403, detail="Hospital coordinator role required.")
        
    query = select(DoctorApplicationModel).where(DoctorApplicationModel.id == app_id)
    result = await db.execute(query)
    app = result.scalars().first()
    if not app:
        raise HTTPException(status_code=404, detail="Application not found")
        
    # Verify that this coordinator actually manages the hospital the doctor applied to
    h_query = select(HospitalModel).where(HospitalModel.uid == uid)
    h_res = await db.execute(h_query)
    hospital = h_res.scalars().first()
    if not hospital or hospital.name != app.selected_hospital:
        raise HTTPException(status_code=403, detail="Unauthorized to manage applications for this hospital.")
        
    app.status = status
    if rejection_reason:
        app.rejection_reason = rejection_reason

    # If approved, automatically register doctor profile in DB
    if status == "Approved":
        # Find doctor's registered user account by matching name or email
        user_query = select(UserModel).where(UserModel.full_name == app.name)
        user_res = await db.execute(user_query)
        user = user_res.scalars().first()
        
        doc_uid = user.uid if user else app.uid if app.uid else ("uid_" + app.id)
        doc_query = select(DoctorModel).where(DoctorModel.uid == doc_uid)
        doc_res = await db.execute(doc_query)
        existing_doc = doc_res.scalars().first()
        if not existing_doc:
            doctor = DoctorModel(
                id="doc_" + app.id,
                uid=doc_uid,
                specialization=app.specialization,
                hospital_name=app.selected_hospital,
                rating=4.8,
                experience_years=int(app.experience_years) if app.experience_years.isdigit() else 5,
                contact="+1 (555) 000-0000",
                room="Room 3C",
                shift="Morning Shift",
                mbbs_institution=app.mbbs_institution,
                registration_number=app.medical_registration_number,
                bio=f"MBBS Graduate from {app.mbbs_institution} with {app.experience_years} years experience."
            )
            db.add(doctor)
            
            # Register in staff scheduling as a member
            staff = StaffMemberModel(
                id="stf_" + app.id,
                hospital_name=app.selected_hospital,
                name=app.name,
                role="Doctor",
                department=app.specialization,
                room="Room 3C",
                status="On Duty"
            )
            db.add(staff)

    await db.commit()
    await db.refresh(app)
    return app


@router.get("/staff", response_model=List[StaffMemberResponse])
async def get_staff_members(
    current_user: dict = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    role = current_user.get("role")
    uid = current_user.get("sub")
    if role in ["hospital", "hospital_coordinator"]:
        h_query = select(HospitalModel).where(HospitalModel.uid == uid)
        h_res = await db.execute(h_query)
        hospital = h_res.scalars().first()
        if not hospital:
            return []
        query = select(StaffMemberModel).where(StaffMemberModel.hospital_name == hospital.name)
    else:
        query = select(StaffMemberModel)
        
    result = await db.execute(query)
    staff = result.scalars().all()
    
    if not staff and (role not in ["hospital", "hospital_coordinator"] or (hospital and hospital.name == "City General Hospital")):
        # Seed initial staff members (only for City General Hospital by default)
        seed = [
            StaffMemberModel(id="stf_1", hospital_name="City General Hospital", name="Nurse Clara Barton", role="Nurse", department="Pediatrics", room="Room 1A", status="On Duty"),
            StaffMemberModel(id="stf_2", hospital_name="City General Hospital", name="Technician Marie Curie", role="Lab Technician", department="Cardiology", room="Room 2B", status="On Duty")
        ]
        for s in seed:
            db.add(s)
        await db.commit()
        if role in ["hospital", "hospital_coordinator"]:
            query = select(StaffMemberModel).where(StaffMemberModel.hospital_name == hospital.name)
        else:
            query = select(StaffMemberModel)
        result = await db.execute(query)
        staff = result.scalars().all()
    return staff


# --- STAFF SCHEDULING ---
@router.get("/scheduling", response_model=List[StaffScheduleResponse])
async def get_staff_scheduling(
    current_user: dict = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    role = current_user.get("role")
    uid = current_user.get("sub")
    if role in ["hospital", "hospital_coordinator"]:
        h_query = select(HospitalModel).where(HospitalModel.uid == uid)
        h_res = await db.execute(h_query)
        hospital = h_res.scalars().first()
        if not hospital:
            return []
        query = select(StaffScheduleModel).where(StaffScheduleModel.hospital_name == hospital.name)
    else:
        query = select(StaffScheduleModel)
    result = await db.execute(query)
    schedules = result.scalars().all()
    if not schedules and (role not in ["hospital", "hospital_coordinator"] or hospital.name == "City General Hospital"):
        # Seed initial weekly staff schedule (only for City General Hospital by default)
        seed = [
            StaffScheduleModel(id="sch_1", hospital_name="City General Hospital", name="Dr. John Doe", role="Doctor", department="Cardiology", date="Monday", shift_type="Morning", shift_time="07:00 AM - 01:00 PM", room="Room 4B"),
            StaffScheduleModel(id="sch_2", hospital_name="City General Hospital", name="Dr. Helen Cho", role="Doctor", department="Neurology", date="Monday", shift_type="Afternoon", shift_time="01:00 PM - 07:00 PM", room="Room 2A"),
            StaffScheduleModel(id="sch_3", hospital_name="City General Hospital", name="Nurse Chloe Bennett", role="Nurse", department="Emergency", date="Monday", shift_type="Morning", shift_time="07:00 AM - 01:00 PM", room="ER Wing A"),
            StaffScheduleModel(id="sch_4", hospital_name="City General Hospital", name="Nurse Sarah Connor", role="Nurse", department="ICU", date="Monday", shift_type="Night", shift_time="07:00 PM - 07:00 AM", room="ICU Desk")
        ]
        for s in seed:
            db.add(s)
        await db.commit()
        if role in ["hospital", "hospital_coordinator"]:
            query = select(StaffScheduleModel).where(StaffScheduleModel.hospital_name == hospital.name)
        else:
            query = select(StaffScheduleModel)
        result = await db.execute(query)
        schedules = result.scalars().all()
    return schedules

@router.post("/scheduling", response_model=StaffScheduleResponse)
async def assign_staff_shift(
    payload: StaffScheduleCreate,
    current_user: dict = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    role = current_user.get("role")
    uid = current_user.get("sub")
    hosp_name = None
    if role in ["hospital", "hospital_coordinator"]:
        h_query = select(HospitalModel).where(HospitalModel.uid == uid)
        h_res = await db.execute(h_query)
        hospital = h_res.scalars().first()
        if hospital:
            hosp_name = hospital.name

    schedule = StaffScheduleModel(
        id="sch_" + str(uuid.uuid4())[:8],
        hospital_name=hosp_name,
        name=payload.name,
        role=payload.role,
        department=payload.department,
        date=payload.date,
        shift_type=payload.shift_type,
        shift_time=payload.shift_time,
        room=payload.room,
        status=payload.status
    )
    db.add(schedule)
    await db.commit()
    await db.refresh(schedule)
    return schedule

@router.put("/scheduling/{sch_id}", response_model=StaffScheduleResponse)
async def edit_staff_shift(
    sch_id: str,
    payload: StaffScheduleCreate,
    current_user: dict = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    role = current_user.get("role")
    uid = current_user.get("sub")
    if role not in ["hospital", "hospital_coordinator"]:
        raise HTTPException(status_code=403, detail="Unauthorized role.")
        
    h_query = select(HospitalModel).where(HospitalModel.uid == uid)
    h_res = await db.execute(h_query)
    hospital = h_res.scalars().first()
    if not hospital:
        raise HTTPException(status_code=404, detail="Hospital not found")
        
    query = select(StaffScheduleModel).where(
        (StaffScheduleModel.id == sch_id) &
        (StaffScheduleModel.hospital_name == hospital.name)
    )
    result = await db.execute(query)
    schedule = result.scalars().first()
    if not schedule:
        raise HTTPException(status_code=404, detail="Shift not found")
        
    schedule.name = payload.name
    schedule.role = payload.role
    schedule.department = payload.department
    schedule.date = payload.date
    schedule.shift_type = payload.shift_type
    schedule.shift_time = payload.shift_time
    schedule.room = payload.room
    schedule.status = payload.status
    
    await db.commit()
    await db.refresh(schedule)
    return schedule

@router.delete("/scheduling/{sch_id}")
async def delete_staff_shift(
    sch_id: str,
    current_user: dict = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    role = current_user.get("role")
    uid = current_user.get("sub")
    if role not in ["hospital", "hospital_coordinator"]:
        raise HTTPException(status_code=403, detail="Unauthorized role.")
        
    h_query = select(HospitalModel).where(HospitalModel.uid == uid)
    h_res = await db.execute(h_query)
    hospital = h_res.scalars().first()
    if not hospital:
        raise HTTPException(status_code=404, detail="Hospital not found")
        
    query = select(StaffScheduleModel).where(
        (StaffScheduleModel.id == sch_id) &
        (StaffScheduleModel.hospital_name == hospital.name)
    )
    result = await db.execute(query)
    schedule = result.scalars().first()
    if not schedule:
        raise HTTPException(status_code=404, detail="Shift not found")
    await db.delete(schedule)
    await db.commit()
    return {"status": "success", "message": "Shift deleted successfully"}

@router.post("/scheduling/duplicate")
async def duplicate_scheduling(
    current_user: dict = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    role = current_user.get("role")
    uid = current_user.get("sub")
    if role in ["hospital", "hospital_coordinator"]:
        h_query = select(HospitalModel).where(HospitalModel.uid == uid)
        h_res = await db.execute(h_query)
        hospital = h_res.scalars().first()
        if not hospital:
            raise HTTPException(status_code=404, detail="Hospital not found")
        query = select(StaffScheduleModel).where(StaffScheduleModel.hospital_name == hospital.name)
        hosp_name = hospital.name
    else:
        query = select(StaffScheduleModel)
        hosp_name = None

    result = await db.execute(query)
    schedules = result.scalars().all()
    
    if not schedules:
        return {"status": "warning", "message": "No schedules found to duplicate."}
        
    duplicated_count = 0
    for sch in schedules:
        new_sch = StaffScheduleModel(
            id="sch_" + str(uuid.uuid4())[:8],
            hospital_name=hosp_name,
            name=sch.name,
            role=sch.role,
            department=sch.department,
            date=sch.date,
            shift_type=sch.shift_type,
            shift_time=sch.shift_time,
            room=sch.room,
            status=sch.status
        )
        db.add(new_sch)
        duplicated_count += 1
        
    await db.commit()
    return {"status": "success", "message": f"Successfully duplicated {duplicated_count} schedules for the next rotation!"}


# --- LEAVE REQUESTS ---
@router.get("/leaves", response_model=List[LeaveRequestResponse])
async def get_leave_requests(
    current_user: dict = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    role = current_user.get("role")
    uid = current_user.get("sub")
    if role in ["hospital", "hospital_coordinator"]:
        h_query = select(HospitalModel).where(HospitalModel.uid == uid)
        h_res = await db.execute(h_query)
        hospital = h_res.scalars().first()
        if not hospital:
            return []
        query = select(LeaveRequestModel).where(LeaveRequestModel.hospital_name == hospital.name)
    else:
        query = select(LeaveRequestModel)
    result = await db.execute(query)
    leaves = result.scalars().all()
    if not leaves and (role not in ["hospital", "hospital_coordinator"] or hospital.name == "City General Hospital"):
        # Seed initial leave requests (only for City General Hospital by default)
        seed = [
            LeaveRequestModel(id="lv_1", hospital_name="City General Hospital", staff_id="stf_9", staff_name="Nurse Clara Barton", role="Nurse", department="Pediatrics", start_date="Aug 10", end_date="Aug 14", reason="Family emergency and personal travel.", status="Pending"),
            LeaveRequestModel(id="lv_2", hospital_name="City General Hospital", staff_id="stf_2", staff_name="Dr. Helen Cho", role="Doctor", department="Neurology", start_date="Aug 12", end_date="Aug 13", reason="Medical checkup appointment.", status="Pending")
        ]
        for l in seed:
            db.add(l)
        await db.commit()
        if role in ["hospital", "hospital_coordinator"]:
            query = select(LeaveRequestModel).where(LeaveRequestModel.hospital_name == hospital.name)
        else:
            query = select(LeaveRequestModel)
        result = await db.execute(query)
        leaves = result.scalars().all()
    return leaves

@router.post("/leaves/{lv_id}/status", response_model=LeaveRequestResponse)
async def update_leave_status(lv_id: str, status: str, db: AsyncSession = Depends(get_db)):
    query = select(LeaveRequestModel).where(LeaveRequestModel.id == lv_id)
    result = await db.execute(query)
    leave = result.scalars().first()
    if not leave:
        raise HTTPException(status_code=404, detail="Leave request not found")
    leave.status = status
    await db.commit()
    await db.refresh(leave)
    return leave

UPLOAD_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "..", "uploaded_documents"))
os.makedirs(UPLOAD_DIR, exist_ok=True)

@router.post("/upload-document")
async def upload_document(
    file: UploadFile = File(...),
    current_user: dict = Depends(get_current_user)
):
    filename = file.filename
    ext = os.path.splitext(filename)[1].lower()
    if ext != ".pdf":
        raise HTTPException(status_code=400, detail="Only PDF files are allowed.")
    
    unique_filename = f"{uuid.uuid4().hex}_{filename}"
    file_path = os.path.join(UPLOAD_DIR, unique_filename)
    
    with open(file_path, "wb") as buffer:
        shutil.copyfileobj(file.file, buffer)
        
    return {"filename": unique_filename}

@router.get("/documents/{filename}")
async def get_uploaded_document(
    filename: str,
    current_user: dict = Depends(get_current_user)
):
    if current_user.get("role") != "super_admin":
        raise HTTPException(status_code=403, detail="Unauthorized access. Super Admin only.")
    
    safe_filename = os.path.basename(filename)
    file_path = os.path.join(UPLOAD_DIR, safe_filename)
    
    if not os.path.exists(file_path):
        raise HTTPException(status_code=404, detail="Document not found.")
        
    return FileResponse(file_path, media_type="application/pdf", filename=safe_filename.split("_", 1)[-1])
