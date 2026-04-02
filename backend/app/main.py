import os
import uuid

from fastapi import Depends, FastAPI, HTTPException
from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncSession

from .crud import (
    create_order,
    create_product,
    delete_order,
    delete_product,
    get_order,
    get_product,
    list_orders,
    list_products,
    update_order,
    update_product,
)
from .db import AsyncSessionLocal, engine, get_db
from .models import Base, Product
from .schemas import (
    OrderCreate,
    OrderRead,
    OrderUpdate,
    ProductCreate,
    ProductRead,
    ProductUpdate,
)

app = FastAPI(title="Mobile Course - Delivery API")


@app.on_event("startup")
async def on_startup():
    # 1) создаем таблицы
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)

    # 2) опциональная “засыпка” демо-товаров (чтобы можно было сразу тестировать CRUD)
    seed_enabled = os.getenv("SEED_DEMO_DATA", "true").lower() in {"1", "true", "yes", "y"}
    if not seed_enabled:
        return

    async with AsyncSessionLocal() as session:
        count = (await session.execute(select(func.count(Product.id)))).scalar_one()
        if count and count > 0:
            return

        demo_products = [
            Product(id="milk_1l", name="Молоко 1л", price_rub=100, description="Здесь дополнительное описание товара"),
            Product(id="kefir_1l", name="Кефир 1л", price_rub=129, description="Здесь дополнительное описание товара"),
            Product(id="bread", name="Хлеб", price_rub=55, description="Здесь дополнительное описание товара"),
            Product(id="eggs", name="Яйца 10шт", price_rub=140, description="Здесь дополнительное описание товара"),
            Product(id="cheese", name="Сыр 200г", price_rub=260, description="Здесь дополнительное описание товара"),
            Product(id="apples", name="Яблоки 1кг", price_rub=120, description="Здесь дополнительное описание товара"),
        ]
        session.add_all(demo_products)
        await session.commit()


@app.get("/api/products", response_model=list[ProductRead])
async def api_list_products(
    offset: int = 0,
    limit: int = 100,
    db: AsyncSession = Depends(get_db),
):
    return await list_products(db, offset=offset, limit=limit)


@app.post("/api/products", response_model=ProductRead, status_code=201)
async def api_create_product(product_in: ProductCreate, db: AsyncSession = Depends(get_db)):
    try:
        return await create_product(db, product_in)
    except Exception as e:
        # Например, конфликт по PK (`id`) — возвращаем понятную ошибку.
        raise HTTPException(status_code=400, detail=str(e))


@app.get("/api/products/{product_id}", response_model=ProductRead)
async def api_get_product(product_id: str, db: AsyncSession = Depends(get_db)):
    product = await get_product(db, product_id)
    if product is None:
        raise HTTPException(status_code=404, detail="product_not_found")
    return ProductRead(id=product.id, name=product.name, price_rub=product.price_rub, description=product.description)


@app.put("/api/products/{product_id}", response_model=ProductRead)
async def api_update_product(product_id: str, product_in: ProductUpdate, db: AsyncSession = Depends(get_db)):
    try:
        return await update_product(db, product_id, data=product_in)
    except KeyError:
        raise HTTPException(status_code=404, detail="product_not_found")
    except Exception as e:
        raise HTTPException(status_code=400, detail=str(e))


@app.delete("/api/products/{product_id}", status_code=204)
async def api_delete_product(product_id: str, db: AsyncSession = Depends(get_db)):
    try:
        await delete_product(db, product_id)
    except KeyError:
        raise HTTPException(status_code=404, detail="product_not_found")
    except ValueError:
        raise HTTPException(status_code=409, detail="product_in_use")
    return None


@app.get("/api/orders", response_model=list[OrderRead])
async def api_list_orders(
    offset: int = 0,
    limit: int = 100,
    db: AsyncSession = Depends(get_db),
):
    return await list_orders(db, offset=offset, limit=limit)


@app.post("/api/orders", response_model=OrderRead, status_code=201)
async def api_create_order(order_in: OrderCreate, db: AsyncSession = Depends(get_db)):
    try:
        return await create_order(db, order_in)
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))


@app.get("/api/orders/{order_id}", response_model=OrderRead)
async def api_get_order(order_id: uuid.UUID, db: AsyncSession = Depends(get_db)):
    try:
        return await get_order(db, order_id)
    except KeyError:
        raise HTTPException(status_code=404, detail="order_not_found")


@app.put("/api/orders/{order_id}", response_model=OrderRead)
async def api_update_order(order_id: uuid.UUID, order_in: OrderUpdate, db: AsyncSession = Depends(get_db)):
    try:
        return await update_order(db, order_id, order_in)
    except KeyError:
        raise HTTPException(status_code=404, detail="order_not_found")
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))


@app.delete("/api/orders/{order_id}", status_code=204)
async def api_delete_order(order_id: uuid.UUID, db: AsyncSession = Depends(get_db)):
    try:
        await delete_order(db, order_id)
    except KeyError:
        raise HTTPException(status_code=404, detail="order_not_found")
    return None

