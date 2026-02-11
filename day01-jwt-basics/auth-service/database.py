from sqlalchemy import create_engine
from sqlalchemy.orm import DeclarativeBase, sessionmaker
from config import settings

engine = create_engine(settings.DATABASE_URL)

Sessionlocal = sessionmaker(bind=engine)

class Base(DeclarativeBase):
    pass

def get_db():
    db = Sessionlocal() # обьект класса Sessionlocal
    try:
        yield db
    finally:
        db.close() 

"""
говорю что открой соединие с базой и держи его открытым, пока тебя еще раз не пнут
(фаст апи делает это сам)

"""
