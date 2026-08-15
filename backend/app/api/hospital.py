from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from typing import List
import uuid

from ..database.connection import get_db
from ..database.models import (
    InventoryModel, OperationalAlertModel, DoctorApplicationModel,
    StaffMemberModel, StaffScheduleModel, LeaveRequestModel, DoctorModel,
    HospitalModel, UserModel
)
from ..database.schemas import (
    InventoryItemResponse, OperationalAlertResponse, DoctorApplicationResponse,
    StaffScheduleResponse, StaffScheduleCreate, LeaveRequestResponse, DoctorApplicationCreate,
    HospitalRegister, HospitalResponse
)

router = APIRouter(prefix="/api/hospital", tags=["Hospital Operations"])

# --- HOSPITAL REGISTRATION & SUPER ADMIN VERIFICATION ---
@router.post("/register", response_model=HospitalResponse)
async def register_hospital(payload: HospitalRegister, db: AsyncSession = Depends(get_db)):
    # Check if registration number already exists
    query = select(HospitalModel).where(HospitalModel.registration_number == payload.registration_number)
    res = await db.execute(query)
    existing = res.scalars().first()
    if existing:
        raise HTTPException(status_code=400, detail="Hospital registration number already registered.")
        
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
async def get_all_hospitals(db: AsyncSession = Depends(get_db)):
    query = select(HospitalModel)
    res = await db.execute(query)
    return res.scalars().all()

@router.post("/{hosp_id}/status", response_model=HospitalResponse)
async def update_hospital_status(hosp_id: str, status: str, rejection_reason: str = None, db: AsyncSession = Depends(get_db)):
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


# --- RESOURCES & INVENTORY ---
@router.get("/inventory", response_model=List[InventoryItemResponse])
async def get_inventory(db: AsyncSession = Depends(get_db)):
    query = select(InventoryModel)
    result = await db.execute(query)
    items = result.scalars().all()
    if not items:
        # Seed initial resource metrics
        seed = [
            InventoryModel(id="inv_1", name="ICU Beds Available", total=20, available=8, unit="Beds", category="ICU"),
            InventoryModel(id="inv_2", name="Ward Beds Available", total=150, available=45, unit="Beds", category="Beds"),
            InventoryModel(id="inv_3", name="Oxygen Reserves", total=1000, available=720, unit="Liters", category="Gas"),
            InventoryModel(id="inv_4", name="Emergency Ambulances", total=12, available=4, unit="Ambulances", category="Ambulances"),
            InventoryModel(id="inv_5", name="O-Negative Blood Units", total=50, available=15, unit="Bags", category="Blood"),
            InventoryModel(id="inv_6", name="Paracetamol 500mg", total=5000, available=1200, unit="Tablets", category="Medicine"),
            InventoryModel(id="inv_7", name="Defibrillators Active", total=15, available=12, unit="Units", category="Equipment")
        ]
        for item in seed:
            db.add(item)
        await db.commit()
        query = select(InventoryModel)
        result = await db.execute(query)
        items = result.scalars().all()
    return items

@router.put("/inventory/{item_id}", response_model=InventoryItemResponse)
async def update_inventory_levels(item_id: str, available: int, db: AsyncSession = Depends(get_db)):
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
async def get_alerts(db: AsyncSession = Depends(get_db)):
    query = select(OperationalAlertModel).where(OperationalAlertModel.is_resolved == False)
    result = await db.execute(query)
    alerts = result.scalars().all()
    if not alerts:
        # Seed initial active operational alerts
        seed = [
            OperationalAlertModel(id="alt_1", title="Low Oxygen Level Warning", message="Oxygen reserves dropped below 30% safety threshold.", severity="Critical", timestamp="10 mins ago", department="ICU"),
            OperationalAlertModel(id="alt_2", title="OPD Patient Overload", message="Waiting time in General Medicine exceeds 60 minutes.", severity="Medium", timestamp="25 mins ago", department="Outpatient"),
            OperationalAlertModel(id="alt_3", title="Defibrillator Maintenance", message="Defibrillator in ER Room 3 needs hardware diagnostics.", severity="High", timestamp="1 hour ago", department="ER")
        ]
        for a in seed:
            db.add(a)
        await db.commit()
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
async def get_recruitment_applications(db: AsyncSession = Depends(get_db)):
    query = select(DoctorApplicationModel)
    result = await db.execute(query)
    apps = result.scalars().all()
    if not apps:
        seed = [
            DoctorApplicationModel(id="app_1", name="Dr. Jane Smith", specialization="Pediatrics", experience_years="8", medical_registration_number="MC-8872", mbbs_institution="Harvard Medical School", selected_hospital="City General Hospital", status="Pending"),
            DoctorApplicationModel(id="app_2", name="Dr. Charles Xavier", specialization="Neurology", experience_years="15", medical_registration_number="MC-1102", mbbs_institution="Oxford University", selected_hospital="Metro Health Medical Center", status="Pending")
        ]
        for a in seed:
            db.add(a)
        await db.commit()
        query = select(DoctorApplicationModel)
        result = await db.execute(query)
        apps = result.scalars().all()
    return apps

