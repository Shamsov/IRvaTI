"""
Роутер учебных предметов.
Публичный список предметов, создание и удаление — только для администраторов.
"""

from typing import List

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session

from database import get_db
from middleware.auth_middleware import require_admin
from models.subject import Subject
from models.user import User
from schemas.subject import SubjectCreate, SubjectResponse

router = APIRouter(prefix="/api/subjects", tags=["Предметы"])


@router.get(
    "",
    response_model=List[SubjectResponse],
    summary="Список всех предметов",
)
def list_subjects(db: Session = Depends(get_db)) -> List[Subject]:
    """Возвращает все учебные предметы, отсортированные по названию."""
    return db.query(Subject).order_by(Subject.name).all()


@router.post(
    "",
    response_model=SubjectResponse,
    status_code=status.HTTP_201_CREATED,
    summary="Создать предмет (только администратор)",
)
def create_subject(
    subject_data: SubjectCreate,
    db: Session = Depends(get_db),
    _: User = Depends(require_admin),
) -> Subject:
    """
    Создаёт новый учебный предмет.
    Доступно только пользователям с ролью admin.
    """
    existing = db.query(Subject).filter(Subject.name == subject_data.name).first()
    if existing:
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail=f"Предмет с названием '{subject_data.name}' уже существует",
        )

    subject = Subject(
        name=subject_data.name,
        description=subject_data.description,
    )
    db.add(subject)
    db.commit()
    db.refresh(subject)

    return subject


@router.delete(
    "/{subject_id}",
    status_code=status.HTTP_204_NO_CONTENT,
    summary="Удалить предмет (только администратор)",
)
def delete_subject(
    subject_id: int,
    db: Session = Depends(get_db),
    _: User = Depends(require_admin),
) -> None:
    """
    Удаляет предмет и все его лекции (каскадное удаление).
    Доступно только пользователям с ролью admin.
    """
    subject = db.query(Subject).filter(Subject.id == subject_id).first()
    if subject is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Предмет с ID {subject_id} не найден",
        )

    db.delete(subject)
    db.commit()
