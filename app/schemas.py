from pydantic import BaseModel
from datetime import datetime
from typing import Optional, List

# Ticket Type Schemas
class TicketTypeBase(BaseModel):
    name: str
    price: float
    description: Optional[str] = None
    status: bool = True
    min_group_size: Optional[int] = None

class TicketTypeCreate(TicketTypeBase):
    pass

class TicketType(TicketTypeBase):
    id: int

    class Config:
        from_attributes = True

# Operator Schemas
class OperatorBase(BaseModel):
    name: str
    username: str
    status: bool = True

class OperatorCreate(OperatorBase):
    password: str

class Operator(OperatorBase):
    id: int
    create_time: datetime

    class Config:
        from_attributes = True

# Ticket Sale Schemas
class TicketSaleBase(BaseModel):
    ticket_type_id: int
    quantity: int
    total_amount: float

class TicketSaleCreate(TicketSaleBase):
    pass

class TicketSale(TicketSaleBase):
    id: int
    operator_id: int
    sale_time: datetime
    status: bool

    class Config:
        from_attributes = True

# Refund Schemas
class RefundBase(BaseModel):
    sale_id: int
    reason: Optional[str] = None

class RefundCreate(RefundBase):
    pass

class Refund(RefundBase):
    id: int
    operator_id: int
    refund_time: datetime

    class Config:
        from_attributes = True
