from fastapi import APIRouter, Depends, HTTPException
from fastapi.security import OAuth2PasswordBearer, OAuth2PasswordRequestForm
from sqlalchemy.orm import Session
from typing import List
from datetime import datetime, timedelta
from jose import JWTError, jwt
from .. import crud, schemas, database

# JWT configuration
SECRET_KEY = "your-secret-key-keep-it-secret"  # In production, this should be in environment variables
ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRE_MINUTES = 30

router = APIRouter(
    prefix="/operators",
    tags=["营业员管理"]
)

oauth2_scheme = OAuth2PasswordBearer(tokenUrl="operators/token")

# Dependency
def get_db():
    db = database.SessionLocal()
    try:
        yield db
    finally:
        db.close()

def create_access_token(data: dict):
    to_encode = data.copy()
    expire = datetime.utcnow() + timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
    to_encode.update({"exp": expire})
    encoded_jwt = jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)
    return encoded_jwt

async def get_current_operator(token: str = Depends(oauth2_scheme), db: Session = Depends(get_db)):
    credentials_exception = HTTPException(
        status_code=401,
        detail="无效的认证凭据",
        headers={"WWW-Authenticate": "Bearer"},
    )
    try:
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
        username: str = payload.get("sub")
        if username is None:
            raise credentials_exception
    except JWTError:
        raise credentials_exception
    operator = crud.get_operator_by_username(db, username=username)
    if operator is None:
        raise credentials_exception
    return operator

@router.post("/token")
async def login(
    form_data: OAuth2PasswordRequestForm = Depends(),
    db: Session = Depends(get_db)
):
    """
    营业员登录
    """
    operator = crud.get_operator_by_username(db, username=form_data.username)
    if not operator or not crud.verify_password(form_data.password, operator.password):
        raise HTTPException(
            status_code=401,
            detail="用户名或密码错误",
            headers={"WWW-Authenticate": "Bearer"},
        )
    access_token = create_access_token(data={"sub": operator.username})
    return {"access_token": access_token, "token_type": "bearer"}

@router.post("/", response_model=schemas.Operator)
def create_operator(
    operator: schemas.OperatorCreate,
    db: Session = Depends(get_db),
    current_operator: schemas.Operator = Depends(get_current_operator)
):
    """
    创建新的营业员账号
    """
    db_operator = crud.get_operator_by_username(db, username=operator.username)
    if db_operator:
        raise HTTPException(status_code=400, detail="用户名已存在")
    return crud.create_operator(db=db, operator=operator)

@router.get("/", response_model=List[schemas.Operator])
def list_operators(
    skip: int = 0,
    limit: int = 100,
    db: Session = Depends(get_db),
    current_operator: schemas.Operator = Depends(get_current_operator)
):
    """
    获取营业员列表
    """
    return crud.get_operators(db, skip=skip, limit=limit)

@router.get("/me", response_model=schemas.Operator)
def read_operator_me(current_operator: schemas.Operator = Depends(get_current_operator)):
    """
    获取当前登录营业员信息
    """
    return current_operator
