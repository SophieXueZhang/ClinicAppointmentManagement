"""
This module contains SQLAlchemy event listeners that act as triggers for the ticket system.
"""

from sqlalchemy import event
from datetime import datetime
from . import models

# Trigger: Validate ticket sale quantity and calculate total amount
@event.listens_for(models.TicketSale, 'before_insert')
def validate_ticket_sale(mapper, connection, target):
    if target.quantity <= 0:
        raise ValueError("门票数量必须大于0")
    
    # Calculate total amount based on ticket type price
    ticket_type = connection.execute(
        models.TicketType.__table__.select()
        .where(models.TicketType.id == target.ticket_type_id)
    ).first()
    
    if not ticket_type:
        raise ValueError("无效的票种")
    
    target.total_amount = ticket_type.price * target.quantity

# Trigger: Update sale status when refund is created
@event.listens_for(models.Refund, 'after_insert')
def update_sale_status_on_refund(mapper, connection, target):
    connection.execute(
        models.TicketSale.__table__.update()
        .where(models.TicketSale.id == target.sale_id)
        .values(status=False)
    )

# Trigger: Log ticket sales operations
@event.listens_for(models.TicketSale, 'after_insert')
def log_ticket_sale(mapper, connection, target):
    connection.execute(
        models.SalesLog.__table__.insert().values(
            operation_type=models.OperationType.sale,
            ticket_sale_id=target.id,
            operator_id=target.operator_id,
            operation_time=datetime.now(),
            details=f"售出 {target.quantity} 张票，总金额 {target.total_amount}"
        )
    )

# Trigger: Log refund operations
@event.listens_for(models.Refund, 'after_insert')
def log_refund(mapper, connection, target):
    sale = connection.execute(
        models.TicketSale.__table__.select()
        .where(models.TicketSale.id == target.sale_id)
    ).first()
    
    if sale:
        connection.execute(
            models.SalesLog.__table__.insert().values(
                operation_type=models.OperationType.refund,
                ticket_sale_id=target.sale_id,
                operator_id=target.operator_id,
                operation_time=datetime.now(),
                details=f"退票，退款金额 {sale.total_amount}"
            )
        )

# Trigger: Validate operator status before sale
@event.listens_for(models.TicketSale, 'before_insert')
def validate_operator(mapper, connection, target):
    operator = connection.execute(
        models.Operator.__table__.select()
        .where(models.Operator.id == target.operator_id)
    ).first()
    
    if not operator or not operator.status:
        raise ValueError("无效的营业员账号")

# Trigger: Validate ticket type status before sale
@event.listens_for(models.TicketSale, 'before_insert')
def validate_ticket_type(mapper, connection, target):
    ticket_type = connection.execute(
        models.TicketType.__table__.select()
        .where(models.TicketType.id == target.ticket_type_id)
    ).first()
    
    if not ticket_type or not ticket_type.status:
        raise ValueError("无效的票种")
