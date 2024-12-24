from sqlalchemy.orm import Session
from sqlalchemy import func, event
from datetime import datetime, date
from fastapi import HTTPException
from . import models, schemas
from passlib.context import CryptContext

pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

# Ticket Type operations
def create_ticket_type(db: Session, ticket_type: schemas.TicketTypeCreate):
    db_ticket_type = models.TicketType(**ticket_type.model_dump())
    db.add(db_ticket_type)
    db.commit()
    db.refresh(db_ticket_type)
    return db_ticket_type

def get_ticket_types(db: Session, skip: int = 0, limit: int = 100):
    return db.query(models.TicketType).offset(skip).limit(limit).all()

def get_ticket_type(db: Session, ticket_type_id: int):
    return db.query(models.TicketType).filter(models.TicketType.id == ticket_type_id).first()

def update_ticket_type(db: Session, ticket_type_id: int, ticket_type: schemas.TicketTypeCreate):
    db_ticket_type = db.query(models.TicketType).filter(models.TicketType.id == ticket_type_id).first()
    for key, value in ticket_type.model_dump().items():
        setattr(db_ticket_type, key, value)
    db.commit()
    db.refresh(db_ticket_type)
    return db_ticket_type

# Operator operations
def create_operator(db: Session, operator: schemas.OperatorCreate):
    hashed_password = pwd_context.hash(operator.password)
    db_operator = models.Operator(
        name=operator.name,
        username=operator.username,
        password=hashed_password,
        status=operator.status
    )
    db.add(db_operator)
    db.commit()
    db.refresh(db_operator)
    return db_operator

def get_operator(db: Session, operator_id: int):
    return db.query(models.Operator).filter(models.Operator.id == operator_id).first()

def get_operators(db: Session, skip: int = 0, limit: int = 100):
    return db.query(models.Operator).offset(skip).limit(limit).all()

def get_operator_by_username(db: Session, username: str):
    return db.query(models.Operator).filter(models.Operator.username == username).first()

def verify_password(plain_password: str, hashed_password: str):
    return pwd_context.verify(plain_password, hashed_password)

# Ticket Sale operations
def create_ticket_sale(db: Session, ticket_sale: schemas.TicketSaleCreate, operator_id: int):
    # Get ticket type to check group size requirements
    ticket_type = db.query(models.TicketType).filter(models.TicketType.id == ticket_sale.ticket_type_id).first()
    if not ticket_type:
        raise HTTPException(status_code=404, detail="票种不存在")
    
    # Validate group size if minimum is set
    if ticket_type.min_group_size and ticket_sale.quantity < ticket_type.min_group_size:
        raise HTTPException(
            status_code=400,
            detail=f"{ticket_type.name}最小购买数量为{ticket_type.min_group_size}人"
        )
        
    # Calculate total amount based on ticket price and quantity
    ticket_sale.total_amount = ticket_type.price * ticket_sale.quantity
    
    db_ticket_sale = models.TicketSale(
        **ticket_sale.model_dump(),
        operator_id=operator_id
    )
    db.add(db_ticket_sale)
    db.commit()
    db.refresh(db_ticket_sale)
    return db_ticket_sale

def get_ticket_sale(db: Session, sale_id: int):
    return db.query(models.TicketSale).filter(models.TicketSale.id == sale_id).first()

# Refund operations
def create_refund(db: Session, refund: schemas.RefundCreate, operator_id: int):
    db_refund = models.Refund(
        **refund.model_dump(),
        operator_id=operator_id
    )
    # Update the sale status
    sale = db.query(models.TicketSale).filter(models.TicketSale.id == refund.sale_id).first()
    if sale:
        sale.status = False
    db.add(db_refund)
    db.commit()
    db.refresh(db_refund)
    return db_refund

