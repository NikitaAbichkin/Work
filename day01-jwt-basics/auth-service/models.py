from datetime import datetime, timezone
from sqlalchemy import String
from sqlalchemy.orm import Mapped, mapped_column
from database import Base

class User (Base):
    __tablename__ = "users"
    id:Mapped[int] =  mapped_column(primary_key=True)
    username:Mapped[str] = mapped_column(String(50),unique=True,index=True)
    hashed_password: Mapped[str] = mapped_column(String (5000))
    email:Mapped[str] = mapped_column(String(255),nullable=True)
    created_at:Mapped[datetime] = mapped_column(default= lambda:datetime.now(timezone.utc))
    phone: Mapped[str] = mapped_column(String(20), nullable=True)



