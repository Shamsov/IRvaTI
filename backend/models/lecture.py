"""
Модель лекции.
Хранит путь к файлу и извлечённый текстовый контент лекции.
"""

from datetime import datetime
from sqlalchemy import Column, Integer, String, Text, DateTime, ForeignKey
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func

from database import Base


class Lecture(Base):
    """ORM-модель лекции."""

    __tablename__ = "lectures"

    id: int = Column(Integer, primary_key=True, index=True)
    title: str = Column(String(255), nullable=False, index=True)
    subject_id: int = Column(
        Integer,
        ForeignKey("subjects.id", ondelete="CASCADE"),
        nullable=False,
        index=True,
    )
    file_path: str = Column(String(512), nullable=True)   # Путь к загруженному файлу
    text_content: str = Column(Text, nullable=True)        # Извлечённый текст лекции
    created_at: datetime = Column(
        DateTime(timezone=True),
        server_default=func.now(),
        nullable=False,
    )

    # Обратная связь с предметом
    subject = relationship("Subject", back_populates="lectures")
