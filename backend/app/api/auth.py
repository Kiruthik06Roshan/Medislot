from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
import uuid

from ..database.connection import get_db
from ..database.models import UserModel
from ..database.schemas import UserLogin, UserRegister, TokenResponse
from ..utils.security import get_password_hash, verify_password, create_access_token

router = APIRouter(prefix="/api/auth", tags=["Authentication"])

@router.post("/register", response_model=TokenResponse)
async def register(payload: UserRegister, db: AsyncSession = Depends(get_db)):
    # Check if user already exists
    query = select(UserModel).where(UserModel.email == payload.email)
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
        email=payload.email,
        password_hash=hashed_pwd,
        role=payload.role,
        full_name=payload.full_name
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
    query = select(UserModel).where(UserModel.email == payload.email)
    result = await db.execute(query)
    user = result.scalars().first()
    if not user or not verify_password(payload.password, user.password_hash):
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
