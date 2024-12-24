from sqlalchemy.orm import Session
from . import crud, schemas

def init_default_ticket_types(db: Session):
    """Initialize default ticket types if they don't exist"""
    default_types = [
        {"name": "老年票", "price": 20.0, "description": "适用于60岁及以上老年人，需要提供有效证件", "status": True},
        {"name": "儿童票", "price": 15.0, "description": "适用于6-12岁儿童，1.2米以下免票", "status": True},
        {"name": "成人票", "price": 50.0, "description": "适用于12-60岁成年人", "status": True},
        {"name": "团体票", "price": 40.0, "description": "30人以上团体，每人优惠价", "status": True, "min_group_size": 30},
    ]
    
    # Check if any ticket types exist
    existing_types = crud.get_ticket_types(db, skip=0, limit=1)
    if not existing_types:
        for ticket_type in default_types:
            crud.create_ticket_type(db=db, ticket_type=schemas.TicketTypeCreate(**ticket_type))
