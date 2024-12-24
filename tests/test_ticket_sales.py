"""
Test module for ticket sales functionality
"""
import pytest
from fastapi.testclient import TestClient
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from datetime import datetime, date

from app.main import app
from app.database import Base, get_db
from app.models import TicketType, Operator, TicketSale, DailySalesStats, MonthlySalesStats
from app import crud, schemas

# Setup test database
SQLALCHEMY_DATABASE_URL = "sqlite:///./test.db"
engine = create_engine(SQLALCHEMY_DATABASE_URL)
TestingSessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

def override_get_db():
    """Override database dependency"""
    try:
        db = TestingSessionLocal()
        yield db
    finally:
        db.close()

# Override the database dependency
app.dependency_overrides[get_db] = override_get_db

def setup_module():
    """Create test database and tables"""
    Base.metadata.drop_all(bind=engine)  # Clean up first
    Base.metadata.create_all(bind=engine)

def teardown_module():
    """Drop test database tables"""
    Base.metadata.drop_all(bind=engine)

@pytest.fixture(autouse=True)
def clean_db():
    """Clean database before each test"""
    Base.metadata.drop_all(bind=engine)
    Base.metadata.create_all(bind=engine)

@pytest.fixture
def db_session():
    """Get database session"""
    try:
        db = TestingSessionLocal()
        yield db
    finally:
        db.close()

@pytest.fixture
def client():
    """Get test client"""
    return TestClient(app)

@pytest.fixture
def test_operator(db_session):
    """Create test operator"""
    operator = crud.create_operator(
        db_session,
        schemas.OperatorCreate(
            name="Test Operator",
            username="testop",
            password="testpass",
            status=True
        )
    )
    return operator

@pytest.fixture
def test_ticket_types(db_session):
    """Create test ticket types"""
    types = [
        schemas.TicketTypeCreate(
            name="团体票",
            price=40.0,
            description="30人以上团体",
            status=True,
            min_group_size=30
        ),
        schemas.TicketTypeCreate(
            name="成人票",
            price=50.0,
            description="普通成人票",
            status=True
        )
    ]
    return [crud.create_ticket_type(db_session, t) for t in types]

def test_create_group_ticket_below_minimum(db_session, test_operator, test_ticket_types):
    """Test creating group ticket sale with quantity below minimum"""
    group_ticket = test_ticket_types[0]  # 团体票
    
    # Try to create sale with quantity below minimum
    with pytest.raises(Exception) as exc_info:
        crud.create_ticket_sale(
            db_session,
            schemas.TicketSaleCreate(
                ticket_type_id=group_ticket.id,
                quantity=20,  # Below minimum of 30
                total_amount=800.0
            ),
            test_operator.id
        )
    assert "团体票最小购买数量为30人" in str(exc_info.value.detail)

def test_create_group_ticket_valid(db_session, test_operator, test_ticket_types):
    """Test creating valid group ticket sale"""
    group_ticket = test_ticket_types[0]  # 团体票
    
    # Create sale with valid quantity
    sale = crud.create_ticket_sale(
        db_session,
        schemas.TicketSaleCreate(
            ticket_type_id=group_ticket.id,
            quantity=35,  # Above minimum of 30
            total_amount=1400.0
        ),
        test_operator.id
    )
    assert sale.quantity == 35
    assert sale.status == True

def test_daily_sales_stats_update(db_session, test_operator, test_ticket_types):
    """Test that daily sales stats are updated after sale"""
    regular_ticket = test_ticket_types[1]  # 成人票
    today = date.today().isoformat()
    
    # Create a sale
    sale = crud.create_ticket_sale(
        db_session,
        schemas.TicketSaleCreate(
            ticket_type_id=regular_ticket.id,
            quantity=2,
            total_amount=100.0
        ),
        test_operator.id
    )
    
    # Check daily stats
    stats = db_session.query(DailySalesStats).filter(
        DailySalesStats.date == today
    ).first()
    
    assert stats is not None
    assert stats.total_transactions == 1
    assert stats.total_quantity == 2
    assert stats.total_amount == 100.0

def test_monthly_sales_stats_update(db_session, test_operator, test_ticket_types):
    """Test that monthly sales stats are updated after sale"""
    regular_ticket = test_ticket_types[1]  # 成人票
    year_month = datetime.now().strftime("%Y-%m")
    
    # Create a sale
    sale = crud.create_ticket_sale(
        db_session,
        schemas.TicketSaleCreate(
            ticket_type_id=regular_ticket.id,
            quantity=2,
            total_amount=100.0
        ),
        test_operator.id
    )
    
    # Check monthly stats
    stats = db_session.query(MonthlySalesStats).filter(
        MonthlySalesStats.year_month == year_month
    ).first()
    
    assert stats is not None
    assert stats.total_transactions == 1
    assert stats.total_quantity == 2
    assert stats.total_amount == 100.0

def test_refund_updates_stats(db_session, test_operator, test_ticket_types):
    """Test that refund updates daily stats"""
    regular_ticket = test_ticket_types[1]  # 成人票
    today = date.today().isoformat()
    
    # Create a sale
    sale = crud.create_ticket_sale(
        db_session,
        schemas.TicketSaleCreate(
            ticket_type_id=regular_ticket.id,
            quantity=2,
            total_amount=100.0
        ),
        test_operator.id
    )
    
    # Create refund
    refund = crud.create_refund(
        db_session,
        schemas.RefundCreate(
            sale_id=sale.id,
            reason="Test refund"
        ),
        test_operator.id
    )
    
    # Check daily stats
    stats = db_session.query(DailySalesStats).filter(
        DailySalesStats.date == today
    ).first()
    
    assert stats is not None
    assert stats.total_transactions == 0  # Should be back to 0
    assert stats.total_quantity == 0
    assert stats.total_amount == 0.0