# Statistical stored procedures (implemented as functions)
def get_daily_sales_stats(db: Session, target_date: date):
    """统计指定日期的门票销售情况"""
    try:
        # Get basic sales statistics
        basic_stats = db.query(
            func.count(models.TicketSale.id).label('total_transactions'),
            func.sum(models.TicketSale.quantity).label('total_quantity'),
            func.sum(models.TicketSale.total_amount).label('total_amount')
        ).filter(
            func.date(models.TicketSale.sale_time) == target_date,
            models.TicketSale.status == True
        ).first()

        # Get detailed sales by ticket type
        detailed_stats = db.query(
            models.TicketType.name,
            models.TicketType.price,
            func.count(models.TicketSale.id).label('transactions'),
            func.sum(models.TicketSale.quantity).label('quantity'),
            func.sum(models.TicketSale.total_amount).label('amount')
        ).join(models.TicketType).filter(
            func.date(models.TicketSale.sale_time) == target_date,
            models.TicketSale.status == True
        ).group_by(
            models.TicketType.name,
            models.TicketType.price
        ).all()

        # Get hourly distribution
        hourly_stats = db.query(
            func.extract('hour', models.TicketSale.sale_time).label('hour'),
            func.sum(models.TicketSale.quantity).label('quantity'),
            func.sum(models.TicketSale.total_amount).label('amount')
        ).filter(
            func.date(models.TicketSale.sale_time) == target_date,
            models.TicketSale.status == True
        ).group_by(
            func.extract('hour', models.TicketSale.sale_time)
        ).order_by('hour').all()

        return {
            "date": target_date,
            "summary": {
                "total_transactions": basic_stats[0] if basic_stats else 0,
                "total_quantity": basic_stats[1] if basic_stats else 0,
                "total_amount": basic_stats[2] if basic_stats else 0
            },
            "by_ticket_type": [
                {
                    "ticket_type": stat[0],
                    "price": stat[1],
                    "transactions": stat[2],
                    "quantity": stat[3],
                    "amount": stat[4]
                } for stat in detailed_stats
            ],
            "hourly_distribution": [
                {
                    "hour": stat[0],
                    "quantity": stat[1],
                    "amount": stat[2]
                } for stat in hourly_stats
            ]
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"统计数据生成失败: {str(e)}")

def get_monthly_sales_stats(db: Session, year: int, month: int):
    """统计指定月份的门票销售情况"""
    try:
        # Get monthly summary
        monthly_summary = db.query(
            func.count(models.TicketSale.id).label('total_transactions'),
            func.sum(models.TicketSale.quantity).label('total_quantity'),
            func.sum(models.TicketSale.total_amount).label('total_amount')
        ).filter(
            func.extract('year', models.TicketSale.sale_time) == year,
            func.extract('month', models.TicketSale.sale_time) == month,
            models.TicketSale.status == True
        ).first()

        # Get daily breakdown
        daily_stats = db.query(
            func.date(models.TicketSale.sale_time).label('date'),
            func.count(models.TicketSale.id).label('transactions'),
            func.sum(models.TicketSale.quantity).label('quantity'),
            func.sum(models.TicketSale.total_amount).label('amount')
        ).filter(
            func.extract('year', models.TicketSale.sale_time) == year,
            func.extract('month', models.TicketSale.sale_time) == month,
            models.TicketSale.status == True
        ).group_by(
            func.date(models.TicketSale.sale_time)
        ).order_by(
            func.date(models.TicketSale.sale_time)
        ).all()

        # Get ticket type breakdown
        type_stats = db.query(
            models.TicketType.name,
            func.count(models.TicketSale.id).label('transactions'),
            func.sum(models.TicketSale.quantity).label('quantity'),
            func.sum(models.TicketSale.total_amount).label('amount')
        ).join(models.TicketType).filter(
            func.extract('year', models.TicketSale.sale_time) == year,
            func.extract('month', models.TicketSale.sale_time) == month,
            models.TicketSale.status == True
        ).group_by(
            models.TicketType.name
        ).all()

        return {
            "year": year,
            "month": month,
            "summary": {
                "total_transactions": monthly_summary[0] if monthly_summary else 0,
                "total_quantity": monthly_summary[1] if monthly_summary else 0,
                "total_amount": monthly_summary[2] if monthly_summary else 0
            },
            "daily_breakdown": [
                {
                    "date": stat[0],
                    "transactions": stat[1],
                    "quantity": stat[2],
                    "amount": stat[3]
                } for stat in daily_stats
            ],
            "by_ticket_type": [
                {
                    "ticket_type": stat[0],
                    "transactions": stat[1],
                    "quantity": stat[2],
                    "amount": stat[3]
                } for stat in type_stats
            ]
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"统计数据生成失败: {str(e)}")

