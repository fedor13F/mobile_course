# Backend на FastAPI (CRUD + PostgreSQL)

## Что реализовано

Сервис содержит CRUD-логику для сущностей в контексте приложения доставки продуктов:

- `Product`: товары (создать/получить/обновить/удалить)
- `Order`: заказ (создать заказ с позициями, получить, обновить, удалить)
- `OrderItem`: позиции заказа (добавляются/заменяются в составе `Order`)

База данных: PostgreSQL.

## Быстрый старт

### Вариант A: запуск в Docker (PostgreSQL + FastAPI)

```bash
cd /Users/fedor/AndroidStudioProjects/mobile_course/backend
docker compose up -d --build
```

API будет доступен тут:
- `http://localhost:8000/docs`

### Если FastAPI падает с `InvalidPasswordError`

Это обычно бывает, когда PostgreSQL-контейнер уже запускался раньше и данные лежат в volume (пароль пользователя `app` мог не совпасть).

Сбросить БД и поднять заново:

```bash
cd /Users/fedor/AndroidStudioProjects/mobile_course/backend
docker compose down -v
docker compose up -d --build
```

### Вариант B: запуск локально (без Docker)

```bash
cd /Users/fedor/AndroidStudioProjects/mobile_course/backend
python3 -m venv venv
source venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8000
```

Если БД не в Docker, то нужно задать `DATABASE_URL` (см. `backend/.env.example`).

## Проверка CRUD через `curl`

### Добавить товар

```bash
curl -X POST http://localhost:8000/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "id": "milk_1l",
    "name": "Молоко 1л",
    "price_rub": 100,
    "description": "Пример товара"
  }'
```

### Получить список товаров

```bash
curl http://localhost:8000/api/products
```

### Создать заказ (с позициями)

```bash
curl -X POST http://localhost:8000/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customer_name": "Иван",
    "address": "Москва, ул. Примерная, 1",
    "items": [
      { "product_id": "milk_1l", "quantity": 2 }
    ]
  }'
```

### Получить заказ

```bash
curl http://localhost:8000/api/orders/<ORDER_ID>
```

## Как это “подключить” к UI

В твоём Android-приложении сейчас корзина и товары хранятся локально (статические данные `DataStore`).
Для ПР3 обычно нужно заменить локальную логику на запросы к API.

Если хочешь, я также могу подсказать, какие запросы (эндпоинты + схемы) нужно вызвать из экранов `MainActivity`, `ProductActivity` и `CartActivity`.

