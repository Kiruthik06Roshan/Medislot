import pytest
import uuid
from fastapi.testclient import TestClient
from app.main import app

@pytest.fixture(scope="module")
def client():
    with TestClient(app) as c:
        yield c

def test_authentication_flows(client):
    # 1. Random user login -> 401
    res = client.post("/api/auth/login", json={
        "email": "nonexistent_random_user_99@medislot.test",
        "password": "WrongPassword123!"
    })
    assert res.status_code == 401

    # 2. Register valid user
    shared_suffix = uuid.uuid4().hex[:6]
    unique_email = f"pytest_user_{shared_suffix}@medislot.test"
    unique_name = f"Loki_{shared_suffix} Patient"
    reg_res = client.post("/api/auth/register", json={
        "email": unique_email,
        "password": "ValidPassword123!",
        "full_name": unique_name,
        "role": "patient"
    })
    assert reg_res.status_code == 200
    data = reg_res.json()
    assert "access_token" in data
    assert data["email"] == unique_email
    uid = data["uid"]

    # 3. Wrong password -> 401
    wrong_res = client.post("/api/auth/login", json={
        "email": unique_email,
        "password": "WrongPassword123!"
    })
    assert wrong_res.status_code == 401

    # 4. Correct password with Email -> 200
    login_res = client.post("/api/auth/login", json={
        "email": unique_email,
        "password": "ValidPassword123!"
    })
    assert login_res.status_code == 200
    assert login_res.json()["uid"] == uid

    # 5. Correct password with Full Name / Username -> 200 (Resolves to same UID)
    login_username = client.post("/api/auth/login", json={
        "email": unique_name,
        "password": "ValidPassword123!"
    })
    assert login_username.status_code == 200
    assert login_username.json()["uid"] == uid

    # 6. Login with Whitespace Padding -> 200
    login_whitespace = client.post("/api/auth/login", json={
        "email": f"  {unique_name}  ",
        "password": "ValidPassword123!"
    })
    assert login_whitespace.status_code == 200
    assert login_whitespace.json()["uid"] == uid

    # 7. Login with Case Insensitivity -> 200
    login_casing = client.post("/api/auth/login", json={
        "email": unique_name.upper(),
        "password": "ValidPassword123!"
    })
    assert login_casing.status_code == 200
    assert login_casing.json()["uid"] == uid

    # 8. Login with First Name ("Loki") -> 200
    login_firstname = client.post("/api/auth/login", json={
        "email": unique_name.split()[0],
        "password": "ValidPassword123!"
    })
    assert login_firstname.status_code == 200
    assert login_firstname.json()["uid"] == uid

    # 9. Login with Email Prefix -> 200
    login_prefix = client.post("/api/auth/login", json={
        "email": unique_email.split("@")[0],
        "password": "ValidPassword123!"
    })
    assert login_prefix.status_code == 200
    assert login_prefix.json()["uid"] == uid

    # 10. Exact name prioritization test: Register user "Loki_<suffix>" alongside "Loki_<suffix> Patient ..."
    exact_loki_email = f"loki_exact_{shared_suffix}@medislot.test"
    exact_loki_name = f"Loki_{shared_suffix}"
    exact_loki_pass = "ExactLokiPass123!"
    reg_exact = client.post("/api/auth/register", json={
        "email": exact_loki_email,
        "password": exact_loki_pass,
        "full_name": exact_loki_name,
        "role": "patient"
    })
    assert reg_exact.status_code == 200
    exact_loki_uid = reg_exact.json()["uid"]

    # Login as exact name must resolve to exact account UID, not distractor "Loki_<suffix> Patient ..."
    loki_login = client.post("/api/auth/login", json={
        "email": exact_loki_name,
        "password": exact_loki_pass
    })
    assert loki_login.status_code == 200
    assert loki_login.json()["uid"] == exact_loki_uid

    # Whitespace and upper case for exact
    loki_whitespace = client.post("/api/auth/login", json={
        "email": f"   {exact_loki_name.upper()}   ",
        "password": exact_loki_pass
    })
    assert loki_whitespace.status_code == 200
    assert loki_whitespace.json()["uid"] == exact_loki_uid


def test_patient_profile_flow(client):
    unique_email = f"pytest_patient_{uuid.uuid4().hex[:6]}@medislot.test"
    reg_res = client.post("/api/auth/register", json={
        "email": unique_email,
        "password": "PatientPass123!",
        "full_name": "Pytest Patient",
        "role": "patient"
    })
    assert reg_res.status_code == 200
    uid = reg_res.json()["uid"]

    # Fetch profile
    prof_res = client.get(f"/api/patients/profile/{uid}")
    assert prof_res.status_code == 200
    prof_data = prof_res.json()
    assert prof_data["uid"] == uid

    # Update profile
    update_res = client.put("/api/patients/profile", json={
        "uid": uid,
        "age": 30,
        "gender": "Male",
        "contact": "+1 (555) 111-2222",
        "blood_group": "A+",
        "height": "180 cm",
        "weight": "75 kg",
        "bmi": "23.1",
        "allergies": "Pollen",
        "medications": "None",
        "medical_history": "None"
    })
    assert update_res.status_code == 200
    assert update_res.json()["blood_group"] == "A+"

    # Refetch profile
    refetch = client.get(f"/api/patients/profile/{uid}")
    assert refetch.status_code == 200
    assert refetch.json()["allergies"] == "Pollen"


