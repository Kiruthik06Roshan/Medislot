from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from .models import (
    UserModel, PatientModel, DoctorModel, HospitalModel,
    AppointmentModel, MedicalRecordModel, StaffMemberModel,
    StaffScheduleModel, LeaveRequestModel, InventoryModel,
    OperationalAlertModel
)
from ..utils.security import get_password_hash
import datetime

from sqlalchemy import text

async def seed_data(db: AsyncSession):
    print("Ensuring database columns exist...")
    alter_queries = [
        "ALTER TABLE patients ADD COLUMN IF NOT EXISTS insurance_provider VARCHAR;",
        "ALTER TABLE patients ADD COLUMN IF NOT EXISTS insurance_plan VARCHAR;",
        "ALTER TABLE patients ADD COLUMN IF NOT EXISTS insurance_policy_number VARCHAR;",
        "ALTER TABLE patients ADD COLUMN IF NOT EXISTS insurance_expiry VARCHAR;",
        "ALTER TABLE patients ADD COLUMN IF NOT EXISTS emergency_contact_name VARCHAR;",
        "ALTER TABLE patients ADD COLUMN IF NOT EXISTS emergency_contact_phone VARCHAR;",
        "ALTER TABLE patients ADD COLUMN IF NOT EXISTS emergency_contact_relation VARCHAR;",
        "ALTER TABLE patients ADD COLUMN IF NOT EXISTS vitals_heart_rate INTEGER;",
        "ALTER TABLE patients ADD COLUMN IF NOT EXISTS vitals_bp VARCHAR;",
        "ALTER TABLE patients ADD COLUMN IF NOT EXISTS vitals_spo2 INTEGER;",
        "ALTER TABLE patients ADD COLUMN IF NOT EXISTS vitals_temperature FLOAT;",
        "ALTER TABLE patients ADD COLUMN IF NOT EXISTS vitals_blood_sugar INTEGER;",
        "ALTER TABLE staff_members ADD COLUMN IF NOT EXISTS hospital_name VARCHAR;",
        "ALTER TABLE staff_schedules ADD COLUMN IF NOT EXISTS hospital_name VARCHAR;",
        "ALTER TABLE leave_requests ADD COLUMN IF NOT EXISTS hospital_name VARCHAR;",
        "ALTER TABLE inventory ADD COLUMN IF NOT EXISTS hospital_name VARCHAR;",
        "ALTER TABLE operational_alerts ADD COLUMN IF NOT EXISTS hospital_name VARCHAR;"
    ]
    for q in alter_queries:
        try:
            await db.execute(text(q))
            await db.commit()
        except Exception:
            await db.rollback()

    print("Checking database for seed data...")
    hashed_password = get_password_hash("Password123!")

    # 1. Seed Users if not present
    seed_users = [
        UserModel(uid="uid_patient_1", email="patient@medislot.com", password_hash=hashed_password, role="patient", full_name="John Doe"),
        UserModel(uid="uid_doc_1", email="john.doe@medislot.com", password_hash=hashed_password, role="doctor", full_name="Dr. John Doe"),
        UserModel(uid="uid_doc_2", email="helen.cho@medislot.com", password_hash=hashed_password, role="doctor", full_name="Dr. Helen Cho"),
        UserModel(uid="uid_doc_3", email="marcus.vance@medislot.com", password_hash=hashed_password, role="doctor", full_name="Dr. Marcus Vance"),
        UserModel(uid="uid_hosp_1", email="coordinator@medislot.com", password_hash=hashed_password, role="hospital_coordinator", full_name="City General Admin"),
        UserModel(uid="uid_super_1", email="admin@medislot.com", password_hash=hashed_password, role="super_admin", full_name="Super Admin")
    ]
    for u in seed_users:
        res = await db.execute(select(UserModel).where(UserModel.uid == u.uid))
        if not res.scalars().first():
            db.add(u)
    await db.commit()

    # 2. Seed Patient
    res_pat = await db.execute(select(PatientModel).where(PatientModel.id == "pat_1"))
    if not res_pat.scalars().first():
        patient = PatientModel(
            id="pat_1",
            uid="uid_patient_1",
            age=30,
            gender="Male",
            contact="+1 (555) 019-2834",
            blood_group="O+",
            height="178 cm",
            weight="75 kg",
            bmi="23.7",
            allergies="Peanuts, Penicillin",
            medications="Vitamin D3, Zyrtec",
            medical_history="Mild Asthma (diagnosed 2018), Sprained Ankle (2022)"
        )
        db.add(patient)

    # 3. Seed Hospital
    res_hosp = await db.execute(select(HospitalModel).where(HospitalModel.id == "hosp_1"))
    if not res_hosp.scalars().first():
        hospital = HospitalModel(
            id="hosp_1",
            name="City General Hospital",
            uid="uid_hosp_1",
            license_number="LIC-99283-GEN",
            registration_number="REG-11029",
            address="123 Main St, Metroville",
            hospital_type="General",
            departments="Cardiology,Neurology,ICU,Pediatrics,Emergency",
            contact="+1 (555) 000-1111",
            admin_name="Coordinator Admin",
            status="Approved",
            docs_attached="License_Doc.pdf"
        )
        db.add(hospital)
    await db.commit()

    # 4. Seed Doctors
    doctors = [
        DoctorModel(
            id="doc_1",
            uid="uid_doc_1",
            specialization="Cardiology",
            hospital_id="hosp_1",
            hospital_name="City General Hospital",
            rating=4.9,
            experience_years=14,
            fees="$120",
            bio="Senior Cardiologist specialized in preventive cardiology, heart failure management, and cardiovascular imaging.",
            availability="Monday - Friday",
            slot_times="09:00 AM,10:30 AM,11:00 AM,02:30 PM,04:00 PM",
            contact="+1 (555) 123-4567",
            status="On Duty",
            room="Room 2A",
            shift="Morning Shift",
            mbbs_institution="Johns Hopkins School of Medicine",
            registration_number="MC-99283"
        ),
        DoctorModel(
            id="doc_2",
            uid="uid_doc_2",
            specialization="Neurology",
            hospital_id="hosp_1",
            hospital_name="City General Hospital",
            rating=4.8,
            experience_years=10,
            fees="$150",
            bio="Experienced Neurologist focused on headache disorders, epilepsy management, and stroke prevention.",
            availability="Tuesday & Thursday",
            slot_times="09:30 AM,10:00 AM,01:30 PM,03:00 PM",
            contact="+1 (555) 234-5678",
            status="On Duty",
            room="Room 3B",
            shift="Afternoon Shift",
            mbbs_institution="Harvard Medical School",
            registration_number="MC-11029"
        ),
        DoctorModel(
            id="doc_3",
            uid="uid_doc_3",
            specialization="Orthopedics",
            hospital_id="hosp_1",
            hospital_name="City General Hospital",
            rating=4.7,
            experience_years=12,
            fees="$100",
            bio="Orthopedic Surgeon specialized in sports medicine and arthroscopic joint reconstruction surgery.",
            availability="Monday, Wednesday, Friday",
            slot_times="10:00 AM,11:30 AM,03:30 PM,04:30 PM",
            contact="+1 (555) 345-6789",
            status="On Duty",
            room="Room 1C",
            shift="Day Shift",
            mbbs_institution="Stanford University School of Medicine",
            registration_number="MC-88721"
        )
    ]
    for d in doctors:
        res = await db.execute(select(DoctorModel).where(DoctorModel.id == d.id))
        if not res.scalars().first():
            db.add(d)

    # 5. Seed Appointments
    appointments = [
        AppointmentModel(
            id="apt_1",
            patient_id="pat_1",
            doctor_id="doc_1",
            doctor_name="Dr. John Doe",
            department="Cardiology",
            hospital="City General Hospital",
            date="2026-08-10",
            time="10:30 AM",
            status="Upcoming",
            queue_number=1
        ),
        AppointmentModel(
            id="apt_2",
            patient_id="pat_1",
            doctor_id="doc_2",
            doctor_name="Dr. Helen Cho",
            department="Neurology",
            hospital="City General Hospital",
            date="2026-08-08",
            time="01:30 PM",
            status="Completed",
            queue_number=2
        ),
        AppointmentModel(
            id="apt_3",
            patient_id="pat_1",
            doctor_id="doc_3",
            doctor_name="Dr. Marcus Vance",
            department="Orthopedics",
            hospital="City General Hospital",
            date="2026-08-12",
            time="11:30 AM",
            status="Upcoming",
            queue_number=3
        )
    ]
    for a in appointments:
        res = await db.execute(select(AppointmentModel).where(AppointmentModel.id == a.id))
        if not res.scalars().first():
            db.add(a)

    # 6. Seed Medical Records
    records = [
        MedicalRecordModel(
            id="rec_1",
            patient_id="pat_1",
            title="Cardiology Follow-up Prescription",
            record_type="Prescription",
            date="2026-08-08",
            file_url="prescription_doc.pdf",
            result_summary="Lisinopril 10mg once daily for hypertension control. Maintain low-sodium diet and check blood pressure daily.",
            doctor_id="doc_1"
        ),
        MedicalRecordModel(
            id="rec_2",
            patient_id="pat_1",
            title="Lipid Panel Blood Test",
            record_type="Lab Report",
            date="2026-08-05",
            file_url="lipid_panel_report.pdf",
            result_summary="Total Cholesterol: 195 mg/dL (Normal). LDL: 110 mg/dL (Borderline). Triglycerides: 150 mg/dL. Recommended dietary modifications.",
            doctor_id="doc_1"
        )
    ]
    for r in records:
        res = await db.execute(select(MedicalRecordModel).where(MedicalRecordModel.id == r.id))
        if not res.scalars().first():
            db.add(r)

    # 7. Seed Staff Members
    staff = [
        StaffMemberModel(id="stf_1", hospital_name="City General Hospital", name="Nurse Clara Barton", role="Nurse", department="Pediatrics", room="Room 1A", status="On Duty"),
        StaffMemberModel(id="stf_2", hospital_name="City General Hospital", name="Technician Marie Curie", role="Lab Technician", department="Cardiology", room="Room 2B", status="On Duty")
    ]
    for s in staff:
        res = await db.execute(select(StaffMemberModel).where(StaffMemberModel.id == s.id))
        if not res.scalars().first():
            db.add(s)

    # 8. Seed Staff Schedules
    schedules = [
        StaffScheduleModel(id="sch_1", hospital_name="City General Hospital", name="Dr. John Doe", role="Doctor", department="Cardiology", date="Monday", shift_type="Morning", shift_time="08:00 AM - 12:00 PM", room="Room 2A", status="On Duty"),
        StaffScheduleModel(id="sch_2", hospital_name="City General Hospital", name="Dr. Helen Cho", role="Doctor", department="Neurology", date="Tuesday", shift_type="Afternoon", shift_time="12:00 PM - 04:00 PM", room="Room 3B", status="On Duty"),
        StaffScheduleModel(id="sch_3", hospital_name="City General Hospital", name="Nurse Clara Barton", role="Nurse", department="Pediatrics", date="Wednesday", shift_type="Night", shift_time="08:00 PM - 08:00 AM", room="Room 1A", status="On Duty"),
        StaffScheduleModel(id="sch_4", hospital_name="City General Hospital", name="Dr. Marcus Vance", role="Doctor", department="Orthopedics", date="Monday", shift_type="Morning", shift_time="08:00 AM - 12:00 PM", room="Room 1C", status="On Duty")
    ]
    for sc in schedules:
        res = await db.execute(select(StaffScheduleModel).where(StaffScheduleModel.id == sc.id))
        if not res.scalars().first():
            db.add(sc)

    # 9. Seed Leave Requests
    leaves = [
        LeaveRequestModel(id="lv_1", hospital_name="City General Hospital", staff_id="stf_1", staff_name="Nurse Clara Barton", role="Nurse", department="Pediatrics", start_date="Aug 10", end_date="Aug 14", reason="Family emergency and personal travel.", status="Pending"),
        LeaveRequestModel(id="lv_2", hospital_name="City General Hospital", staff_id="doc_2", staff_name="Dr. Helen Cho", role="Doctor", department="Neurology", start_date="Aug 12", end_date="Aug 13", reason="Medical checkup appointment.", status="Pending")
    ]
    for l in leaves:
        res = await db.execute(select(LeaveRequestModel).where(LeaveRequestModel.id == l.id))
        if not res.scalars().first():
            db.add(l)

    # 10. Seed Inventory
    inventory = [
        InventoryModel(id="inv_1", hospital_name="City General Hospital", name="ICU Beds Available", total=50, available=12, unit="Beds", category="ICU", last_updated="Updated just now", trend="Decreasing", is_trend_positive=False),
        InventoryModel(id="inv_2", hospital_name="City General Hospital", name="Oxygen Concentrators", total=100, available=82, unit="Units", category="Gas", last_updated="Updated 2h ago", trend="Increasing", is_trend_positive=True),
        InventoryModel(id="inv_3", hospital_name="City General Hospital", name="O- Blood Bags", total=30, available=8, unit="Bags", category="Blood bank", last_updated="Updated just now", trend="Critical Low", is_trend_positive=False),
        InventoryModel(id="inv_4", hospital_name="City General Hospital", name="O+ Blood Bags", total=50, available=35, unit="Bags", category="Blood bank", last_updated="Updated 1h ago", trend="Stable", is_trend_positive=True),
        InventoryModel(id="inv_5", hospital_name="City General Hospital", name="Paracetamol 500mg", total=5000, available=4200, unit="Tablets", category="Medicines", last_updated="Updated 4h ago", trend="Stable", is_trend_positive=True),
        InventoryModel(id="inv_6", hospital_name="City General Hospital", name="Amoxicillin 250mg", total=1000, available=900, unit="Capsules", category="Medicines", last_updated="Updated just now", trend="Stable", is_trend_positive=True),
        InventoryModel(id="inv_7", hospital_name="City General Hospital", name="Disposable Syringes", total=2000, available=1850, unit="Units", category="Equipment", last_updated="Updated 5h ago", trend="Stable", is_trend_positive=True)
    ]
    for i in inventory:
        res = await db.execute(select(InventoryModel).where(InventoryModel.id == i.id))
        if not res.scalars().first():
            db.add(i)

    # 11. Seed Operational Alerts
    alerts = [
        OperationalAlertModel(id="al_1", hospital_name="City General Hospital", title="Critical Low: O- Blood Bags", message="O- Blood bags have dropped below safety thresholds (8 bags left). Immediate replacement requested.", severity="Critical", timestamp="10 mins ago", department="Blood bank", is_resolved=False),
        OperationalAlertModel(id="al_2", hospital_name="City General Hospital", title="Staff Shortage - Pediatrics", message="Nurse Clara Barton approved leave leaves Pediatrics shift short on Wednesday night.", severity="High", timestamp="1 hr ago", department="Pediatrics", is_resolved=False)
    ]
    for al in alerts:
        res = await db.execute(select(OperationalAlertModel).where(OperationalAlertModel.id == al.id))
        if not res.scalars().first():
            db.add(al)

    await db.commit()
    print("Database check completed!")
