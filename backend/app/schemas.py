from datetime import datetime
from uuid import UUID
from pydantic import BaseModel, ConfigDict, EmailStr, Field

class RegisterRequest(BaseModel):
    email: EmailStr
    password: str = Field(min_length=12, max_length=128)
    display_name: str = Field(min_length=1, max_length=80)
    hunter_name: str = Field(min_length=1, max_length=40)
class LoginRequest(BaseModel):
    email: EmailStr
    password: str
class RefreshRequest(BaseModel): refresh_token: str
class TokenPair(BaseModel):
    access_token: str
    refresh_token: str
    token_type: str = "bearer"
    access_expires_at: datetime
    refresh_expires_at: datetime
class Profile(BaseModel):
    model_config = ConfigDict(from_attributes=True)
    id: UUID
    hunter_name: str
    level: int
    rank: str
    xp: int
    streak: int
    version: int
class SyncEventIn(BaseModel):
    id: UUID
    aggregate_type: str = Field(max_length=40)
    aggregate_id: UUID
    sequence: int = Field(ge=1)
    occurred_at: datetime
    payload: dict
class SyncPush(BaseModel):
    device_id: UUID
    events: list[SyncEventIn] = Field(max_length=100)
class SyncResult(BaseModel):
    accepted: list[UUID]
    duplicates: list[UUID]