def test_doctor_profile_and_registration(client):
    unique_email = f"pytest_doc_{uuid.uuid4().hex[:6]}@medislot.test"
    reg_res = client.post("/api/auth/register", json={
        "email": unique_email,
        "password": "DoctorPass123!",
        "full_name": "Dr. Pytest Doctor",
        "role": "doctor"
    })
    assert reg_res.status_code == 200
    uid = reg_res.json()["uid"]

    # Update doctor profile
    doc_put = client.put("/api/doctors/profile", json={
        "uid": uid,
        "specialization": "Orthopedics",
        "hospital_name": "City Sports Medicine",
        "experience_years": 8,
        "contact": "+1 (555) 333-4444",
        "mbbs_institution": "Stanford Medical School",
        "registration_number": "REG-DOC-5566"
    })
    assert doc_put.status_code == 200

    # Fetch doctor profile
    doc_get = client.get(f"/api/doctors/profile/{uid}")
    assert doc_get.status_code == 200
    assert doc_get.json()["specialization"] == "Orthopedics"
    assert doc_get.json()["uid"] == uid


def test_hospital_profile_and_registration(client):
    unique_email = f"pytest_hosp_{uuid.uuid4().hex[:6]}@medislot.test"
    reg_res = client.post("/api/auth/register", json={
        "email": unique_email,
        "password": "HospPass123!",
        "full_name": "Pytest Hospital Admin",
        "role": "hospital"
    })
    assert reg_res.status_code == 200
    uid = reg_res.json()["uid"]
    token = reg_res.json()["access_token"]

    hosp_reg = client.post("/api/hospital/register", headers={"Authorization": f"Bearer {token}"}, json={
        "name": "Pytest Memorial Hospital",
        "uid": uid,
        "license_number": "LIC-PYTEST-01",
        "registration_number": f"REG-{uuid.uuid4().hex[:6]}",
        "address": "123 Science Way, Metro City",
        "hospital_type": "Specialty",
        "departments": "Orthopedics, Pediatrics",
        "contact": "+1 (555) 777-9999",
        "admin_name": "Pytest Hospital Admin"
    })
    assert hosp_reg.status_code == 200

    hosp_get = client.get(f"/api/hospital/profile/{uid}")
    assert hosp_get.status_code == 200
    assert hosp_get.json()["name"] == "Pytest Memorial Hospital"


def test_appointment_booking(client):
    pat_email = f"pytest_appt_pat_{uuid.uuid4().hex[:6]}@medislot.test"
    doc_email = f"pytest_appt_doc_{uuid.uuid4().hex[:6]}@medislot.test"
    
    p_res = client.post("/api/auth/register", json={"email": pat_email, "password": "Pass!", "full_name": "Appt Patient", "role": "patient"})
    d_res = client.post("/api/auth/register", json={"email": doc_email, "password": "Pass!", "full_name": "Appt Doctor", "role": "doctor"})
    
    p_uid = p_res.json()["uid"]
    d_uid = d_res.json()["uid"]

    apt_payload = {
        "patient_id": p_uid,
        "doctor_id": d_uid,
        "doctor_name": "Appt Doctor",
        "department": "General Medicine",
        "hospital": "City Hospital",
        "date": "2026-09-01",
        "time": "09:00 AM"
    }
    apt_res = client.post("/api/appointments", json=apt_payload)
    assert apt_res.status_code == 200, f"Got {apt_res.status_code}: {apt_res.json()}"
    assert apt_res.json()["status"] in ["Upcoming", "Scheduled", "Confirmed"]


def test_appointment_reschedule_and_conflict(client):
    doc_id = f"doc_{uuid.uuid4().hex[:6]}"
    pat_id1 = f"pat_{uuid.uuid4().hex[:6]}"
    pat_id2 = f"pat_{uuid.uuid4().hex[:6]}"

    # Book apt 1: Sep 10, 10:00 AM
    apt1 = client.post("/api/appointments", json={
        "patient_id": pat_id1,
        "doctor_id": doc_id,
        "doctor_name": "Dr. Conflict Test",
        "department": "Cardiology",
        "hospital": "Central Clinic",
        "date": "2026-09-10",
        "time": "10:00 AM"
    }).json()
    apt1_id = apt1["id"]

    # Reschedule apt 1 to Sep 11, 11:00 AM
    resched1 = client.put(f"/api/appointments/{apt1_id}/reschedule?date=2026-09-11&time=11:00%20AM")
    assert resched1.status_code == 200
    assert resched1.json()["date"] == "2026-09-11"
    assert resched1.json()["time"] == "11:00 AM"

    # Book apt 2: Sep 11, 02:00 PM
    apt2 = client.post("/api/appointments", json={
        "patient_id": pat_id2,
        "doctor_id": doc_id,
        "doctor_name": "Dr. Conflict Test",
        "department": "Cardiology",
        "hospital": "Central Clinic",
        "date": "2026-09-11",
        "time": "02:00 PM"
    }).json()
    apt2_id = apt2["id"]

    # Attempt to reschedule apt 1 to Sep 11, 02:00 PM (Conflicting slot) -> 409 Conflict
    conflict_res = client.put(f"/api/appointments/{apt1_id}/reschedule?date=2026-09-11&time=02:00%20PM")
    assert conflict_res.status_code == 409

    # Nonexistent appointment -> 404
    nonexist_res = client.put("/api/appointments/apt_nonexistent_99/reschedule?date=2026-09-11&time=04:00%20PM")
    assert nonexist_res.status_code == 404


