# Практическая работа 4

Android-приложение доставки продуктов с backend на FastAPI/PostgreSQL.

## Что реализовано в ПР4

- **Карта (OpenStreetMap / osmdroid)** на экране выбора адреса.
- **Метки с описанием** на карте (пункты выдачи и склад).
- **Выбор адреса тапом по карте** и сохранение выбранной точки в БД через API.
- **Внешнее API**: тайлы OpenStreetMap загружаются из сети и отображаются в приложении.
- Выбранный адрес показывается в корзине и используется при оформлении заказа.

## Структура проекта

- `app/` — Android-приложение (Java, XML, osmdroid, OkHttp).
- `backend/` — FastAPI + PostgreSQL (товары, корзина, заказы, адрес доставки).

## Backend API

После запуска backend Swagger доступен по адресу: `http://localhost:8000/docs`.

Основные эндпоинты:
- `GET /api/products` — список товаров.
- `GET/POST /api/cart`, `DELETE /api/cart` — корзина.
- `POST /api/orders` — оформление заказа.
- `GET/PUT /api/address` — получить/сохранить выбранный адрес доставки.


## Запуск backend в Docker

```bash
cd /Users/fedor/AndroidStudioProjects/mobile_course/backend
docker compose up -d --build
```

Остановка:

```bash
docker compose down
```

Если есть ошибка `InvalidPasswordError`:

```bash
docker compose down -v
docker compose up -d --build
```

## Сборка APK из командной строки

См. `BUILD_APK.md`.

