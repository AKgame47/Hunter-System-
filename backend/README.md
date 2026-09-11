# Hunter System backend

Production API foundation using FastAPI, PostgreSQL, rotating refresh tokens, Argon2 password hashing, idempotent event sync, container health checks, and Apache TLS termination.

## Local verification
1. Copy `.env.example` to `.env` and replace all credentials.
2. Create `deployment/secrets/postgres_password.txt` outside version control.
3. Apply `backend/migrations/001_initial.sql` to PostgreSQL.
4. Run the API container behind Apache.

Automatic table creation is development-only. Never commit `.env`, signing keys, provider keys, database dumps, or TLS private keys.