def test_appointment_cancellation(client):
    pat_id = f"pat_{uuid.uuid4().hex[:6]}"
    doc_id = f"doc_{uuid.uuid4().hex[:6]}"

    apt = client.post("/api/appointments", json={
        "patient_id": pat_id,
        "doctor_id": doc_id,
        "doctor_name": "Dr. Cancel Test",
        "department": "Neurology",
        "hospital": "City Hospital",
        "date": "2026-09-15",
        "time": "03:00 PM"
    }).json()
    apt_id = apt["id"]

    # Cancel appointment
    cancel_res = client.put(f"/api/appointments/{apt_id}/cancel")
    assert cancel_res.status_code == 200
    assert cancel_res.json()["status"] == "Cancelled"

    # Cancel nonexistent appointment -> 404
    nonexist_cancel = client.put("/api/appointments/apt_nonexistent_99/cancel")
    assert nonexist_cancel.status_code == 404


def test_doctor_search_and_filtering(client):
    # 1. Fetch all doctors
    all_res = client.get("/api/doctors/all")
    assert all_res.status_code == 200
    doctors = all_res.json()
    assert len(doctors) > 0

    # Verify no password hash or secrets exposed in DoctorResponse
    first_doc = doctors[0]
    assert "name" in first_doc
    assert "specialization" in first_doc
    assert "hashed_password" not in first_doc
    assert "password" not in first_doc

    # 2. Register a new doctor
    unique_email = f"pytest_search_doc_{uuid.uuid4().hex[:6]}@medislot.test"
    reg_res = client.post("/api/auth/register", json={
        "email": unique_email,
        "password": "DocPassword123!",
        "full_name": "Dr. Pytest Search Specialist",
        "role": "doctor"
    })
    assert reg_res.status_code == 200
    d_uid = reg_res.json()["uid"]

    # Update new doctor's profile with distinct specialization
    unique_spec = f"Specialty_{uuid.uuid4().hex[:6]}"
    client.put("/api/doctors/profile", json={
        "uid": d_uid,
        "specialization": unique_spec,
        "hospital_name": "Pytest Research Hospital",
        "experience_years": 15,
        "contact": "+1 (555) 888-0000",
        "mbbs_institution": "Harvard Medical",
        "registration_number": "REG-TEST-99"
    })

    # 3. Filter by unique specialization -> should find newly registered doctor
    spec_res = client.get(f"/api/doctors/all?specialization={unique_spec}")
    assert spec_res.status_code == 200
    spec_docs = spec_res.json()
    assert len(spec_docs) >= 1
    assert any(d["uid"] == d_uid for d in spec_docs)

    # 4. Search by name
    name_res = client.get("/api/doctors/all?name=Search%20Specialist")
    assert name_res.status_code == 200
    assert len(name_res.json()) >= 1

    # 5. Non-matching search -> empty list
    empty_res = client.get("/api/doctors/all?name=NonexistentDoctorQueryXYZ999")
    assert empty_res.status_code == 200
    assert empty_res.json() == []


def test_doctor_appointments_retrieval_and_isolation(client):
    # 1. Register Doctor A and Doctor B
    docA_email = f"pytest_docA_{uuid.uuid4().hex[:6]}@medislot.test"
    docB_email = f"pytest_docB_{uuid.uuid4().hex[:6]}@medislot.test"
    pat_email = f"pytest_pat_{uuid.uuid4().hex[:6]}@medislot.test"

    docA_reg = client.post("/api/auth/register", json={"email": docA_email, "password": "Pass!", "full_name": "Dr. Doctor A", "role": "doctor"}).json()
    docB_reg = client.post("/api/auth/register", json={"email": docB_email, "password": "Pass!", "full_name": "Dr. Doctor B", "role": "doctor"}).json()
    pat_reg = client.post("/api/auth/register", json={"email": pat_email, "password": "Pass!", "full_name": "Patient Alpha", "role": "patient"}).json()

    docA_uid = docA_reg["uid"]
    docB_uid = docB_reg["uid"]
    pat_uid = pat_reg["uid"]

    # Doctor A has no appointments initially -> empty list
    empty_docA = client.get(f"/api/doctors/appointments/{docA_uid}")
    assert empty_docA.status_code == 200
    assert empty_docA.json() == []

    # 2. Patient books appointment with Doctor A
    apt_res = client.post("/api/appointments", json={
        "patient_id": pat_uid,
        "doctor_id": docA_uid,
        "doctor_name": "Dr. Doctor A",
        "department": "Cardiology",
        "hospital": "City Hospital",
        "date": "2026-09-20",
        "time": "11:00 AM"
    })
    assert apt_res.status_code == 200
    apt_id = apt_res.json()["id"]

    # 3. Doctor A fetches appointments -> should return appointment with patient_name
    docA_apts = client.get(f"/api/doctors/appointments/{docA_uid}").json()
    assert len(docA_apts) == 1
    assert docA_apts[0]["id"] == apt_id
    assert docA_apts[0]["patient_name"] == "Patient Alpha"

    # 4. Doctor B fetches appointments -> MUST NOT return Doctor A's appointment (Doctor Isolation)
    docB_apts = client.get(f"/api/doctors/appointments/{docB_uid}").json()
    assert len(docB_apts) == 0

    # 5. Reschedule appointment to Sep 22, 02:00 PM
    client.put(f"/api/appointments/{apt_id}/reschedule?date=2026-09-22&time=02:00%20PM")
    docA_rescheduled = client.get(f"/api/doctors/appointments/{docA_uid}").json()
    assert docA_rescheduled[0]["date"] == "2026-09-22"
    assert docA_rescheduled[0]["time"] == "02:00 PM"

    # 6. Cancel appointment
    client.put(f"/api/appointments/{apt_id}/cancel")
    docA_cancelled = client.get(f"/api/doctors/appointments/{docA_uid}").json()
    assert docA_cancelled[0]["status"] == "Cancelled"


