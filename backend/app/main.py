from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from .database.connection import engine, Base
from .api import auth, patients, doctors, appointments, hospital, ai

app = FastAPI(
    title="MediSlot Hospital Management System API Gateway",
    description="Production-grade asynchronous endpoints for appointment scheduling, patient records, and resources allocation.",
    version="1.0.0"
)

import os

allowed_origins_env = os.getenv("ALLOWED_ORIGINS", "*")
if allowed_origins_env == "*":
    origins = ["*"]
else:
    origins = [origin.strip() for origin in allowed_origins_env.split(",") if origin.strip()]

# CORS Policy
app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

from sqlalchemy import text

@app.on_event("startup")
async def startup_event():
    # Asynchronously initialize PostgreSQL tables on gateway bootstrap
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)
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
            "ALTER TABLE operational_alerts ADD COLUMN IF NOT EXISTS hospital_name VARCHAR;",
            "ALTER TABLE patient_queues ADD COLUMN IF NOT EXISTS status VARCHAR;",
            "ALTER TABLE patient_queues ADD COLUMN IF NOT EXISTS joined_at TIMESTAMP;"
        ]
        for q in alter_queries:
            try:
                await conn.execute(text(q))
            except Exception:
                pass

    # Seed deterministic demo data if needed
    from .database.connection import AsyncSessionLocal
    from .database.seeding import seed_data
    async with AsyncSessionLocal() as session:
        await seed_data(session)

# Register Sub-routers
app.include_router(auth.router)
app.include_router(patients.router)
app.include_router(doctors.router)
app.include_router(appointments.router)
app.include_router(hospital.router)
app.include_router(ai.router)

@app.get("/")
async def root():
    return {
        "status": "healthy",
        "service": "MediSlot API Gateway",
        "timestamp": "2026-08-07T23:00:00Z"
    }
