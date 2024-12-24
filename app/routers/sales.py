from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from typing import List
from datetime import date

from .. import crud, schemas, database
from ..routers.operators import get_current_operator

router = APIRouter(
    prefix="/sales",
    tags=["门票销售"]
)

# Dependency
def get_db():
    db = database.SessionLocal()
    try:
        yield db
    finally:
        db.close()

@router.post("/", response_model=schemas.TicketSale)
def create_sale(
    ticket_sale: schemas.TicketSaleCreate,
    db: Session = Depends(get_db),
    current_operator: schemas.Operator = Depends(get_current_operator)
):
    """
    创建新的门票销售记录
    """
    # Verify ticket type exists
    ticket_type = crud.get_ticket_type(db, ticket_type_id=ticket_sale.ticket_type_id)
    if not ticket_type:
        raise HTTPException(status_code=404, detail="票种不存在")
    
    # Total amount will be calculated in crud.create_ticket_sale
    
    return crud.create_ticket_sale(db=db, ticket_sale=ticket_sale, operator_id=current_operator.id)

@router.post("/{sale_id}/refund", response_model=schemas.Refund)
def create_refund(
    sale_id: int,
    refund: schemas.RefundCreate,
    db: Session = Depends(get_db),
    current_operator: schemas.Operator = Depends(get_current_operator)
):
    """
    创建退票记录
    """
    # Verify sale exists and hasn't been refunded
    sale = crud.get_ticket_sale(db, sale_id=sale_id)
    if not sale:
        raise HTTPException(status_code=404, detail="销售记录不存在")
    if not sale.status:
        raise HTTPException(status_code=400, detail="该票已退款")
    
    return crud.create_refund(db=db, refund=refund, operator_id=current_operator.id)

@router.get("/stats/daily/{date}")
def get_daily_stats(
    date: date,
    db: Session = Depends(get_db),
    current_operator: schemas.Operator = Depends(get_current_operator)
):
    """
    获取指定日期的门票销售统计
    """
    return crud.get_daily_sales_stats(db, target_date=date)

@router.get("/stats/monthly/{year}/{month}")
def get_monthly_stats(
    year: int,
    month: int,
    db: Session = Depends(get_db),
    current_operator: schemas.Operator = Depends(get_current_operator)
):
    """
    获取指定月份的门票销售统计
    """
    return crud.get_monthly_sales_stats(db, year=year, month=month)

@router.get("/stats/daily/{date}/by-type")
def get_daily_stats_by_type(
    date: date,
    db: Session = Depends(get_db),
    current_operator: schemas.Operator = Depends(get_current_operator)
):
    """
    获取指定日期各种票价的销售统计
    """
    return crud.get_daily_sales_by_type(db, target_date=date)

@router.get("/stats/operator/{operator_id}/daily/{date}")
def get_operator_daily_stats(
    operator_id: int,
    date: date,
    db: Session = Depends(get_db),
    current_operator: schemas.Operator = Depends(get_current_operator)
):
    """
    获取指定营业员指定日期的收费统计
    """
    return crud.get_operator_daily_stats(db, operator_id=operator_id, target_date=date)
