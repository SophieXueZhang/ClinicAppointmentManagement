from sqlalchemy import Boolean, Column, ForeignKey, Integer, String, Float, DateTime, Text, Enum
from sqlalchemy.orm import relationship
from .database import Base
from datetime import datetime, UTC
import enum

class OperationType(enum.Enum):
    sale = "sale"
    refund = "refund"

class TicketType(Base):
    __tablename__ = "ticket_types"

    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(50), nullable=False)
    price = Column(Float, nullable=False)
    description = Column(Text)
    status = Column(Boolean, default=True)  # True for enabled, False for disabled
    min_group_size = Column(Integer, nullable=True)  # Minimum size for group tickets

    # Relationship with sales
    sales = relationship("TicketSale", back_populates="ticket_type")

class Operator(Base):
    __tablename__ = "operators"

    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(50), nullable=False)
    username = Column(String(50), unique=True, nullable=False)
    password = Column(String(100), nullable=False)  # Will store hashed password
    status = Column(Boolean, default=True)  # True for active, False for inactive
    create_time = Column(DateTime, default=lambda: datetime.now(UTC))

    # Relationships
    sales = relationship("TicketSale", back_populates="operator")
    refunds = relationship("Refund", back_populates="operator")
    logs = relationship("SalesLog", back_populates="operator")

class TicketSale(Base):
    __tablename__ = "ticket_sales"

    id = Column(Integer, primary_key=True, index=True)
    ticket_type_id = Column(Integer, ForeignKey("ticket_types.id"))
    operator_id = Column(Integer, ForeignKey("operators.id"))
    quantity = Column(Integer, nullable=False)
    total_amount = Column(Float, nullable=False)
    sale_time = Column(DateTime, default=lambda: datetime.now(UTC))
    status = Column(Boolean, default=True)  # True for valid, False for refunded

    # Relationships
    ticket_type = relationship("TicketType", back_populates="sales")
    operator = relationship("Operator", back_populates="sales")
    refund = relationship("Refund", back_populates="sale", uselist=False)
    logs = relationship("SalesLog", back_populates="ticket_sale")

class Refund(Base):
    __tablename__ = "refunds"

    id = Column(Integer, primary_key=True, index=True)
    sale_id = Column(Integer, ForeignKey("ticket_sales.id"), unique=True)
    operator_id = Column(Integer, ForeignKey("operators.id"))
    refund_time = Column(DateTime, default=lambda: datetime.now(UTC))
    reason = Column(Text)

    # Relationships
    sale = relationship("TicketSale", back_populates="refund")
    operator = relationship("Operator", back_populates="refunds")

class SalesLog(Base):
    """Sales operation log table"""
    __tablename__ = "sales_logs"

    id = Column(Integer, primary_key=True, index=True)
    operation_type = Column(Enum(OperationType))
    ticket_sale_id = Column(Integer, ForeignKey("ticket_sales.id"))
    operator_id = Column(Integer, ForeignKey("operators.id"))
    operation_time = Column(DateTime, default=lambda: datetime.now(UTC))
    details = Column(Text)

    # Relationships
    ticket_sale = relationship("TicketSale", back_populates="logs")
    operator = relationship("Operator", back_populates="logs")

class DailySalesStats(Base):
    """Daily sales statistics table (emulated stored procedure)"""
    __tablename__ = "daily_sales_stats"

    date = Column(String, primary_key=True)
    total_transactions = Column(Integer, default=0)
    total_quantity = Column(Integer, default=0)
    total_amount = Column(Float, default=0.0)
    updated_at = Column(DateTime, default=datetime.utcnow)

class MonthlySalesStats(Base):
    """Monthly sales statistics table (emulated stored procedure)"""
    __tablename__ = "monthly_sales_stats"

    year_month = Column(String, primary_key=True)  # Format: YYYY-MM
    total_transactions = Column(Integer, default=0)
    total_quantity = Column(Integer, default=0)
    total_amount = Column(Float, default=0.0)
    updated_at = Column(DateTime, default=datetime.utcnow)

class TicketTypeStats(Base):
    """Ticket type sales statistics table (emulated stored procedure)"""
    __tablename__ = "ticket_type_stats"

    ticket_type_id = Column(Integer, ForeignKey("ticket_types.id"), primary_key=True)
    total_transactions = Column(Integer, default=0)
    total_quantity = Column(Integer, default=0)
    total_amount = Column(Float, default=0.0)
    updated_at = Column(DateTime, default=lambda: datetime.now(UTC))

class OperatorStats(Base):
    """Operator sales statistics table (emulated stored procedure)"""
    __tablename__ = "operator_stats"


    operator_id = Column(Integer, ForeignKey("operators.id"), primary_key=True)
    total_transactions = Column(Integer, default=0)
    total_quantity = Column(Integer, default=0)
    total_amount = Column(Float, default=0.0)
    updated_at = Column(DateTime, default=lambda: datetime.now(UTC))
