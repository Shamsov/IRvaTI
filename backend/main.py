"""
Точка входа приложения FastAPI «Банк лекций».

Регистрирует все роутеры, настраивает CORS, создаёт таблицы
и директорию для загрузок при старте.
"""

import logging
from contextlib import asynccontextmanager
from pathlib import Path
from typing import AsyncGenerator

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse

from config import settings
from database import Base, engine

# Импорт моделей необходим для регистрации их в метаданных Base
from models.user import User        # noqa: F401
from models.subject import Subject  # noqa: F401
from models.lecture import Lecture  # noqa: F401

from routers import auth, subjects, lectures, ai_search

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s | %(levelname)-8s | %(name)s | %(message)s",
)
logger = logging.getLogger(__name__)


@asynccontextmanager
async def lifespan(app: FastAPI) -> AsyncGenerator[None, None]:
    """
    Обработчик жизненного цикла приложения.
    Выполняется при старте и остановке сервера.
    """
    # --- Запуск ---
    # Создаём директорию для хранения загруженных файлов
    upload_dir = Path(settings.UPLOAD_DIR)
    upload_dir.mkdir(parents=True, exist_ok=True)
    logger.info("Директория загрузок: %s", upload_dir.resolve())

    # Создаём таблицы в базе данных (если они ещё не существуют)
    Base.metadata.create_all(bind=engine)
    logger.info("Таблицы базы данных созданы (или уже существуют).")

    logger.info("Приложение 'Банк лекций' запущено.")
    yield

    # --- Остановка ---
    logger.info("Приложение 'Банк лекций' остановлено.")


# Создание экземпляра FastAPI
app = FastAPI(
    title="Банк лекций",
    description=(
        "API для хранения и поиска лекционных материалов. "
        "Поддерживает загрузку PDF/DOCX/TXT-файлов и AI-поиск на основе LangChain + FAISS."
    ),
    version="1.0.0",
    docs_url="/docs",
    redoc_url="/redoc",
    lifespan=lifespan,
)

# --- CORS ---
# В продакшене замените allow_origins на конкретный список доменов
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# --- Регистрация роутеров ---
app.include_router(auth.router)
app.include_router(subjects.router)
app.include_router(lectures.router)
app.include_router(ai_search.router)


# --- Корневой эндпоинт ---
@app.get("/", tags=["Информация"], summary="Информация о приложении")
def root() -> JSONResponse:
    """Возвращает базовую информацию о запущенном API."""
    return JSONResponse(
        content={
            "app": "Банк лекций",
            "version": "1.0.0",
            "status": "running",
            "docs": "/docs",
            "redoc": "/redoc",
            "endpoints": {
                "auth": "/api/auth",
                "subjects": "/api/subjects",
                "lectures": "/api/lectures",
                "ai_search": "/api/ai/search",
            },
        }
    )


@app.get("/health", tags=["Информация"], summary="Проверка работоспособности")
def health_check() -> JSONResponse:
    """Эндпоинт для мониторинга — возвращает статус 200 при нормальной работе."""
    return JSONResponse(content={"status": "ok"})
