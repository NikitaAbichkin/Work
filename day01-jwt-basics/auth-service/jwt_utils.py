import jwt
from datetime import datetime, timedelta, timezone
from typing import Optional
import config

"""
create_token(принимает словарь, и потом создается токен исходя из параметров)
verify_token(принимает строку в виде токена)
- возращает словарь с параметрами декодированного токена зная тип шифрования и секретный ключ
"""


def create_token(data:dict,expires_delta:Optional[timedelta] = None)->str:
    """
    Создает JWT токен
    1. Копируем данные
    2. Добавляем exp (время истечения и дату создания)
    3. jwt.encode(данные, ключ, алгоритм) -> токен
    """

    copy_of_our_dict = data.copy() # создаем копию нашего словаря с данными
    
    if expires_delta:
        expire = datetime.now(timezone.utc)+ expires_delta    
        """
        время сейчас + столько сколько указали в expires_delta
        """
    else:
          
          expire = datetime.now(timezone.utc) + timedelta(minutes=3)

          """
          если время не прописано при создании токена то он дейсвтует 15 минут от 
            время сейчас + 15 минут
        """
    iat = datetime.now(timezone.utc)
    copy_of_our_dict.update({"exp":expire})
    copy_of_our_dict.update({"iat":iat})
    token = jwt.encode(copy_of_our_dict,config.settings.JWT_SECRET_KEY,algorithm=config.settings.JWT_ALGORITHM)
    return token

def verify_token(token:str):
     try:
        payload = jwt.decode(token,config.settings.JWT_SECRET_KEY,algorithms=[config.settings.JWT_ALGORITHM])
        return payload
     except jwt.ExpiredSignatureError:
        print("Токен истек")
        return None
     except jwt.InvalidTokenError:
        print("Невалидный токен")
        return None
     
if __name__=="__main__":
    userData = {
        "user_id": 123, 
        "username": "john"
    }
    time = timedelta(minutes=15)
    token = create_token(userData, time)
    print (token)
    print (verify_token(token))
    





    



