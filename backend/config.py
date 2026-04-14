"""
Конфигурация приложения через pydantic-settings.
Все параметры загружаются из переменных окружения или файла .env.
"""

from pydantic_settings import BaseSettings
from pydantic import Field


class Settings(BaseSettings):
    """Настройки приложения."""

    # База данных
    DATABASE_URL: str = Field(
        default="postgresql://postgres:postgres@localhost:5432/lecture_bank",
        description="URL подключения к PostgreSQL",
    )

    # JWT аутентификация
    SECRET_KEY: str = Field(
        default="change-this-secret-key-in-production-use-openssl-rand-hex-32",
        description="Секретный ключ для подписи JWT токенов",
    )
    ALGORITHM: str = Field(
        default="HS256",
        description="Алгоритм подписи JWT",
    )
    ACCESS_TOKEN_EXPIRE_MINUTES: int = Field(
        default=60 * 24,  # 24 часа
        description="Время жизни access-токена в минутах",
    )

    # OpenAI
    OPENAI_API_KEY: str = Field(
        default="",
        description="API-ключ OpenAI для AI-поиска",
    )

    # Загрузка файлов
    UPLOAD_DIR: str = Field(
        default="uploads",
        description="Директория для хранения загруженных файлов",
    )
    MAX_FILE_SIZE: int = Field(
        default=50 * 1024 * 1024,  # 50 МБ
        description="Максимальный размер файла в байтах",
    )

    class Config:
        env_file = ".env"
        env_file_encoding = "utf-8"
        case_sensitive = True


# Глобальный объект настроек
settings = Settings()
