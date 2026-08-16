from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from sqlalchemy import func, or_
import uuid

from ..database.connection import get_db
from ..database.models import UserModel, HospitalModel, DoctorApplicationModel
from ..database.schemas import UserLogin, UserRegister, TokenResponse
from ..utils.security import get_password_hash, verify_password, create_access_token

router = APIRouter(prefix="/api/auth", tags=["Authentication"])

@router.post("/register", response_model=TokenResponse)
async def register(payload: UserRegister, db: AsyncSession = Depends(get_db)):
    # Check if user already exists (case-insensitive check)
    clean_email = payload.email.strip().lower()
    query = select(UserModel).where(func.lower(UserModel.email) == clean_email)
    result = await db.execute(query)
    existing_user = result.scalars().first()
    if existing_user:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Email address already registered"
        )

    # Create new user
    uid = str(uuid.uuid4())
    hashed_pwd = get_password_hash(payload.password)
    user = UserModel(
        uid=uid,
        email=payload.email.strip(),
        password_hash=hashed_pwd,
        role=payload.role,
        full_name=payload.full_name.strip()
    )
    db.add(user)
    await db.commit()
    await db.refresh(user)

    # Generate tokens
    access = create_access_token(user.uid, user.role)
    refresh = create_access_token(user.uid, user.role)
    return TokenResponse(
        access_token=access,
        refresh_token=refresh,
        uid=user.uid,
        role=user.role,
        email=user.email
    )

@router.post("/login", response_model=TokenResponse)
async def login(payload: UserLogin, db: AsyncSession = Depends(get_db)):
    identifier = payload.email.strip().lower()
    if not identifier:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid email or password"
        )

    user = None

    # 1. Exact match on email or full_name (case-insensitive)
    exact_query = select(UserModel).where(
        or_(
            func.lower(UserModel.email) == identifier,
            func.lower(UserModel.full_name) == identifier
        )
    )
    result = await db.execute(exact_query)
    exact_candidates = result.scalars().all()
    for candidate in exact_candidates:
        if verify_password(payload.password, candidate.password_hash):
            user = candidate
            break

    # 2. If no exact match candidate succeeded, check email prefix / full_name prefix matches
    if not user:
        prefix_query = select(UserModel).where(
            or_(
                func.lower(UserModel.email).like(f"{identifier}@%"),
                func.lower(UserModel.full_name).like(f"{identifier} %"),
                func.lower(UserModel.full_name).like(f"{identifier}%")
            )
        )
        result = await db.execute(prefix_query)
        prefix_candidates = result.scalars().all()
        for candidate in prefix_candidates:
            if verify_password(payload.password, candidate.password_hash):
                user = candidate
                break

    if not user:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid email or password"
        )

    access = create_access_token(user.uid, user.role)
    refresh = create_access_token(user.uid, user.role)
    return TokenResponse(
        access_token=access,
        refresh_token=refresh,
        uid=user.uid,
        role=user.role,
        email=user.email
    )

@router.post("/refresh", response_model=TokenResponse)
async def refresh_token(refresh_token: str, db: AsyncSession = Depends(get_db)):
    # Simply decode and return new tokens if valid
    from ..utils.security import decode_access_token
    payload = decode_access_token(refresh_token)
    if not payload:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Expired or invalid refresh token"
        )

    uid = payload.get("sub")
    role = payload.get("role")
    
    query = select(UserModel).where(UserModel.uid == uid)
    result = await db.execute(query)
    user = result.scalars().first()
    if not user:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="User not found"
        )

    access = create_access_token(user.uid, user.role)
    refresh = create_access_token(user.uid, user.role)
    return TokenResponse(
        access_token=access,
        refresh_token=refresh,
        uid=user.uid,
        role=user.role,
        email=user.email
    )

@router.get("/status/{uid}")
async def get_user_status(uid: str, db: AsyncSession = Depends(get_db)):
    # Find user
    query = select(UserModel).where(UserModel.uid == uid)
    res = await db.execute(query)
    user = res.scalars().first()
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
        
    if user.role in ["patient", "super_admin"]:
        return {"status": "Approved", "rejection_reason": None, "hospital_name": None}
        
    elif user.role in ["hospital", "hospital_coordinator"]:
        h_query = select(HospitalModel).where(HospitalModel.uid == uid)
        h_res = await db.execute(h_query)
        hospital = h_res.scalars().first()
        if not hospital:
            return {"status": "Pending", "rejection_reason": None, "hospital_name": None}
        return {
            "status": hospital.status,
            "rejection_reason": hospital.rejection_reason,
            "hospital_name": hospital.name
        }
        
    elif user.role == "doctor":
        # Find doctor application by uid
        app_query = select(DoctorApplicationModel).where(DoctorApplicationModel.uid == uid)
        app_res = await db.execute(app_query)
        app = app_res.scalars().first()
        if not app:
            # Fallback by matching full name
            app_query2 = select(DoctorApplicationModel).where(DoctorApplicationModel.name == user.full_name)
            app_res2 = await db.execute(app_query2)
            app = app_res2.scalars().first()
            if not app:
                return {"status": "Pending", "rejection_reason": None, "hospital_name": None}
        return {
            "status": app.status,
            "rejection_reason": app.rejection_reason,
            "hospital_name": app.selected_hospital
        }
        
    return {"status": "Pending", "rejection_reason": None, "hospital_name": None}
