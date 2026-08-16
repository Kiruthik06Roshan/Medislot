import asyncio
import uuid
import sys
import httpx

# Add backend to path
backend_path = r"c:\Users\saita\AndroidStudioProjects\MediSlot\backend"
if backend_path not in sys.path:
    sys.path.append(backend_path)

from sqlalchemy.future import select
from app.database.connection import AsyncSessionLocal
from app.database.models import UserModel, HospitalModel, DoctorApplicationModel, DoctorModel

BASE_URL = "http://localhost:8000"

async def get_hospital_from_db(uid: str):
    async with AsyncSessionLocal() as db:
        res = await db.execute(select(HospitalModel).where(HospitalModel.uid == uid))
        h = res.scalars().first()
        if h:
            return {"id": h.id, "name": h.name, "status": h.status, "uid": h.uid}
        return None

async def get_doctor_from_db(uid: str):
    async with AsyncSessionLocal() as db:
        res = await db.execute(select(DoctorModel).where(DoctorModel.uid == uid))
        d = res.scalars().first()
        if d:
            return {"id": d.id, "uid": d.uid, "specialization": d.specialization, "hospital_name": d.hospital_name}
        return None

async def get_doctor_app_from_db(uid: str):
    async with AsyncSessionLocal() as db:
        res = await db.execute(select(DoctorApplicationModel).where(DoctorApplicationModel.uid == uid))
        a = res.scalars().first()
        if a:
            return {"id": a.id, "uid": a.uid, "status": a.status, "selected_hospital": a.selected_hospital}
        return None

async def cleanup_temp_records(hosp_uid: str, doc_uid: str, distractor_doc_uid: str = None, hosp_b_uid: str = None):
    async with AsyncSessionLocal() as db:
        for uid in [doc_uid, distractor_doc_uid]:
            if uid:
                res = await db.execute(select(DoctorModel).where(DoctorModel.uid == uid))
                d = res.scalars().first()
                if d:
                    await db.delete(d)
                res2 = await db.execute(select(DoctorApplicationModel).where(DoctorApplicationModel.uid == uid))
                apps = res2.scalars().all()
                for a in apps:
                    await db.delete(a)
        
        for uid in [hosp_uid, hosp_b_uid]:
            if uid:
                res3 = await db.execute(select(HospitalModel).where(HospitalModel.uid == uid))
                h = res3.scalars().first()
                if h:
                    await db.delete(h)
        
        uids = [hosp_uid, doc_uid, distractor_doc_uid, hosp_b_uid]
        uids = [u for u in uids if u]
        if uids:
            res4 = await db.execute(select(UserModel).where(UserModel.uid.in_(uids)))
            users = res4.scalars().all()
            for u in users:
                await db.delete(u)
        await db.commit()

