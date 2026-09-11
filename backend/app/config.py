from functools import lru_cache
from pydantic import Field, field_validator
from pydantic_settings import BaseSettings, SettingsConfigDict

class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", extra="ignore")
    environment: str = "development"
    database_url: str
    jwt_secret: str = Field(min_length=32)
    jwt_issuer: str = "hunter-system"
    access_token_minutes: int = Field(default=15, ge=5, le=60)
    refresh_token_days: int = Field(default=30, ge=1, le=90)
    cors_origins: str = ""
    log_level: str = "INFO"
    @property
    def allowed_origins(self) -> list[str]:
        return [v.strip() for v in self.cors_origins.split(",") if v.strip()]
    @field_validator("environment")
    @classmethod
    def validate_environment(cls, value: str) -> str:
        if value not in {"development", "staging", "production"}:
            raise ValueError("invalid environment")
        return value

@lru_cache
def get_settings() -> Settings:
    return Settings()
