from typing import Literal, Optional
from uuid import UUID

from pydantic import BaseModel, Field

OrderStatus = Literal["created", "confirmed", "prepared", "delivered", "cancelled"]


class ProductCreate(BaseModel):
    id: str = Field(min_length=1, max_length=128)
    name: str = Field(min_length=1, max_length=256)
    price_rub: int = Field(ge=0)
    description: Optional[str] = None


class ProductUpdate(BaseModel):
    name: str = Field(min_length=1, max_length=256)
    price_rub: int = Field(ge=0)
    description: Optional[str] = None


class ProductRead(BaseModel):
    id: str
    name: str
    price_rub: int
    description: Optional[str] = None


class OrderItemIn(BaseModel):
    product_id: str = Field(min_length=1, max_length=128)
    quantity: int = Field(ge=1, le=10_000)


class OrderCreate(BaseModel):
    customer_name: str = Field(min_length=1, max_length=256)
    address: str = Field(min_length=1, max_length=2048)
    items: list[OrderItemIn]
    status: Optional[OrderStatus] = None


class OrderUpdate(BaseModel):
    customer_name: Optional[str] = Field(default=None, min_length=1, max_length=256)
    address: Optional[str] = Field(default=None, min_length=1, max_length=2048)
    status: Optional[OrderStatus] = None
    items: Optional[list[OrderItemIn]] = None


class OrderItemRead(BaseModel):
    product_id: str
    quantity: int
    unit_price_rub: int
    line_total_rub: int


class OrderRead(BaseModel):
    id: UUID
    customer_name: str
    address: str
    status: OrderStatus
    items: list[OrderItemRead]
    total_rub: int


class CartLineRead(BaseModel):
    product_id: str
    name: str
    quantity: int
    unit_price_rub: int
    line_total_rub: int


class CartItemAdd(BaseModel):
    product_id: str = Field(min_length=1, max_length=128)
    quantity: int = Field(default=1, ge=1, le=10_000)

