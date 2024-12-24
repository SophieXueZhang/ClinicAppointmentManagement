from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from typing import List

from .. import crud, schemas, database

router = APIRouter(
    prefix="/ticket-types",
    tags=["票价管理"]
)

# Dependency
def get_db():
    db = database.SessionLocal()
    try:
        yield db
    finally:
        db.close()

@router.post("/", response_model=schemas.TicketType)
def create_ticket_type(ticket_type: schemas.TicketTypeCreate, db: Session = Depends(get_db)):
    """
    创建新的票种类型
    """
    return crud.create_ticket_type(db=db, ticket_type=ticket_type)

@router.get("/", response_model=List[schemas.TicketType])
def list_ticket_types(skip: int = 0, limit: int = 100, db: Session = Depends(get_db)):
    """
    获取所有票种列表
    """
    return crud.get_ticket_types(db, skip=skip, limit=limit)

@router.get("/{ticket_type_id}", response_model=schemas.TicketType)
def get_ticket_type(ticket_type_id: int, db: Session = Depends(get_db)):
    """
    获取指定票种详情
    """
    db_ticket_type = crud.get_ticket_type(db, ticket_type_id=ticket_type_id)
    if db_ticket_type is None:
        raise HTTPException(status_code=404, detail="票种不存在")
    return db_ticket_type

@router.put("/{ticket_type_id}", response_model=schemas.TicketType)
def update_ticket_type(
    ticket_type_id: int,
    ticket_type: schemas.TicketTypeCreate,
    db: Session = Depends(get_db)
):
    """
    更新票种信息
    """
    db_ticket_type = crud.get_ticket_type(db, ticket_type_id=ticket_type_id)
    if db_ticket_type is None:
        raise HTTPException(status_code=404, detail="票种不存在")
    return crud.update_ticket_type(db=db, ticket_type_id=ticket_type_id, ticket_type=ticket_type)
