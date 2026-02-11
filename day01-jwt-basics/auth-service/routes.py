
import password_utils
from sqlalchemy.orm import Session
from models import User
import jwt_utils
from datetime import timedelta
from fastapi import Header, FastAPI
from fastapi import APIRouter, Depends
from fastapi.middleware.cors import CORSMiddleware
from database import get_db, engine, Base
from config import settings

Base.metadata.create_all(bind=engine)

app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:5173"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

router = APIRouter(prefix="/api")

@router.post("/register")
def registration(data:dict,db: Session = Depends(get_db) ):
    username = data["username"]
    password =  data["password"]
    hashed_password= password_utils.hash_password(password)
    existing_user = db.query(User).filter(User.username == username).first()

    if existing_user:
        # Нашли — значит занят
        return {"error": "Username already exists"}
    
    new_user  = (User(
        username = username,
        hashed_password = hashed_password,
    ))
    db.add(new_user) # добавили

    db.commit() # сохраняем в бд

    # Обновляем объект (чтобы получить id который БД присвоила)
    db.refresh(new_user)
    return {"username": new_user.username, "message": "User registered successfully"}

@router.post("/login")
def login(data:dict,db:Session = Depends(get_db)):
    username = data["username"]
    password_first = data["password"]

    user = db.query(User).filter(User.username == username).first()
    
    if not user:
        return {"error": "User not found"}

    
    password_hash = user.hashed_password
    if password_utils.verify_password(password_first,password_hash):
        data_for_creating_token = {"username":user.username,"user_id":user.id}
        token = jwt_utils.create_token(data_for_creating_token,timedelta(minutes=settings.ACCESS_TOKEN_EXPIRE_MINUTES))
        return {"access_token":token,"token_type": "bearer"}
    else:
        return {"error": "Wrong password"}
    
    
    
@router.get("/profile")
def profile (authorization:str  = Header(alias= "authorization")):
    token = authorization.replace("Bearer ", "")
    payload = jwt_utils.verify_token(token)
    
    
    if not payload:
        return{"error": "Wrong token"}
    
    id:str = payload["user_id"]
    username = payload["username"]
    return{ "username": username, "user_id": id}

app.include_router(router)



    




    
    





    


