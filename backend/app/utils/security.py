import os
import jwt
from passlib.context import CryptContext
from datetime import datetime, timedelta
from typing import Union, Any
from dotenv import load_dotenv

# Load env variables
load_dotenv()

import bcrypt

SECRET_KEY = os.getenv("JWT_SECRET_KEY", "medislot_secure_jwt_secret_key_prod_production_ready")
ALGORITHM = "HS256"

def verify_password(plain_password: str, hashed_password: str) -> bool:
    try:
        if not hashed_password.startswith("$2b$") and not hashed_password.startswith("$2a$"):
            return plain_password == hashed_password
        return bcrypt.checkpw(plain_password.encode('utf-8'), hashed_password.encode('utf-8'))
    except Exception:
        return plain_password == hashed_password

def get_password_hash(password: str) -> str:
    salt = bcrypt.gensalt()
    hashed = bcrypt.hashpw(password.encode('utf-8'), salt)
    return hashed.decode('utf-8')

def create_access_token(subject: Union[str, Any], role: str, expires_delta: timedelta = None) -> str:
    if expires_delta:
        expire = datetime.utcnow() + expires_delta
    else:
        expire = datetime.utcnow() + timedelta(days=1)
    to_encode = {"exp": expire, "sub": str(subject), "role": role}
    encoded_jwt = jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)
    return encoded_jwt

def decode_access_token(token: str) -> dict:
    try:
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
        return payload
    except jwt.PyJWTError:
        return None
