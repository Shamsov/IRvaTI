"""
Модель учебного предмета (дисциплины).
Предмет объединяет список лекций по одной теме.
"""

from datetime import datetime
from sqlalchemy import Column, Integer, String, Text, DateTime
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func

from database import Base


class Subject(Base):
    """ORM-модель предмета."""

    __tablename__ = "subjects"

    id: int = Column(Integer, primary_key=True, index=True)
    name: str = Column(String(255), unique=True, nullable=False, index=True)
    description: str = Column(Text, nullable=True)
    created_at: datetime = Column(
        DateTime(timezone=True),
        server_default=func.now(),
        nullable=False,
    )

    # Связь один-ко-многим с лекциями
    lectures = relationship(
        "Lecture",
        back_populates="subject",
        cascade="all, delete-orphan",  # Удаление предмета удаляет его лекции
        lazy="select",
    )
