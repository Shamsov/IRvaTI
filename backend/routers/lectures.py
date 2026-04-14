"""
Роутер лекций.
Загрузка файлов, просмотр содержимого и удаление лекций.
"""

import logging
import os
import uuid
from pathlib import Path
from typing import List

from fastapi import (
    APIRouter,
    Depends,
    File,
    Form,
    HTTPException,
    UploadFile,
    status,
)
from sqlalchemy.orm import Session

from config import settings
from database import get_db
from middleware.auth_middleware import get_current_user, require_admin
from models.lecture import Lecture
from models.subject import Subject
from models.user import User
from schemas.lecture import LectureContentResponse, LectureResponse
from services.ai_service import ai_service
from services.file_processor import process_file, split_text_into_chunks

router = APIRouter(prefix="/api/lectures", tags=["Лекции"])

# Разрешённые расширения файлов
_ALLOWED_EXTENSIONS = {".pdf", ".docx", ".txt"}


@router.get(
    "/{subject_id}",
    response_model=List[LectureResponse],
    summary="Список лекций предмета",
)
def list_lectures(
    subject_id: int,
    db: Session = Depends(get_db),
    _: User = Depends(get_current_user),
) -> List[Lecture]:
    """Возвращает все лекции для указанного предмета."""
    subject = db.query(Subject).filter(Subject.id == subject_id).first()
    if subject is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Предмет с ID {subject_id} не найден",
        )

    return (
        db.query(Lecture)
        .filter(Lecture.subject_id == subject_id)
        .order_by(Lecture.created_at.desc())
        .all()
    )


@router.post(
    "/upload",
    response_model=LectureResponse,
    status_code=status.HTTP_201_CREATED,
    summary="Загрузить лекцию (только администратор)",
)
async def upload_lecture(
    title: str = Form(..., description="Название лекции"),
    subject_id: int = Form(..., description="ID предмета"),
    file: UploadFile = File(..., description="Файл лекции (PDF, DOCX или TXT)"),
    db: Session = Depends(get_db),
    _: User = Depends(require_admin),
) -> Lecture:
    """
    Загружает файл лекции, извлекает текст и добавляет в AI-индекс.
    Поддерживаемые форматы: .pdf, .docx, .txt.
    """
    # Проверяем существование предмета
    subject = db.query(Subject).filter(Subject.id == subject_id).first()
    if subject is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Предмет с ID {subject_id} не найден",
        )

    # Валидируем расширение файла
    original_name = file.filename or "unknown"
    suffix = Path(original_name).suffix.lower()
    if suffix not in _ALLOWED_EXTENSIONS:
        raise HTTPException(
            status_code=status.HTTP_415_UNSUPPORTED_MEDIA_TYPE,
            detail=(
                f"Формат файла '{suffix}' не поддерживается. "
                f"Допустимые форматы: {', '.join(_ALLOWED_EXTENSIONS)}"
            ),
        )

    # Читаем содержимое и проверяем размер
    content = await file.read()
    if len(content) > settings.MAX_FILE_SIZE:
        raise HTTPException(
            status_code=status.HTTP_413_REQUEST_ENTITY_TOO_LARGE,
            detail=(
                f"Размер файла превышает допустимый предел "
                f"({settings.MAX_FILE_SIZE // (1024 * 1024)} МБ)"
            ),
        )

    # Сохраняем файл с уникальным именем
    upload_dir = Path(settings.UPLOAD_DIR)
    upload_dir.mkdir(parents=True, exist_ok=True)
    unique_name = f"{uuid.uuid4()}{suffix}"
    file_path = upload_dir / unique_name

    with open(file_path, "wb") as f_out:
        f_out.write(content)

    # Извлекаем текст из файла
    try:
        text_content = process_file(str(file_path), suffix)
    except Exception as exc:
        # При ошибке обработки удаляем загруженный файл
        file_path.unlink(missing_ok=True)
        raise HTTPException(
            status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
            detail=f"Не удалось обработать файл: {exc}",
        )

    # Сохраняем запись в базе данных
    lecture = Lecture(
        title=title,
        subject_id=subject_id,
        file_path=str(file_path),
        text_content=text_content,
    )
    db.add(lecture)
    db.commit()
    db.refresh(lecture)

    # Асинхронно добавляем лекцию в AI-индекс
    try:
        chunks = split_text_into_chunks(text_content)
        ai_service.add_lecture_to_index(
            lecture_id=lecture.id,
            lecture_title=lecture.title,
            subject_id=lecture.subject_id,
            chunks=chunks,
        )
    except Exception as exc:
        # Ошибка индексирования не критична — лекция уже сохранена
        logging.getLogger(__name__).warning(
            "Не удалось добавить лекцию %d в AI-индекс: %s", lecture.id, exc
        )

    return lecture


@router.get(
    "/{lecture_id}/content",
    response_model=LectureContentResponse,
    summary="Получить текстовое содержимое лекции",
)
def get_lecture_content(
    lecture_id: int,
    db: Session = Depends(get_db),
    _: User = Depends(get_current_user),
) -> Lecture:
    """Возвращает полное текстовое содержимое лекции."""
    lecture = db.query(Lecture).filter(Lecture.id == lecture_id).first()
    if lecture is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Лекция с ID {lecture_id} не найдена",
        )
    return lecture


@router.delete(
    "/{lecture_id}",
    status_code=status.HTTP_204_NO_CONTENT,
    summary="Удалить лекцию (только администратор)",
)
def delete_lecture(
    lecture_id: int,
    db: Session = Depends(get_db),
    _: User = Depends(require_admin),
) -> None:
    """
    Удаляет лекцию из базы данных, файловой системы и AI-индекса.
    """
    lecture = db.query(Lecture).filter(Lecture.id == lecture_id).first()
    if lecture is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Лекция с ID {lecture_id} не найдена",
        )

    # Удаляем файл с диска
    if lecture.file_path:
        try:
            Path(lecture.file_path).unlink(missing_ok=True)
        except OSError:
            pass

    # Удаляем чанки из AI-индекса
    ai_service.remove_lecture_from_index(lecture_id)

    db.delete(lecture)
    db.commit()
