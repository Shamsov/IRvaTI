"""
Настройка подключения к базе данных через SQLAlchemy.
Предоставляет движок, фабрику сессий и базовый класс моделей.
"""

from sqlalchemy import create_engine
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker, Session
from typing import Generator

from config import settings

# Создание движка базы данных
engine = create_engine(
    settings.DATABASE_URL,
    pool_pre_ping=True,       # Проверка соединения перед использованием
    pool_size=10,              # Размер пула соединений
    max_overflow=20,           # Максимальное количество дополнительных соединений
    echo=False,                # Отключить логирование SQL-запросов в продакшене
)

# Фабрика сессий
SessionLocal = sessionmaker(
    autocommit=False,
    autoflush=False,
    bind=engine,
)

# Базовый класс для всех ORM-моделей
Base = declarative_base()


def get_db() -> Generator[Session, None, None]:
    """
    Зависимость FastAPI для получения сессии базы данных.
    Автоматически закрывает сессию после завершения запроса.
    """
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