@router.post("/recruitment", response_model=DoctorApplicationResponse)
async def create_doctor_application(payload: DoctorApplicationCreate, db: AsyncSession = Depends(get_db)):
    app = DoctorApplicationModel(
        id="app_" + str(uuid.uuid4())[:8],
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
async def update_application_status(app_id: str, status: str, rejection_reason: str = None, db: AsyncSession = Depends(get_db)):
    query = select(DoctorApplicationModel).where(DoctorApplicationModel.id == app_id)
    result = await db.execute(query)
    app = result.scalars().first()
    if not app:
        raise HTTPException(status_code=404, detail="Application not found")
    
    app.status = status
    if rejection_reason:
        app.rejection_reason = rejection_reason

    # If approved, automatically register doctor profile in DB
    if status == "Approved":
        # Find doctor's registered user account by matching name or email
        user_query = select(UserModel).where(UserModel.full_name == app.name)
        user_res = await db.execute(user_query)
        user = user_res.scalars().first()
        
        doc_uid = user.uid if user else ("uid_" + app.id)
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
                name=app.name,
                role="Doctor",
                department=app.specialization,
                room="Room 3C"
            )
            db.add(staff)

    await db.commit()
    await db.refresh(app)
    return app


# --- STAFF SCHEDULING ---
@router.get("/scheduling", response_model=List[StaffScheduleResponse])
async def get_staff_scheduling(db: AsyncSession = Depends(get_db)):
    query = select(StaffScheduleModel)
    result = await db.execute(query)
    schedules = result.scalars().all()
    if not schedules:
        # Seed initial weekly staff schedule
        seed = [
            StaffScheduleModel(id="sch_1", name="Dr. John Doe", role="Doctor", department="Cardiology", date="Monday", shift_type="Morning", shift_time="07:00 AM - 01:00 PM", room="Room 4B"),
            StaffScheduleModel(id="sch_2", name="Dr. Helen Cho", role="Doctor", department="Neurology", date="Monday", shift_type="Afternoon", shift_time="01:00 PM - 07:00 PM", room="Room 2A"),
            StaffScheduleModel(id="sch_3", name="Nurse Chloe Bennett", role="Nurse", department="Emergency", date="Monday", shift_type="Morning", shift_time="07:00 AM - 01:00 PM", room="ER Wing A"),
            StaffScheduleModel(id="sch_4", name="Nurse Sarah Connor", role="Nurse", department="ICU", date="Monday", shift_type="Night", shift_time="07:00 PM - 07:00 AM", room="ICU Desk")
        ]
        for s in seed:
            db.add(s)
        await db.commit()
        query = select(StaffScheduleModel)
        result = await db.execute(query)
        schedules = result.scalars().all()
    return schedules

@router.post("/scheduling", response_model=StaffScheduleResponse)
async def assign_staff_shift(payload: StaffScheduleCreate, db: AsyncSession = Depends(get_db)):
    schedule = StaffScheduleModel(
        id="sch_" + str(uuid.uuid4())[:8],
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

@router.delete("/scheduling/{sch_id}")
async def delete_staff_shift(sch_id: str, db: AsyncSession = Depends(get_db)):
    query = select(StaffScheduleModel).where(StaffScheduleModel.id == sch_id)
    result = await db.execute(query)
    schedule = result.scalars().first()
    if not schedule:
        raise HTTPException(status_code=404, detail="Shift not found")
    await db.delete(schedule)
    await db.commit()
    return {"status": "success", "message": "Shift deleted successfully"}

@router.post("/scheduling/duplicate")
async def duplicate_scheduling(db: AsyncSession = Depends(get_db)):
    query = select(StaffScheduleModel)
    result = await db.execute(query)
    schedules = result.scalars().all()
    
    if not schedules:
        return {"status": "warning", "message": "No schedules found to duplicate."}
        
    duplicated_count = 0
    for sch in schedules:
        new_sch = StaffScheduleModel(
            id="sch_" + str(uuid.uuid4())[:8],
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
async def get_leave_requests(db: AsyncSession = Depends(get_db)):
    query = select(LeaveRequestModel)
    result = await db.execute(query)
    leaves = result.scalars().all()
    if not leaves:
        # Seed initial leave requests
        seed = [
            LeaveRequestModel(id="lv_1", staff_id="stf_9", staff_name="Nurse Clara Barton", role="Nurse", department="Pediatrics", start_date="Aug 10", end_date="Aug 14", reason="Family emergency and personal travel.", status="Pending"),
            LeaveRequestModel(id="lv_2", staff_id="stf_2", staff_name="Dr. Helen Cho", role="Doctor", department="Neurology", start_date="Aug 12", end_date="Aug 13", reason="Medical checkup appointment.", status="Pending")
        ]
        for l in seed:
            db.add(l)
        await db.commit()
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