def test_patient_profile_and_appointment_isolation(client):
    # 1. Register new Patient 1 and Patient 2
    p1_email = f"pytest_pat1_{uuid.uuid4().hex[:6]}@medislot.test"
    p2_email = f"pytest_pat2_{uuid.uuid4().hex[:6]}@medislot.test"
    doc_email = f"pytest_doc_{uuid.uuid4().hex[:6]}@medislot.test"

    p1_reg = client.post("/api/auth/register", json={"email": p1_email, "password": "Pass!", "full_name": "Loki Patient", "role": "patient"}).json()
    p2_reg = client.post("/api/auth/register", json={"email": p2_email, "password": "Pass!", "full_name": "Thor Patient", "role": "patient"}).json()
    doc_reg = client.post("/api/auth/register", json={"email": doc_email, "password": "Pass!", "full_name": "Dr. Thor Doctor", "role": "doctor"}).json()

    p1_uid = p1_reg["uid"]
    p2_uid = p2_reg["uid"]
    doc_uid = doc_reg["uid"]

    # 2. Fetch new Patient 1 profile -> fields must be clean / unpopulated (no fake 25 yrs / +1 555 000-0000 data)
    prof1_res = client.get(f"/api/patients/profile/{p1_uid}")
    assert prof1_res.status_code == 200
    prof1 = prof1_res.json()
    assert prof1["age"] == 0
    assert prof1["contact"] == ""
    assert prof1["insurance_provider"] is None
    assert prof1["emergency_contact_phone"] is None

    # 3. Patient 1 has 0 appointments & 0 records initially
    p1_apts = client.get(f"/api/patients/appointments/{p1_uid}").json()
    assert p1_apts == []

    p1_recs = client.get(f"/api/patients/medical-records/{p1_uid}").json()
    assert p1_recs == []

    # 4. Patient 1 books appointment
    apt_res = client.post("/api/appointments", json={
        "patient_id": p1_uid,
        "doctor_id": doc_uid,
        "doctor_name": "Dr. Thor Doctor",
        "department": "Neurology",
        "hospital": "City Hospital",
        "date": "2026-10-01",
        "time": "09:00 AM"
    })
    assert apt_res.status_code == 200

    # 5. Patient 1 fetches appointments -> 1 appointment
    p1_apts_after = client.get(f"/api/patients/appointments/{p1_uid}").json()
    assert len(p1_apts_after) == 1

    # 6. Patient 2 fetches appointments -> MUST NOT return Patient 1's appointment (Patient Isolation)
    p2_apts = client.get(f"/api/patients/appointments/{p2_uid}").json()
    assert len(p2_apts) == 0


