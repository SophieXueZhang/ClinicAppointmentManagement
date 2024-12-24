from sqlalchemy.orm import Session
from . import crud, schemas

def init_default_operator(db: Session):
    """Initialize a default operator if none exists"""
    default_operator = {
        "name": "管理员",
        "username": "admin",
        "password": "admin123",
        "status": True
    }
    
    # Check if any operators exist
    operator = crud.get_operator_by_username(db, username=default_operator["username"])
    if not operator:
        crud.create_operator(db=db, operator=schemas.OperatorCreate(**default_operator))
