from __future__ import annotations

import uuid
from typing import Optional

from sqlalchemy import select, delete
from sqlalchemy.exc import IntegrityError
from sqlalchemy.ext.asyncio import AsyncSession

from .models import CartLine, Order, OrderItem, Product
from .schemas import (
    CartItemAdd,
    CartLineRead,
    OrderCreate,
    OrderRead,
    OrderUpdate,
    ProductCreate,
    ProductRead,
    ProductUpdate,
)


async def list_products(db: AsyncSession, offset: int = 0, limit: int = 100) -> list[ProductRead]:
    rows = (await db.execute(select(Product).offset(offset).limit(limit))).scalars().all()
    return [
        ProductRead(id=p.id, name=p.name, price_rub=p.price_rub, description=p.description)
        for p in rows
    ]


async def get_product(db: AsyncSession, product_id: str) -> Optional[Product]:
    row = (await db.execute(select(Product).where(Product.id == product_id))).scalar_one_or_none()
    return row


async def create_product(db: AsyncSession, data: ProductCreate) -> ProductRead:
    product = Product(
        id=data.id,
        name=data.name,
        price_rub=data.price_rub,
        description=data.description,
    )
    db.add(product)
    await db.commit()
    await db.refresh(product)
    return ProductRead(
        id=product.id,
        name=product.name,
        price_rub=product.price_rub,
        description=product.description,
    )


async def update_product(db: AsyncSession, product_id: str, data: ProductUpdate) -> ProductRead:
    product = await get_product(db, product_id)
    if product is None:
        raise KeyError("product_not_found")

    product.name = data.name
    product.price_rub = data.price_rub
    product.description = data.description

    await db.commit()
    await db.refresh(product)
    return ProductRead(
        id=product.id,
        name=product.name,
        price_rub=product.price_rub,
        description=product.description,
    )


async def delete_product(db: AsyncSession, product_id: str) -> None:
    product = await get_product(db, product_id)
    if product is None:
        raise KeyError("product_not_found")

    # Если продукт используется в позициях заказов — удаление упадет по FK.
    try:
        await db.delete(product)
        await db.commit()
    except IntegrityError as e:
        await db.rollback()
        raise ValueError("product_in_use") from e


async def get_cart(db: AsyncSession) -> list[CartLineRead]:
    stmt = (
        select(CartLine.product_id, CartLine.quantity, Product.name, Product.price_rub)
        .join(Product, CartLine.product_id == Product.id)
        .order_by(Product.name)
    )
    rows = (await db.execute(stmt)).all()
    out: list[CartLineRead] = []
    for product_id, quantity, name, unit_price in rows:
        line_total = unit_price * quantity
        out.append(
            CartLineRead(
                product_id=product_id,
                name=name,
                quantity=quantity,
                unit_price_rub=unit_price,
                line_total_rub=line_total,
            )
        )
    return out


async def add_to_cart(db: AsyncSession, data: CartItemAdd) -> list[CartLineRead]:
    product = await get_product(db, data.product_id)
    if product is None:
        raise ValueError("product_not_found")

    line = (await db.execute(select(CartLine).where(CartLine.product_id == data.product_id))).scalar_one_or_none()
    if line is None:
        db.add(CartLine(product_id=data.product_id, quantity=data.quantity))
    else:
        line.quantity += data.quantity

    await db.commit()
    return await get_cart(db)


async def clear_cart(db: AsyncSession) -> None:
    await db.execute(delete(CartLine))
    await db.commit()


async def list_orders(db: AsyncSession, offset: int = 0, limit: int = 100) -> list[OrderRead]:
    orders = (
        await db.execute(
            select(Order)
            .order_by(Order.created_at.desc())
            .offset(offset)
            .limit(limit)
        )
    ).scalars().all()
    result: list[OrderRead] = []
    for o in orders:
        result.append(await get_order(db, o.id))
    return result


async def get_order(db: AsyncSession, order_id: uuid.UUID) -> OrderRead:
    order = (await db.execute(select(Order).where(Order.id == order_id))).scalar_one_or_none()
    if order is None:
        raise KeyError("order_not_found")

    items_stmt = (
        select(OrderItem.product_id, OrderItem.quantity, Product.price_rub)
        .join(Product, OrderItem.product_id == Product.id)
        .where(OrderItem.order_id == order_id)
    )
    rows = (await db.execute(items_stmt)).all()

    items = []
    total_rub = 0
    for product_id, quantity, unit_price in rows:
        line_total = unit_price * quantity
        total_rub += line_total
        items.append(
            {
                "product_id": product_id,
                "quantity": quantity,
                "unit_price_rub": unit_price,
                "line_total_rub": line_total,
            }
        )

    return OrderRead(
        id=order.id,
        customer_name=order.customer_name,
        address=order.address,
        status=order.status,  # pydantic проверит что это валидное значение
        items=items,
        total_rub=total_rub,
    )


async def create_order(db: AsyncSession, data: OrderCreate) -> OrderRead:
    order = Order(
        customer_name=data.customer_name,
        address=data.address,
        status=data.status or "created",
    )
    db.add(order)
    await db.flush()  # чтобы order.id появился

    # Проверим товары и создадим позиции.
    product_ids = [it.product_id for it in data.items]
    prod_rows = (await db.execute(select(Product).where(Product.id.in_(product_ids)))).scalars().all()
    products = {p.id: p for p in prod_rows}

    missing = [pid for pid in product_ids if pid not in products]
    if missing:
        raise ValueError(f"products_not_found: {sorted(set(missing))}")

    for it in data.items:
        db.add(
            OrderItem(
                order_id=order.id,
                product_id=it.product_id,
                quantity=it.quantity,
            )
        )

    await db.commit()
    await db.refresh(order)
    return await get_order(db, order.id)


async def update_order(db: AsyncSession, order_id: uuid.UUID, data: OrderUpdate) -> OrderRead:
    order = (await db.execute(select(Order).where(Order.id == order_id))).scalar_one_or_none()
    if order is None:
        raise KeyError("order_not_found")

    if data.customer_name is not None:
        order.customer_name = data.customer_name
    if data.address is not None:
        order.address = data.address
    if data.status is not None:
        order.status = data.status

    if data.items is not None:
        # Режим "заменить позиции целиком": удаляем старые и вставляем новые.
        await db.execute(delete(OrderItem).where(OrderItem.order_id == order_id))

        product_ids = [it.product_id for it in data.items]
        prod_rows = (await db.execute(select(Product).where(Product.id.in_(product_ids)))).scalars().all()
        products = {p.id: p for p in prod_rows}
        missing = [pid for pid in product_ids if pid not in products]
        if missing:
            raise ValueError(f"products_not_found: {sorted(set(missing))}")

        for it in data.items:
            db.add(
                OrderItem(
                    order_id=order_id,
                    product_id=it.product_id,
                    quantity=it.quantity,
                )
            )

    await db.commit()
    return await get_order(db, order_id)


async def delete_order(db: AsyncSession, order_id: uuid.UUID) -> None:
    order = (await db.execute(select(Order).where(Order.id == order_id))).scalar_one_or_none()
    if order is None:
        raise KeyError("order_not_found")

    await db.delete(order)
    await db.commit()

