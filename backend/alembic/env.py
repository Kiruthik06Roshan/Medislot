import sys
import os
from dotenv import load_dotenv
load_dotenv()

# Append backend root path to sys.path
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))

from logging.config import fileConfig

from sqlalchemy import engine_from_config
from sqlalchemy import pool

from alembic import context

# this is the Alembic Config object, which provides
# access to the values within the .ini file in use.
config = context.config

# Dynamically set the sqlalchemy.url from the env file
db_url = os.getenv("DATABASE_URL")
if db_url:
    # Alembic requires a synchronous driver/engine connection for migrations.
    # If using postgresql+asyncpg, replace it with postgresql (psycopg2) or similar if needed,
    # or let's see if asyncpg can be used by modifying engine creation or just using it directly.
    # Actually, SQLAlchemy 2.0 engine_from_config supports async/sync depending on how connectable is created.
    # Wait, Alembic runs synchronously, so it needs a synchronous connection string!
    # If the URL is postgresql+asyncpg, we can replace it with postgresql+psycopg2 (or just postgresql)
    # for Alembic migrations to run synchronously.
    # Let's check: does python have psycopg2 or psycopg installed?
    # No, we only installed asyncpg. But SQLAlchemy has built-in pg8000 or asyncpg adapter?
    # Wait! Can we create an async connection or run alembic synchronously?
    # Actually, since we only have asyncpg, let's see: we can run migrations using the asyncpg driver,
    # but we must configure env.py to use an async engine!
    # Wait, SQLAlchemy 2.0 + Alembic supports async engine migrations. Let's see if we can convert env.py to run async!
    # Or, does the local postgresql support simple synchronous connections via `postgresql://` (using psycopg2 or similar)?
    # Wait, we don't have psycopg2 installed. If we change postgresql+asyncpg to postgresql, it will use psycopg2 which will throw ModuleNotFoundError!
    # So we MUST run migrations asynchronously using asyncpg!
    # Let's verify how to run Alembic migrations with async engine. It's very simple:
    # We can write an async run_migrations_online function using `asyncio`.
    # Let's write the async env.py configuration. It's extremely robust and standard.
    config.set_main_option("sqlalchemy.url", db_url)

# Interpret the config file for Python logging.
# This line sets up loggers basically.
if config.config_file_name is not None:
    fileConfig(config.config_file_name)

# add your model's MetaData object here
# for 'autogenerate' support
from app.database.models import Base
target_metadata = Base.metadata

# other values from the config, defined by the needs of env.py,
# can be acquired:
# my_important_option = config.get_main_option("my_important_option")
# ... etc.


def run_migrations_offline() -> None:
    """Run migrations in 'offline' mode.

    This configures the context with just a URL
    and not an Engine, though an Engine is acceptable
    here as well.  By skipping the Engine creation
    we don't even need a DBAPI to be available.

    Calls to context.execute() here emit the given string to the
    script output.

    """
    url = config.get_main_option("sqlalchemy.url")
    context.configure(
        url=url,
        target_metadata=target_metadata,
        literal_binds=True,
        dialect_opts={"paramstyle": "named"},
    )

    with context.begin_transaction():
        context.run_migrations()


def do_run_migrations(connection) -> None:
    context.configure(connection=connection, target_metadata=target_metadata)
    with context.begin_transaction():
        context.run_migrations()


async def run_migrations_online() -> None:
    """Run migrations in 'online' mode using async connection."""
    from sqlalchemy.ext.asyncio import async_engine_from_config

    connectable = async_engine_from_config(
        config.get_section(config.config_ini_section, {}),
        prefix="sqlalchemy.",
        poolclass=pool.NullPool,
    )

    async with connectable.connect() as connection:
        await connection.run_sync(do_run_migrations)

    await connectable.dispose()


if context.is_offline_mode():
    run_migrations_offline()
else:
    import asyncio
    asyncio.run(run_migrations_online())
