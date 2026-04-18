import uuid

from sqlalchemy import (
    CheckConstraint,
    Column,
    DateTime,
    Float,
    ForeignKey,
    Integer,
    String,
    Text,
    func,
)
from sqlalchemy.dialects.postgresql import UUID as PG_UUID
from sqlalchemy.orm import declarative_base, relationship

Base = declarative_base()


class Product(Base):
    __tablename__ = "products"

    id = Column(String, primary_key=True)
    name = Column(String, nullable=False)
    price_rub = Column(Integer, nullable=False)
    description = Column(Text, nullable=True)

    order_items = relationship(
        "OrderItem",
        back_populates="product",
        cascade="all, delete-orphan",
    )
    cart_lines = relationship(
        "CartLine",
        back_populates="product",
        cascade="all, delete-orphan",
    )


class CartLine(Base):
    """Одна корзина на всё приложение (для учебного сценария)."""

    __tablename__ = "cart_lines"

    product_id = Column(String, ForeignKey("products.id", ondelete="CASCADE"), primary_key=True)
    quantity = Column(Integer, nullable=False)

    __table_args__ = (
        CheckConstraint("quantity > 0", name="cart_lines_quantity_positive"),
    )

    product = relationship("Product", back_populates="cart_lines")


class SelectedAddress(Base):
    __tablename__ = "selected_address"

    # Один сохраненный адрес для учебного сценария.
    id = Column(Integer, primary_key=True, default=1)
    label = Column(String, nullable=False)
    latitude = Column(Float, nullable=False)
    longitude = Column(Float, nullable=False)


class Order(Base):
    __tablename__ = "orders"

    id = Column(PG_UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    customer_name = Column(String, nullable=False)
    address = Column(Text, nullable=False)

    # created -> confirmed -> prepared -> delivered -> cancelled
    status = Column(String, nullable=False, default="created")

    created_at = Column(DateTime(timezone=True), server_default=func.now(), nullable=False)
    updated_at = Column(DateTime(timezone=True), server_default=func.now(), onupdate=func.now(), nullable=False)

    items = relationship(
        "OrderItem",
        back_populates="order",
        cascade="all, delete-orphan",
        passive_deletes=True,
    )


class OrderItem(Base):
    __tablename__ = "order_items"

    id = Column(Integer, primary_key=True, autoincrement=True)
    order_id = Column(PG_UUID(as_uuid=True), ForeignKey("orders.id", ondelete="CASCADE"), index=True, nullable=False)
    product_id = Column(String, ForeignKey("products.id"), index=True, nullable=False)
    quantity = Column(Integer, nullable=False)

    __table_args__ = (
        CheckConstraint("quantity > 0", name="order_items_quantity_positive"),
    )

    order = relationship("Order", back_populates="items")
    product = relationship("Product", back_populates="order_items")

