from argon2 import PasswordHasher
from argon2.exceptions import VerifyMismatchError

"""
hash_password(). - делает из пароля хэш
verify_password() - (пароль , хэшируемый пароль)
 - вызывает verify_password() и сверяет
 

"""

ph = PasswordHasher()

def hash_password(password:str):
    return ph.hash(password)

def verify_password(password:str, hashed_password: str):
    try:
        ph.verify(hashed_password,password)
        return True
    except VerifyMismatchError:
        return False


if __name__=="__main__":
    password = "something"
    hashed = hash_password(password)
    print (hashed)
    is_valid = verify_password(password,hashed)
    print (is_valid)


    