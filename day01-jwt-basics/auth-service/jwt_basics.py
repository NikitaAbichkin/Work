from datetime import timedelta
from password_utils import hash_password, verify_password
from jwt_utils import create_token,verify_token


def simulate_registration(username:str,password:str)->dict:
    """
    Симуляция регистрации пользователя
    
    Цепочка:
    1. Получаем username и password
    2. Хешируем пароль
    3. Сохраняем username + хеш (вместо БД просто возвращаем)
    """

    hashed = hash_password(password)
    return {"username": username, "hashed_password": hashed}


def simulate_login( username:str,password:str,hash_passwrod:str):
    """
    Симуляция входа в систему
    
    Цепочка:
    1. Проверяем пароль через verify_password
    2. Если верный → создаем JWT токен
    3. Возвращаем токен клиенту
    """   
    if verify_password(password,hash_passwrod):
        print("✓ Пароль верный")
        token_data = {"username": username, "user_id": 1}
        acces_token = create_token(token_data,expires_delta=timedelta(hours=1))
        print ("token is created")
        return acces_token
    else:
        print ("not our user")
    
def simulate_protected_request(token:str):
    """
    Симуляция запроса к защищенному эндпоинту
    
    Цепочка:
    1. Клиент отправляет токен
    2. Проверяем токен через verify_token
    3. Если валидный → доступ разрешен
    """
    payload  = verify_token(token)
    if payload:
        print(f"✓ Доступ разрешен для: {payload['username']}")
        print (payload)
        return True
    else:
        print("✗ Доступ запрещен")
        return False

if __name__ == "__main__":
    user =  simulate_registration("nikita","123")
    token = simulate_login("nikita", "123", user["hashed_password"])
    if token:
         print (simulate_protected_request(token))
    
    
