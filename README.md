# Практическая работа 3

Проект: Android-приложение доставки продуктов + backend API.


## Структура репозитория

- `app/` — Android приложение
- `backend/` — FastAPI + PostgreSQL (CRUD API)

## Backend (FastAPI + PostgreSQL)

### Что реализовано

- **Products CRUD**
- **Cart**: корзина в PostgreSQL (`GET/POST /api/cart`, очистка перед новым заказом)
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

Если Postgres был запущен раньше и FastAPI падает с `InvalidPasswordError`, нужно сбросить volume и поднять заново:

```bash
docker compose down -v
docker compose up -d --build
```


## Android: где смотреть код

- `app/src/main/java/com/example/lab2/` — `Activity`
- `app/src/main/java/com/example/lab2/data/DeliveryRepository.java` — HTTP‑клиент к API (товары, корзина, заказ)
- `app/build.gradle.kts` — `buildConfigField API_BASE_URL` (по умолчанию `http://10.0.2.2:8000` для эмулятора; на телефоне укажи IP ПК в локальной сети)
- `app/src/main/java/com/example/lab2/ui/` — адаптеры RecyclerView
- `app/src/main/res/layout/` — XML‑макеты

## Сборка APK из командной строки

Инструкция: `BUILD_APK.md`.

