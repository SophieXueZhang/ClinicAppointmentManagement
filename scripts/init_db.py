import sys
import os
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from app import crud, schemas, database
from app.models import Base

def init_database():
    """Initialize database and create default operator"""
    # Create all tables
    Base.metadata.create_all(bind=database.engine)
    
    # Create default operator
    db = database.SessionLocal()
    try:
        default_operator = {
            "name": "管理员",
            "username": "admin",
            "password": "admin123",
            "status": True
        }
        
        # Check if operator exists
        operator = crud.get_operator_by_username(db, username=default_operator["username"])
        if not operator:
            operator = crud.create_operator(db=db, operator=schemas.OperatorCreate(**default_operator))
            print(f"Created default operator: {operator.username}")
        else:
            print(f"Default operator already exists: {operator.username}")
            
    finally:
        db.close()

if __name__ == "__main__":
    init_database()
