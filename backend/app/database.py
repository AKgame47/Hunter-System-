from collections.abc import AsyncIterator
from sqlalchemy.ext.asyncio import AsyncSession, async_sessionmaker, create_async_engine
from .config import get_settings

settings = get_settings()
engine = create_async_engine(settings.database_url, pool_pre_ping=True, pool_size=10, max_overflow=20)
SessionFactory = async_sessionmaker(engine, expire_on_commit=False)

async def session() -> AsyncIterator[AsyncSession]:
    async with SessionFactory() as db:
        try:
            yield db
            await db.commit()
        except Exception:
            await db.rollback()
            raise