def test_verification_workflow_and_authorization_boundaries(client):
    suffix = uuid.uuid4().hex[:6]
    unique_doc_name = f"Dr. Pytest Bava {suffix}"
    unique_hosp_name = f"Apollo Hospital {suffix}"

    # Setup UIDs and registers
    h1_email = f"hosp1_{suffix}@medislot.test"
    h2_email = f"hosp2_{suffix}@medislot.test"
    doc_email = f"doc_{suffix}@medislot.test"
    pat_email = f"pat_{suffix}@medislot.test"
    sa_email = f"sa_{suffix}@medislot.test"

    # Register roles
    h1_reg = client.post("/api/auth/register", json={"email": h1_email, "password": "Pass", "full_name": "Apollo Hospital Admin", "role": "hospital"}).json()
    h2_reg = client.post("/api/auth/register", json={"email": h2_email, "password": "Pass", "full_name": "General Hospital Admin", "role": "hospital"}).json()
    doc_reg = client.post("/api/auth/register", json={"email": doc_email, "password": "Pass", "full_name": unique_doc_name, "role": "doctor"}).json()
    pat_reg = client.post("/api/auth/register", json={"email": pat_email, "password": "Pass", "full_name": "Selva Patient", "role": "patient"}).json()
    sa_reg = client.post("/api/auth/register", json={"email": sa_email, "password": "Pass", "full_name": "Super Admin", "role": "super_admin"}).json()

    h1_token = h1_reg["access_token"]
    h1_uid = h1_reg["uid"]
    h2_token = h2_reg["access_token"]
    h2_uid = h2_reg["uid"]
    doc_token = doc_reg["access_token"]
    doc_uid = doc_reg["uid"]
    pat_token = pat_reg["access_token"]
    pat_uid = pat_reg["uid"]
    sa_token = sa_reg["access_token"]
    sa_uid = sa_reg["uid"]

    h1_headers = {"Authorization": f"Bearer {h1_token}"}
    h2_headers = {"Authorization": f"Bearer {h2_token}"}
    doc_headers = {"Authorization": f"Bearer {doc_token}"}
    pat_headers = {"Authorization": f"Bearer {pat_token}"}
    sa_headers = {"Authorization": f"Bearer {sa_token}"}

    # ASSERTION 1: Coordinator registers, profile lookup auto-creates Hospital record in PENDING state
    h1_prof_res = client.get(f"/api/hospital/profile/{h1_uid}", headers=h1_headers)
    assert h1_prof_res.status_code == 200
    h1_prof = h1_prof_res.json()
    assert h1_prof["status"] == "Pending"
    h1_id = h1_prof["id"]

    # Register Hospital A with specific details
    client.post("/api/hospital/register", headers=h1_headers, json={
        "name": unique_hosp_name,
        "uid": h1_uid,
        "license_number": "LIC-APOLLO",
        "registration_number": f"REG-{uuid.uuid4().hex[:6]}",
        "address": "123 Health Ave",
        "hospital_type": "Multispecialty",
        "departments": "Cardiology, Pediatrics",
        "contact": "+1 (555) 999-0000",
        "admin_name": "Apollo Hospital Admin"
    })
    
    # Refresh profile to fetch the actual registered hospital details
    h1_prof = client.get(f"/api/hospital/profile/{h1_uid}", headers=h1_headers).json()
    assert h1_prof["status"] == "Pending"
    h1_id = h1_prof["id"]

    # ASSERTION 2: Coordinator 2 profile lookup auto-creates Hospital record in PENDING state
    h2_prof_res = client.get(f"/api/hospital/profile/{h2_uid}", headers=h2_headers)
    assert h2_prof_res.status_code == 200
    assert h2_prof_res.json()["status"] == "Pending"

    # Register Hospital B with coordinator 2
    client.post("/api/hospital/register", headers=h2_headers, json={
        "name": "General Hospital",
        "uid": h2_uid,
        "license_number": "LIC-GENERAL",
        "registration_number": f"REG-{uuid.uuid4().hex[:6]}",
        "address": "456 City Rd",
        "hospital_type": "General",
        "departments": "Emergency, Surgery",
        "contact": "+1 (555) 888-0000",
        "admin_name": "General Hospital Admin"
    })
    h2_prof = client.get(f"/api/hospital/profile/{h2_uid}", headers=h2_headers).json()
    h2_id = h2_prof["id"]

    # ASSERTION 3: Coordinator 1 cannot view Super Admin list of all hospitals
    h1_get_all = client.get("/api/hospital/all", headers=h1_headers)
    assert h1_get_all.status_code == 403

    # ASSERTION 4: Patient cannot view Super Admin list of all hospitals
    pat_get_all = client.get("/api/hospital/all", headers=pat_headers)
    assert pat_get_all.status_code == 403

    # ASSERTION 5: Unauthenticated request to get all hospitals -> 401
    unauth_get_all = client.get("/api/hospital/all")
    assert unauth_get_all.status_code == 401

    # ASSERTION 6: Coordinator 1 cannot approve/reject their own or other hospitals
    h1_hstatus = client.post(f"/api/hospital/{h1_id}/status?status=Approved", headers=h1_headers)
    assert h1_hstatus.status_code == 403

    # ASSERTION 7: Patient cannot approve/reject hospital status
    pat_hstatus = client.post(f"/api/hospital/{h1_id}/status?status=Approved", headers=pat_headers)
    assert pat_hstatus.status_code == 403

    # ASSERTION 8: Doctor creates recruitment application targeting Apollo Hospital
    doc_app_res = client.post("/api/hospital/recruitment", headers=doc_headers, json={
        "uid": doc_uid,
        "name": unique_doc_name,
        "specialization": "Pediatrics",
        "experience_years": "6",
        "medical_registration_number": f"REG-BAVA-{uuid.uuid4().hex[:6]}",
        "mbbs_institution": "Harvard Medical School",
        "selected_hospital": unique_hosp_name
    })
    assert doc_app_res.status_code == 200
    doc_app = doc_app_res.json()
    assert doc_app["status"] == "Pending"
    doc_app_id = doc_app["id"]

    # ASSERTION 9: Doctor cannot create recruitment application using another doctor's UID
    malicious_app = client.post("/api/hospital/recruitment", headers=doc_headers, json={
        "uid": h1_uid, # Distractor UID
        "name": "Dr. Malicious",
        "specialization": "Cardiology",
        "experience_years": "1",
        "medical_registration_number": f"REG-MAL-{uuid.uuid4().hex[:6]}",
        "mbbs_institution": "Fake Univ",
        "selected_hospital": unique_hosp_name
    })
    assert malicious_app.status_code == 403

    # ASSERTION 10: Check authorization status endpoint for doctor shows Pending
    st_res = client.get(f"/api/auth/status/{doc_uid}")
    assert st_res.status_code == 200
    assert st_res.json()["status"] == "Pending"
    assert st_res.json()["hospital_name"] == unique_hosp_name

    # ASSERTION 11: Super Admin can successfully fetch all hospitals
    sa_all = client.get("/api/hospital/all", headers=sa_headers)
    assert sa_all.status_code == 200
    all_hospitals = sa_all.json()
    assert len(all_hospitals) >= 2

    # ASSERTION 12: Super Admin approves Apollo Hospital coordinator
    sa_approve_h1 = client.post(f"/api/hospital/{h1_id}/status?status=Approved", headers=sa_headers)
    assert sa_approve_h1.status_code == 200
    assert sa_approve_h1.json()["status"] == "Approved"

    # ASSERTION 13: Super Admin rejects General Hospital with reason
    sa_reject_h2 = client.post(f"/api/hospital/{h2_id}/status?status=Rejected&rejection_reason=Incomplete%20registration%20docs", headers=sa_headers)
    assert sa_reject_h2.status_code == 200
    assert sa_reject_h2.json()["status"] == "Rejected"
    assert sa_reject_h2.json()["rejection_reason"] == "Incomplete registration docs"

    # ASSERTION 14: Check status endpoint for Approved Hospital A coordinator
    st_h1 = client.get(f"/api/auth/status/{h1_uid}")
    assert st_h1.status_code == 200
    assert st_h1.json()["status"] == "Approved"

    # ASSERTION 15: Check status endpoint for Rejected Hospital B coordinator returns Rejected and reason
    st_h2 = client.get(f"/api/auth/status/{h2_uid}")
    assert st_h2.status_code == 200
    assert st_h2.json()["status"] == "Rejected"
    assert st_h2.json()["rejection_reason"] == "Incomplete registration docs"

    # ASSERTION 16: Coordinator 2 (General Hospital) cannot view doctor application for Apollo Hospital (Data Isolation)
    h2_recruitment = client.get("/api/hospital/recruitment", headers=h2_headers)
    assert h2_recruitment.status_code == 200
    assert len(h2_recruitment.json()) == 0

    # ASSERTION 17: Coordinator 2 cannot approve/reject doctor application for Apollo Hospital (Enforce Boundaries)
    h2_approve_doc = client.post(f"/api/hospital/recruitment/{doc_app_id}/status?status=Approved", headers=h2_headers)
    assert h2_approve_doc.status_code == 403

    # ASSERTION 18: Coordinator 1 (Apollo Hospital) can view doctor application for Apollo Hospital
    h1_recruitment = client.get("/api/hospital/recruitment", headers=h1_headers)
    assert h1_recruitment.status_code == 200
    assert any(x["id"] == doc_app_id for x in h1_recruitment.json())

    # ASSERTION 19: Patient cannot approve/reject doctor application
    pat_approve_doc = client.post(f"/api/hospital/recruitment/{doc_app_id}/status?status=Approved", headers=pat_headers)
    assert pat_approve_doc.status_code == 403

    # ASSERTION 20: Super Admin cannot approve/reject doctor applications (Responsibility Boundary check)
    sa_approve_doc = client.post(f"/api/hospital/recruitment/{doc_app_id}/status?status=Approved", headers=sa_headers)
    assert sa_approve_doc.status_code == 403

    # ASSERTION 21: Coordinator 1 approves Doctor application
    h1_approve_doc = client.post(f"/api/hospital/recruitment/{doc_app_id}/status?status=Approved", headers=h1_headers)
    assert h1_approve_doc.status_code == 200
    assert h1_approve_doc.json()["status"] == "Approved"

    # ASSERTION 22: Check Doctor profile was auto-created and matches registration details
    doc_prof_res = client.get(f"/api/doctors/profile/{doc_uid}")
    assert doc_prof_res.status_code == 200
    doc_prof = doc_prof_res.json()
    assert doc_prof["specialization"] == "Pediatrics"
    assert doc_prof["hospital_name"] == unique_hosp_name
    assert doc_prof["mbbs_institution"] == "Harvard Medical School"

    # ASSERTION 23: Check status endpoint for Approved Doctor
    st_doc = client.get(f"/api/auth/status/{doc_uid}")
    assert st_doc.status_code == 200
    assert st_doc.json()["status"] == "Approved"

    # ASSERTION 24: Approve non-existent doctor application -> 404
    h1_approve_fake = client.post("/api/hospital/recruitment/fake_app_99/status?status=Approved", headers=h1_headers)
    assert h1_approve_fake.status_code == 404

    # ASSERTION 25: Approve non-existent hospital application -> 404
    sa_approve_fake = client.post("/api/hospital/fake_hosp_99/status?status=Approved", headers=sa_headers)
    assert sa_approve_fake.status_code == 404


