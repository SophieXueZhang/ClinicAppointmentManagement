from fastapi import FastAPI, Depends
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy.orm import Session

from . import models, schemas, utils, initial_operator, triggers
from .database import engine, get_db, SessionLocal
from .routers import ticket_types, sales, operators

# Create database tables
models.Base.metadata.create_all(bind=engine)

# Set up stored procedure triggers
from .stored_procedures import setup_stored_procedures
setup_stored_procedures(models.Base, engine)

# Initialize default data
db = SessionLocal()
utils.init_default_ticket_types(db)
initial_operator.init_default_operator(db)
db.close()

app = FastAPI(title="景点门票销售管理系统")

# Disable CORS. Do not remove this for full-stack development.
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Allows all origins
    allow_credentials=True,
    allow_methods=["*"],  # Allows all methods
    allow_headers=["*"],  # Allows all headers
)

# Include routers
app.include_router(ticket_types.router)
app.include_router(sales.router)
app.include_router(operators.router)

@app.get("/healthz")
async def healthz():
    return {"status": "ok"}
