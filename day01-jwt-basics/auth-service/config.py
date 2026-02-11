from pydantic_settings import BaseSettings,SettingsConfigDict
from dotenv import load_dotenv
from pydantic import Field


class Settings(BaseSettings):
    DATABASE_URL: str
    JWT_SECRET_KEY: str
    JWT_ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 60
    

    # Вместо class Config используем model_config
    # Прописываем  где будут искаться наши файлы
    model_config = SettingsConfigDict(env_file=".env")


class Settings2(BaseSettings):
    MyPhoneNumber :str =  Field(alias="telephone")

    model_config = SettingsConfigDict(env_file="try.env")


settings = Settings()  # type: ignore[call-arg]

s = Settings2()# type: ignore[call-arg]

print (s.MyPhoneNumber)