def get_daily_sales_by_type(db: Session, target_date: date):
    """统计指定日期各种价格的门票销售情况"""
    try:
        # Get sales statistics by ticket type with percentages
        total_stats = db.query(
            func.sum(models.TicketSale.quantity).label('total_quantity'),
            func.sum(models.TicketSale.total_amount).label('total_amount')
        ).filter(
            func.date(models.TicketSale.sale_time) == target_date,
            models.TicketSale.status == True
        ).first()

        total_quantity = total_stats[0] if total_stats[0] else 0
        total_amount = total_stats[1] if total_stats[1] else 0

        type_stats = db.query(
            models.TicketType.name,
            models.TicketType.price,
            func.count(models.TicketSale.id).label('transactions'),
            func.sum(models.TicketSale.quantity).label('quantity'),
            func.sum(models.TicketSale.total_amount).label('amount')
        ).join(models.TicketType).filter(
            func.date(models.TicketSale.sale_time) == target_date,
            models.TicketSale.status == True
        ).group_by(
            models.TicketType.name,
            models.TicketType.price
        ).all()

        return {
            "date": target_date,
            "summary": {
                "total_quantity": total_quantity,
                "total_amount": total_amount
            },
            "by_ticket_type": [
                {
                    "ticket_type": stat[0],
                    "price": stat[1],
                    "transactions": stat[2],
                    "quantity": stat[3],
                    "amount": stat[4],
                    "quantity_percentage": (stat[3] / total_quantity * 100) if total_quantity > 0 else 0,
                    "amount_percentage": (stat[4] / total_amount * 100) if total_amount > 0 else 0
                } for stat in type_stats
            ]
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"统计数据生成失败: {str(e)}")

def get_operator_daily_stats(db: Session, operator_id: int, target_date: date):
    """统计指定营业员指定日期的收费情况"""
    try:
        # Get operator info
        operator = db.query(models.Operator).filter(models.Operator.id == operator_id).first()
        if not operator:
            raise HTTPException(status_code=404, detail="营业员不存在")

        # Get summary statistics
        summary_stats = db.query(
            func.count(models.TicketSale.id).label('total_transactions'),
            func.sum(models.TicketSale.quantity).label('total_quantity'),
            func.sum(models.TicketSale.total_amount).label('total_amount')
        ).filter(
            models.TicketSale.operator_id == operator_id,
            func.date(models.TicketSale.sale_time) == target_date,
            models.TicketSale.status == True
        ).first()

        # Get hourly breakdown
        hourly_stats = db.query(
            func.extract('hour', models.TicketSale.sale_time).label('hour'),
            func.count(models.TicketSale.id).label('transactions'),
            func.sum(models.TicketSale.quantity).label('quantity'),
            func.sum(models.TicketSale.total_amount).label('amount')
        ).filter(
            models.TicketSale.operator_id == operator_id,
            func.date(models.TicketSale.sale_time) == target_date,
            models.TicketSale.status == True
        ).group_by(
            func.extract('hour', models.TicketSale.sale_time)
        ).order_by('hour').all()

        # Get ticket type breakdown
        type_stats = db.query(
            models.TicketType.name,
            models.TicketType.price,
            func.count(models.TicketSale.id).label('transactions'),
            func.sum(models.TicketSale.quantity).label('quantity'),
            func.sum(models.TicketSale.total_amount).label('amount')
        ).join(models.TicketType).filter(
            models.TicketSale.operator_id == operator_id,
            func.date(models.TicketSale.sale_time) == target_date,
            models.TicketSale.status == True
        ).group_by(
            models.TicketType.name,
            models.TicketType.price
        ).all()

        return {
            "date": target_date,
            "operator": {
                "id": operator.id,
                "name": operator.name,
                "username": operator.username
            },
            "summary": {
                "total_transactions": summary_stats[0] if summary_stats[0] else 0,
                "total_quantity": summary_stats[1] if summary_stats[1] else 0,
                "total_amount": summary_stats[2] if summary_stats[2] else 0
            },
            "hourly_breakdown": [
                {
                    "hour": stat[0],
                    "transactions": stat[1],
                    "quantity": stat[2],
                    "amount": stat[3]
                } for stat in hourly_stats
            ],
            "by_ticket_type": [
                {
                    "ticket_type": stat[0],
                    "price": stat[1],
                    "transactions": stat[2],
                    "quantity": stat[3],
                    "amount": stat[4]
                } for stat in type_stats
            ]
        }
    except HTTPException as he:
        raise he
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"统计数据生成失败: {str(e)}")
