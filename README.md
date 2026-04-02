# Практическая работа 3 — локальная БД / CRUD (Delivery app)

Проект: Android-приложение доставки продуктов + backend API.

В рамках ПР3 добавлена “серверная часть” на **FastAPI** с **PostgreSQL** и реализованы **CRUD-операции** в домене приложения (товары/заказы).

## Структура репозитория

- `app/` — Android приложение (Java, UI как в ПР2)
- `backend/` — FastAPI + PostgreSQL (CRUD API)

## Backend (FastAPI + PostgreSQL)

### Что реализовано

- **Products CRUD**: создать/получить/обновить/удалить товар
- **Orders CRUD**: создать/получить/обновить/удалить заказ (заказ содержит позиции `items`)

Эндпоинты доступны в Swagger UI после запуска:
- `http://localhost:8000/docs`

### Запуск backend в Docker

```bash
cd /Users/fedor/AndroidStudioProjects/mobile_course/backend
docker compose up -d --build
```

Остановка:

```bash
docker compose down
```

Если Postgres был запущен раньше и FastAPI падает с `InvalidPasswordError`, сбрось volume и подними заново:

```bash
docker compose down -v
docker compose up -d --build
```

Подробности и примеры запросов: `backend/README.md`.

## Android: где смотреть код

- `app/src/main/java/com/example/lab2/` — `Activity`
- `app/src/main/java/com/example/lab2/data/DataStore.java` — текущие статические данные (как в ПР2)
- `app/src/main/java/com/example/lab2/ui/` — адаптеры RecyclerView
- `app/src/main/res/layout/` — XML‑макеты

## Сборка APK из командной строки

Инструкция: `BUILD_APK.md`.