async def run_e2e():
    suffix = uuid.uuid4().hex[:6]
    hosp_email = f"temp_hosp_{suffix}@medislot.test"
    hosp_name = f"Temp Hospital {suffix}"
    
    doc_email = f"temp_doc_{suffix}@medislot.test"
    doc_name = f"Dr. Temp Bava {suffix}"

    sa_email = f"temp_sa_{suffix}@medislot.test"

    distractor_email = f"temp_distractor_{suffix}@medislot.test"
    distractor_name = f"Dr. Temp Distractor {suffix}"

    hosp_b_email = f"hosp_b_{suffix}@medislot.test"

    print("Step 1: Registering temporary users...")
    async with httpx.AsyncClient() as client:
        # Register Hospital
        r1 = await client.post(f"{BASE_URL}/api/auth/register", json={
            "email": hosp_email,
            "password": "Password123!",
            "full_name": "Temp Hospital Admin",
            "role": "hospital"
        })
        assert r1.status_code == 200, f"Hosp Reg: {r1.text}"
        hosp_uid = r1.json()["uid"]
        hosp_headers = {"Authorization": f"Bearer {r1.json()['access_token']}"}

        # Register Doctor
        r2 = await client.post(f"{BASE_URL}/api/auth/register", json={
            "email": doc_email,
            "password": "Password123!",
            "full_name": doc_name,
            "role": "doctor"
        })
        assert r2.status_code == 200
        doc_uid = r2.json()["uid"]
        doc_headers = {"Authorization": f"Bearer {r2.json()['access_token']}"}

        # Register Super Admin
        r3 = await client.post(f"{BASE_URL}/api/auth/register", json={
            "email": sa_email,
            "password": "Password123!",
            "full_name": "Temp Super Admin",
            "role": "super_admin"
        })
        assert r3.status_code == 200
        sa_headers = {"Authorization": f"Bearer {r3.json()['access_token']}"}

        # Register Distractor Doctor
        r4 = await client.post(f"{BASE_URL}/api/auth/register", json={
            "email": distractor_email,
            "password": "Password123!",
            "full_name": distractor_name,
            "role": "doctor"
        })
        assert r4.status_code == 200
        distractor_uid = r4.json()["uid"]
        dist_headers = {"Authorization": f"Bearer {r4.json()['access_token']}"}

        try:
            print("Step 2: Auto-creating hospital profile and registering details...")
            # A. Register temporary Hospital (coordinator registers details)
            await client.get(f"{BASE_URL}/api/hospital/profile/{hosp_uid}", headers=hosp_headers)
            hosp_details = await client.post(f"{BASE_URL}/api/hospital/register", headers=hosp_headers, json={
                "name": hosp_name,
                "uid": hosp_uid,
                "license_number": f"LIC-TEMP-{suffix.upper()}",
                "registration_number": f"REG-TEMP-{suffix.upper()}",
                "address": "456 Temp Street, Tech City",
                "hospital_type": "Specialty",
                "departments": "Orthopedics, Pediatrics, Cardiology",
                "contact": "+1 (555) 555-5555",
                "admin_name": "Temp Hospital Admin"
            })
            assert hosp_details.status_code == 200

            # B. Verify PostgreSQL record exists
            print("Checking PostgreSQL for hospital record...")
            db_hosp = await get_hospital_from_db(hosp_uid)
            assert db_hosp is not None, "Hospital record not found in DB"
            assert db_hosp["name"] == hosp_name

            # C. Verify status = PENDING
            print(f"Hospital Status in DB: {db_hosp['status']}")
            assert db_hosp["status"] == "Pending"

            # D. Super Admin retrieves it
            print("Super Admin retrieving hospital applications...")
            sa_list = await client.get(f"{BASE_URL}/api/hospital/all", headers=sa_headers)
            assert sa_list.status_code == 200
            assert any(h["uid"] == hosp_uid for h in sa_list.json())

            # E. Super Admin approves it
            print("Super Admin approving hospital status...")
            sa_approve = await client.post(f"{BASE_URL}/api/hospital/{db_hosp['id']}/status?status=Approved", headers=sa_headers)
            assert sa_approve.status_code == 200

            # F. Verify PostgreSQL status = APPROVED
            db_hosp_approved = await get_hospital_from_db(hosp_uid)
            assert db_hosp_approved["status"] == "Approved"

            # G. Register temporary Doctor associated with that hospital
            print("Doctor submitting application...")
            doc_app_res = await client.post(f"{BASE_URL}/api/hospital/recruitment", headers=doc_headers, json={
                "uid": doc_uid,
                "name": doc_name,
                "specialization": "Pediatrics",
                "experience_years": "7",
                "medical_registration_number": f"REG-DOC-{suffix.upper()}",
                "mbbs_institution": "Tech Medical School",
                "selected_hospital": hosp_name
            })
            assert doc_app_res.status_code == 200
            doc_app_id = doc_app_res.json()["id"]

            # H. Verify Doctor is PENDING
            print("Checking PostgreSQL for pending doctor application...")
            db_doc_app = await get_doctor_app_from_db(doc_uid)
            assert db_doc_app is not None
            assert db_doc_app["status"] == "Pending"

            # I. Hospital Admin retrieves Doctor applications
            print("Hospital Admin retrieving applications...")
            hosp_list = await client.get(f"{BASE_URL}/api/hospital/recruitment", headers=hosp_headers)
            assert hosp_list.status_code == 200
            assert any(x["id"] == doc_app_id for x in hosp_list.json())

            # J. Hospital Admin approves Doctor
            print("Hospital Admin approving doctor application...")
            hosp_approve = await client.post(f"{BASE_URL}/api/hospital/recruitment/{doc_app_id}/status?status=Approved", headers=hosp_headers)
            assert hosp_approve.status_code == 200

            # K. Verify PostgreSQL Doctor status = APPROVED and doctor profile exists
            print("Checking DB for approved doctor profile...")
            db_doc_app_approved = await get_doctor_app_from_db(doc_uid)
            assert db_doc_app_approved["status"] == "Approved"
            
            db_doc_profile = await get_doctor_from_db(doc_uid)
            assert db_doc_profile is not None
            assert db_doc_profile["specialization"] == "Pediatrics"
            assert db_doc_profile["hospital_name"] == hosp_name

            # L. Verify Doctor can access the appropriate dashboard
            print("Checking Doctor status endpoint...")
            status_res = await client.get(f"{BASE_URL}/api/auth/status/{doc_uid}")
            assert status_res.status_code == 200
            assert status_res.json()["status"] == "Approved"

            # M. Test rejection workflow
            print("Testing doctor application rejection workflow...")
            distractor_app_res = await client.post(f"{BASE_URL}/api/hospital/recruitment", headers=dist_headers, json={
                "uid": distractor_uid,
                "name": distractor_name,
                "specialization": "Cardiology",
                "experience_years": "3",
                "medical_registration_number": f"REG-DIS-{suffix.upper()}",
                "mbbs_institution": "Distractor College",
                "selected_hospital": hosp_name
            })
            assert distractor_app_res.status_code == 200
            distractor_app_id = distractor_app_res.json()["id"]

            hosp_reject = await client.post(f"{BASE_URL}/api/hospital/recruitment/{distractor_app_id}/status?status=Rejected&rejection_reason=BadDocs", headers=hosp_headers)
            assert hosp_reject.status_code == 200
            assert hosp_reject.json()["status"] == "Rejected"
            assert hosp_reject.json()["rejection_reason"] == "BadDocs"

            # N. Test unauthorized approval attempt (Super Admin cannot approve doctor)
            print("Verifying Super Admin cannot approve doctor...")
            sa_approve_doc = await client.post(f"{BASE_URL}/api/hospital/recruitment/{doc_app_id}/status?status=Approved", headers=sa_headers)
            assert sa_approve_doc.status_code == 403

            # O. Verify Hospital Admin cannot modify another hospital's doctor
            print("Verifying data isolation bounds between hospitals...")
            hosp_b_reg = await client.post(f"{BASE_URL}/api/auth/register", json={
                "email": hosp_b_email,
                "password": "Password123!",
                "full_name": "Hospital B Coordinator",
                "role": "hospital"
            })
            assert hosp_b_reg.status_code == 200
            hosp_b_uid = hosp_b_reg.json()["uid"]
            hosp_b_headers = {"Authorization": f"Bearer {hosp_b_reg.json()['access_token']}"}
            
            await client.get(f"{BASE_URL}/api/hospital/profile/{hosp_b_uid}", headers=hosp_b_headers)
            hosp_b_details = await client.post(f"{BASE_URL}/api/hospital/register", headers=hosp_b_headers, json={
                "name": f"Hospital B {suffix}",
                "uid": hosp_b_uid,
                "license_number": f"LIC-GEN-B-{suffix.upper()}",
                "registration_number": f"REG-GEN-B-{suffix.upper()}",
                "address": "789 Tech Road",
                "hospital_type": "General",
                "departments": "Emergency",
                "contact": "+1 (555) 555-6666",
                "admin_name": "Hospital B coordinator"
            })
            assert hosp_b_details.status_code == 200

            unauth_approve = await client.post(f"{BASE_URL}/api/hospital/recruitment/{doc_app_id}/status?status=Approved", headers=hosp_b_headers)
            assert unauth_approve.status_code == 403

            # P. Verify Normal Mode never returns MockData
            print("Checking that Normal Mode endpoints do not fall back to MockData...")
            # If we query an invalid coordinator profile, it returns 404 instead of mock data fallback
            fake_prof = await client.get(f"{BASE_URL}/api/hospital/profile/nonexistent_uid")
            assert fake_prof.status_code == 404

            print("SUCCESS: ALL DATABASE E2E CHECKS PASSED SUCCESSFULLY!")

        finally:
            print("Cleaning up temporary E2E accounts and records from PostgreSQL...")
            await cleanup_temp_records(hosp_uid, doc_uid, distractor_uid, hosp_b_uid)

if __name__ == "__main__":
    asyncio.run(run_e2e())
