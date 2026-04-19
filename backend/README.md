# Backend на FastAPI (CRUD + PostgreSQL)


Сервис содержит CRUD-логику для сущностей в контексте приложения доставки продуктов

База данных: PostgreSQL.

## Инструкция по запуску

### Вариант A: запуск в Docker (PostgreSQL + FastAPI)

```bash
cd backend
docker compose up -d --build
```

API будет доступен здесь:
- `http://localhost:8000/docs`

### Если FastAPI падает с `InvalidPasswordError`

Это обычно бывает, когда PostgreSQL-контейнер уже запускался раньше и данные лежат в volume (пароль пользователя `app` мог не совпасть).

Сбросить БД и поднять заново:

```bash
cd backend
docker compose down -v
docker compose up -d --build
```

### Вариант B: запуск локально (без Docker)

```bash
cd backend
python3 -m venv venv
source venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8000
```

Если БД не в Docker, то нужно задать `DATABASE_URL` (см. `backend/.env.example`).

