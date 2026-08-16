import os
import asyncio
import asyncpg
import pytest

# Force DATABASE_URL to test database before importing FastAPI app
os.environ["DATABASE_URL"] = "postgresql+asyncpg://postgres:Post123@localhost:5432/medislot_test"

async def async_setup_test_db():
    conn = await asyncpg.connect("postgresql://postgres:Post123@localhost:5432/postgres")
    try:
        # Terminate active connections to medislot_test
        await conn.execute("""
            SELECT pg_terminate_backend(pg_stat_activity.pid)
            FROM pg_stat_activity
            WHERE pg_stat_activity.datname = 'medislot_test'
              AND pid <> pg_backend_pid();
        """)
        # Drop database if exists
        await conn.execute("DROP DATABASE IF EXISTS medislot_test")
        # Create database
        await conn.execute("CREATE DATABASE medislot_test")
    except Exception as e:
        print(f"\n[Test Database Setup Error]: {e}", flush=True)
    finally:
        await conn.close()

def pytest_sessionstart(session):
    print("\n--- Setting up isolated test database (medislot_test) ---")
    asyncio.run(async_setup_test_db())
    print("--- Test database setup complete ---")