def test_queue_e2e_workflow(client):
    suffix = uuid.uuid4().hex[:6]
    
    # 1. Register Super Admin
    sa_email = f"sa_queue_{suffix}@medislot.test"
    sa_reg = client.post("/api/auth/register", json={
        "email": sa_email,
        "password": "SuperAdminPass123!",
        "full_name": "Queue Super Admin",
        "role": "super_admin"
    })
    assert sa_reg.status_code == 200
    sa_token = sa_reg.json()["access_token"]
    sa_headers = {"Authorization": f"Bearer {sa_token}"}

    # 2. Register Hospital Coordinator
    coord_email = f"coord_queue_{suffix}@medislot.test"
    coord_reg = client.post("/api/auth/register", json={
        "email": coord_email,
        "password": "CoordinatorPass123!",
        "full_name": "Queue Coordinator",
        "role": "hospital"
    })
    assert coord_reg.status_code == 200
    coord_token = coord_reg.json()["access_token"]
    coord_uid = coord_reg.json()["uid"]
    coord_headers = {"Authorization": f"Bearer {coord_token}"}

    # 3. Register Hospital via Coordinator
    hosp_name = f"Queue Test Hospital {suffix}"
    hosp_reg = client.post("/api/hospital/register", headers=coord_headers, json={
        "name": hosp_name,
        "uid": coord_uid,
        "license_number": f"LIC-Q-{suffix}",
        "registration_number": f"REG-Q-{suffix}",
        "address": "456 Hospital Blvd",
        "hospital_type": "General",
        "departments": "Cardiology, Neurology, Pediatrics",
        "contact": "+1 (555) 888-7777",
        "admin_name": "Queue Coordinator"
    })
    assert hosp_reg.status_code == 200
    hosp_id = hosp_reg.json()["id"]

    # 4. Approve Hospital via Super Admin
    appr_res = client.post(f"/api/hospital/{hosp_id}/status?status=Approved", headers=sa_headers)
    assert appr_res.status_code == 200

    # 5. Register Patient 1
    p1_email = f"p1_queue_{suffix}@medislot.test"
    p1_reg = client.post("/api/auth/register", json={
        "email": p1_email,
        "password": "Patient1Pass123!",
        "full_name": "Patient One",
        "role": "patient"
    })
    assert p1_reg.status_code == 200
    p1_token = p1_reg.json()["access_token"]
    p1_uid = p1_reg.json()["uid"]
    p1_headers = {"Authorization": f"Bearer {p1_token}"}

    # Retrieve and seed Patient 1 profile
    p1_prof_res = client.get(f"/api/patients/profile/{p1_uid}", headers=p1_headers)
    assert p1_prof_res.status_code == 200
    p1_pat_id = p1_prof_res.json()["id"]

    # Update Patient 1 Vitals (for enrichment check)
    p1_update = client.put("/api/patients/profile", headers=p1_headers, json={
        "uid": p1_uid,
        "age": 45,
        "gender": "Male",
        "vitals_heart_rate": 88,
        "vitals_bp": "130/85",
        "vitals_spo2": 96,
        "vitals_temperature": 37.2,
        "vitals_blood_sugar": 110
    })
    assert p1_update.status_code == 200

    # 6. Register Patient 2
    p2_email = f"p2_queue_{suffix}@medislot.test"
    p2_reg = client.post("/api/auth/register", json={
        "email": p2_email,
        "password": "Patient2Pass123!",
        "full_name": "Patient Two",
        "role": "patient"
    })
    assert p2_reg.status_code == 200
    p2_token = p2_reg.json()["access_token"]
    p2_uid = p2_reg.json()["uid"]
    p2_headers = {"Authorization": f"Bearer {p2_token}"}

    p2_prof_res = client.get(f"/api/patients/profile/{p2_uid}", headers=p2_headers)
    assert p2_prof_res.status_code == 200
    p2_pat_id = p2_prof_res.json()["id"]

    p2_update = client.put("/api/patients/profile", headers=p2_headers, json={
        "uid": p2_uid,
        "age": 62,
        "gender": "Female",
        "vitals_heart_rate": 105,
        "vitals_bp": "150/95",
        "vitals_spo2": 91,
        "vitals_temperature": 38.4,
        "vitals_blood_sugar": 140
    })
    assert p2_update.status_code == 200

    # 7. Join Queue Validation Rules
    # Rule A: Invalid Hospital -> 404
    bad_hosp = client.post("/api/patients/queue/join", headers=p1_headers, json={
        "patient_id": p1_uid,
        "hospital_id": "nonexistent_hosp",
        "department_id": "Cardiology",
        "symptoms": "Chest pain"
    })
    assert bad_hosp.status_code == 404

    # Rule B: Invalid Department -> 400
    bad_dept = client.post("/api/patients/queue/join", headers=p1_headers, json={
        "patient_id": p1_uid,
        "hospital_id": hosp_id,
        "department_id": "Orthopedics",
        "symptoms": "Leg fracture"
    })
    assert bad_dept.status_code == 400

    # Rule C: Cross-patient impersonation -> 403
    bad_auth = client.post("/api/patients/queue/join", headers=p1_headers, json={
        "patient_id": p2_pat_id,
        "hospital_id": hosp_id,
        "department_id": "Cardiology"
    })
    assert bad_auth.status_code == 403

    # Rule D: Role Boundaries -> Non-patient cannot join -> 403
    coord_join = client.post("/api/patients/queue/join", headers=coord_headers, json={
        "patient_id": p1_uid,
        "hospital_id": hosp_id,
        "department_id": "Cardiology"
    })
    assert coord_join.status_code == 403

    # 8. Successful Join Queue
    p1_join = client.post("/api/patients/queue/join", headers=p1_headers, json={
        "patient_id": p1_uid,
        "hospital_id": hosp_id,
        "department_id": "Cardiology",
        "symptoms": "Chest tightness"
    })
    assert p1_join.status_code == 200
    res1 = p1_join.json()
    assert res1["queue_position"] == 1
    assert res1["queue_status"] == "Active"
    assert res1["status"] == "Active"
    assert res1["symptoms"] == "Chest tightness"
    assert res1["estimated_wait_time"] == 10
    q1_id = res1["id"]

    # Rule E: Prevent duplicate active queue entries
    p1_dup = client.post("/api/patients/queue/join", headers=p1_headers, json={
        "patient_id": p1_pat_id,
        "hospital_id": hosp_id,
        "department_id": "Cardiology"
    })
    assert p1_dup.status_code == 400
    assert "already in an active queue" in p1_dup.json()["detail"]

    # Patient 2 joins Cardiology
    p2_join = client.post("/api/patients/queue/join", headers=p2_headers, json={
        "patient_id": p2_pat_id,
        "hospital_id": hosp_name,
        "department_id": "Cardiology",
        "symptoms": "Difficulty breathing"
    })
    assert p2_join.status_code == 200
    res2 = p2_join.json()
    assert res2["queue_position"] == 2
    assert res2["estimated_wait_time"] == 20
    q2_id = res2["id"]

    # 9. Get Active Queue Endpoint and Ownership
    p1_active = client.get(f"/api/patients/queue/active/{p1_uid}", headers=p1_headers)
    assert p1_active.status_code == 200
    assert p1_active.json()["id"] == q1_id

    p1_spy = client.get(f"/api/patients/queue/active/{p2_pat_id}", headers=p1_headers)
    assert p1_spy.status_code == 403

    # 10. Get Department Queue and Isolation
    list_cardio = client.get(f"/api/patients/queue/list/{hosp_id}/Cardiology", headers=p1_headers)
    assert list_cardio.status_code == 200
    cardio_data = list_cardio.json()
    assert len(cardio_data) == 2
    assert cardio_data[0]["patient_id"] == p1_pat_id
    assert cardio_data[0]["vitals_heart_rate"] == 88
    assert cardio_data[1]["patient_id"] == p2_pat_id
    assert cardio_data[1]["vitals_spo2"] == 91

    list_neuro = client.get(f"/api/patients/queue/list/{hosp_id}/Neurology", headers=p1_headers)
    assert list_neuro.status_code == 403

    coord_list = client.get(f"/api/patients/queue/list/{hosp_id}/Cardiology", headers=coord_headers)
    assert coord_list.status_code == 200
    assert len(coord_list.json()) == 2

    # 11. Update Queue Order Legitimacy and Enforcements
    bad_update1 = client.post("/api/patients/queue/update-order", headers=coord_headers, json={
        "items": [
            {"queue_id": q1_id, "queue_position": 1, "estimated_wait_time": 10},
            {"queue_id": "nonexistent_qid", "queue_position": 2, "estimated_wait_time": 20}
        ]
    })
    assert bad_update1.status_code == 404

    bad_update2 = client.post("/api/patients/queue/update-order", headers=coord_headers, json={
        "items": [
            {"queue_id": q1_id, "queue_position": 1, "estimated_wait_time": 10}
        ]
    })
    assert bad_update2.status_code == 400

    bad_update3 = client.post("/api/patients/queue/update-order", headers=coord_headers, json={
        "items": [
            {"queue_id": q1_id, "queue_position": 1, "estimated_wait_time": 10},
            {"queue_id": q2_id, "queue_position": 3, "estimated_wait_time": 30}
        ]
    })
    assert bad_update3.status_code == 400

    valid_swap = client.post("/api/patients/queue/update-order", headers=coord_headers, json={
        "items": [
            {"queue_id": q2_id, "queue_position": 1, "estimated_wait_time": 10},
            {"queue_id": q1_id, "queue_position": 2, "estimated_wait_time": 20}
        ]
    })
    assert valid_swap.status_code == 200

    # Verify positions swapped
    p2_active_swap = client.get(f"/api/patients/queue/active/{p2_uid}", headers=p2_headers)
    assert p2_active_swap.json()["queue_position"] == 1
    assert p2_active_swap.json()["estimated_wait_time"] == 10

    p1_active_swap = client.get(f"/api/patients/queue/active/{p1_uid}", headers=p1_headers)
    assert p1_active_swap.json()["queue_position"] == 2
    assert p1_active_swap.json()["estimated_wait_time"] == 20

    # 12. Leave Queue & Re-indexing validation
    p1_leave_p2 = client.put(f"/api/patients/queue/leave/{q2_id}", headers=p1_headers)
    assert p1_leave_p2.status_code == 403

    p2_leave = client.put(f"/api/patients/queue/leave/{q2_id}", headers=p2_headers)
    assert p2_leave.status_code == 200

    p2_check = client.get(f"/api/patients/queue/active/{p2_uid}", headers=p2_headers)
    assert p2_check.status_code == 404

    p1_check = client.get(f"/api/patients/queue/active/{p1_uid}", headers=p1_headers)
    assert p1_check.status_code == 200
    assert p1_check.json()["queue_position"] == 1
    assert p1_check.json()["estimated_wait_time"] == 10






