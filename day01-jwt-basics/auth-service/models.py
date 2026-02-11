from datetime import datetime, timezone
from sqlalchemy.orm import Mapped, mapped_column
from database import Base
from sqlalchemy import String, func



class User (Base):
    __tablename__ = "users"
    id:Mapped[int] =  mapped_column(primary_key=True)
    username: Mapped[str] = mapped_column(String(100), unique=True, index=True)
    hashed_password: Mapped[str] = mapped_column(String (5000))
    email: Mapped[str] = mapped_column(String(255), nullable=True) 
    created_at:Mapped[datetime] = mapped_column(server_default= func.now())
    phone: Mapped[str] = mapped_column(String(20), nullable=True)
    is_active: Mapped[bool] = mapped_column(default=True)  # ← вот это добавляем
    

class Post(Base):
    __tablename__ = "posts"
    id: Mapped[int] = mapped_column(primary_key=True)
    title: Mapped[str] = mapped_column(String(200))
    content: Mapped[str] = mapped_column(String(50))
    views:Mapped[int] = mapped_column(default=0, server_default="0")
    


