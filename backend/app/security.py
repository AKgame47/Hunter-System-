from datetime import UTC, datetime, timedelta
from hashlib import sha256
from uuid import UUID, uuid4
import jwt
from fastapi import HTTPException, status
from pwdlib import PasswordHash
from .config import get_settings

_passwords = PasswordHash.recommended()
settings = get_settings()

def hash_password(password: str) -> str:
    return _passwords.hash(password)

def verify_password(password: str, encoded: str) -> bool:
    return _passwords.verify(password, encoded)

def token_hash(token: str) -> str:
    return sha256(token.encode()).hexdigest()

def issue_token(user_id: UUID, kind: str) -> tuple[str, datetime, UUID]:
    now = datetime.now(UTC)
    expires = now + (timedelta(minutes=settings.access_token_minutes) if kind == "access" else timedelta(days=settings.refresh_token_days))
    token_id = uuid4()
    payload = {"sub": str(user_id), "typ": kind, "jti": str(token_id), "iss": settings.jwt_issuer, "iat": now, "exp": expires}
    return jwt.encode(payload, settings.jwt_secret, algorithm="HS256"), expires, token_id

def decode_token(token: str, kind: str) -> dict:
    try:
        payload = jwt.decode(token, settings.jwt_secret, algorithms=["HS256"], issuer=settings.jwt_issuer)
        if payload.get("typ") != kind:
            raise ValueError("wrong token type")
        UUID(payload["sub"]); UUID(payload["jti"])
        return payload
    except Exception as exc:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid or expired token") from exc
