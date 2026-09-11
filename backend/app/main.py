from contextlib import asynccontextmanager
from datetime import UTC, datetime
from uuid import UUID
from fastapi import Depends, FastAPI, Header, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession
from .config import get_settings
from .database import engine, session
from .models import Base, HunterProfile, RefreshToken, SyncEvent, User
from .schemas import LoginRequest, Profile, RefreshRequest, RegisterRequest, SyncPush, SyncResult, TokenPair
from .security import decode_token, hash_password, issue_token, token_hash, verify_password

settings = get_settings()
bearer = HTTPBearer(auto_error=False)

@asynccontextmanager
async def lifespan(_: FastAPI):
    if settings.environment == "development":
        async with engine.begin() as conn: await conn.run_sync(Base.metadata.create_all)
    yield
    await engine.dispose()

app = FastAPI(title="Hunter System API", version="1.0.0", lifespan=lifespan, docs_url=None if settings.environment == "production" else "/docs")
if settings.allowed_origins:
    app.add_middleware(CORSMiddleware, allow_origins=settings.allowed_origins, allow_credentials=False, allow_methods=["GET","POST","PATCH","DELETE"], allow_headers=["Authorization","Content-Type","Idempotency-Key"])

def pair(user_id: UUID) -> tuple[TokenPair, RefreshToken]:
    access, access_exp, _ = issue_token(user_id, "access")
    refresh, refresh_exp, refresh_id = issue_token(user_id, "refresh")
    row = RefreshToken(id=refresh_id, user_id=user_id, digest=token_hash(refresh), expires_at=refresh_exp)
    return TokenPair(access_token=access, refresh_token=refresh, access_expires_at=access_exp, refresh_expires_at=refresh_exp), row

async def current_user(credentials: HTTPAuthorizationCredentials | None = Depends(bearer), db: AsyncSession = Depends(session)) -> User:
    if not credentials: raise HTTPException(status_code=401, detail="Authentication required")
    payload = decode_token(credentials.credentials, "access")
    user = await db.get(User, UUID(payload["sub"]))
    if not user or user.state != "active": raise HTTPException(status_code=401, detail="Account unavailable")
    return user

@app.get("/health/live")
async def live(): return {"status":"ok"}
@app.get("/health/ready")
async def ready(db: AsyncSession = Depends(session)):
    await db.execute(select(1)); return {"status":"ready"}

@app.post("/api/v1/auth/register", response_model=TokenPair, status_code=201)
async def register(body: RegisterRequest, db: AsyncSession = Depends(session)):
    email = body.email.lower()
    if await db.scalar(select(User).where(User.email == email)): raise HTTPException(status_code=409, detail="Email already registered")
    user = User(email=email, password_hash=hash_password(body.password), display_name=body.display_name)
    db.add(user); await db.flush(); db.add(HunterProfile(user_id=user.id, hunter_name=body.hunter_name))
    result, refresh = pair(user.id); db.add(refresh); return result

@app.post("/api/v1/auth/login", response_model=TokenPair)
async def login(body: LoginRequest, db: AsyncSession = Depends(session)):
    user = await db.scalar(select(User).where(User.email == body.email.lower()))
    if not user or not verify_password(body.password, user.password_hash): raise HTTPException(status_code=401, detail="Invalid credentials")
    result, refresh = pair(user.id); db.add(refresh); return result

@app.post("/api/v1/auth/refresh", response_model=TokenPair)
async def refresh(body: RefreshRequest, db: AsyncSession = Depends(session)):
    payload = decode_token(body.refresh_token, "refresh")
    row = await db.get(RefreshToken, UUID(payload["jti"]))
    if not row or row.revoked_at or row.digest != token_hash(body.refresh_token) or row.expires_at <= datetime.now(UTC): raise HTTPException(status_code=401, detail="Refresh token unavailable")
    row.revoked_at = datetime.now(UTC)
    result, replacement = pair(UUID(payload["sub"])); db.add(replacement); return result

@app.get("/api/v1/profile", response_model=Profile)
async def profile(user: User = Depends(current_user), db: AsyncSession = Depends(session)):
    value = await db.scalar(select(HunterProfile).where(HunterProfile.user_id == user.id))
    if not value: raise HTTPException(status_code=404, detail="Profile not found")
    return value

@app.post("/api/v1/sync/push", response_model=SyncResult)
async def sync_push(body: SyncPush, idempotency_key: UUID = Header(alias="Idempotency-Key"), user: User = Depends(current_user), db: AsyncSession = Depends(session)):
    accepted, duplicates = [], []
    for event in body.events:
        if await db.get(SyncEvent, event.id): duplicates.append(event.id); continue
        db.add(SyncEvent(id=event.id, user_id=user.id, device_id=body.device_id, aggregate_type=event.aggregate_type, aggregate_id=event.aggregate_id, sequence=event.sequence, occurred_at=event.occurred_at, payload=event.payload)); accepted.append(event.id)
    return SyncResult(accepted=accepted, duplicates=duplicates)

@app.get("/api/v1/sync/pull")
async def sync_pull(cursor: datetime | None = None, limit: int = 100, user: User = Depends(current_user), db: AsyncSession = Depends(session)):
    limit = max(1, min(limit, 100)); query = select(SyncEvent).where(SyncEvent.user_id == user.id)
    if cursor: query = query.where(SyncEvent.received_at > cursor)
    rows = (await db.scalars(query.order_by(SyncEvent.received_at, SyncEvent.id).limit(limit))).all()
    return {"events":[{"id":str(r.id),"aggregate_type":r.aggregate_type,"aggregate_id":str(r.aggregate_id),"sequence":r.sequence,"occurred_at":r.occurred_at,"payload":r.payload,"received_at":r.received_at} for r in rows], "next_cursor": rows[-1].received_at if rows else cursor}
