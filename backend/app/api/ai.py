from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
import datetime

from ..database.connection import get_db
from ..database.models import AiRequestLogModel, AiCacheModel
from ..database.schemas import AiLogRequest

router = APIRouter(prefix="/api/ai", tags=["AI Integration Proxy"])

@router.post("/log")
async def log_ai_request(payload: AiLogRequest, db: AsyncSession = Depends(get_db)):
    log = AiRequestLogModel(
        prompt_type=payload.prompt_type,
        prompt=payload.prompt,
        response=payload.response,
        latency_ms=payload.latency_ms,
        model_used=payload.model_used,
        was_cached=payload.was_cached
    )
    db.add(log)
    await db.commit()
    return {"status": "success", "message": "AI transaction logged successfully"}

@router.get("/cache/{cache_key}")
async def get_ai_cache(cache_key: str, db: AsyncSession = Depends(get_db)):
    query = select(AiCacheModel).where(AiCacheModel.cache_key == cache_key)
    result = await db.execute(query)
    cache = result.scalars().first()
    if not cache:
        raise HTTPException(status_code=404, detail="Cache miss")
    return {"cache_key": cache.cache_key, "response_data": cache.response_data, "timestamp": cache.timestamp}

@router.post("/cache")
async def save_ai_cache(cache_key: str, response_data: str, db: AsyncSession = Depends(get_db)):
    # Check if cache key exists
    query = select(AiCacheModel).where(AiCacheModel.cache_key == cache_key)
    result = await db.execute(query)
    cache = result.scalars().first()
    if not cache:
        cache = AiCacheModel(cache_key=cache_key)
        db.add(cache)
    cache.response_data = response_data
    cache.timestamp = datetime.datetime.utcnow()
    await db.commit()
    return {"status": "success", "message": "Cache saved successfully"}
