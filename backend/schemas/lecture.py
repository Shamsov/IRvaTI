"""
Pydantic-схемы для лекций.
"""

from datetime import datetime
from typing import Optional
from pydantic import BaseModel, Field


class LectureUpload(BaseModel):
    """Схема метаданных при загрузке лекции (форма multipart)."""

    title: str = Field(..., min_length=2, max_length=255, description="Название лекции")
    subject_id: int = Field(..., gt=0, description="ID предмета, к которому относится лекция")


class LectureResponse(BaseModel):
    """Схема ответа с данными лекции."""

    id: int
    title: str
    subject_id: int
    file_path: Optional[str]
    created_at: datetime

    class Config:
        from_attributes = True


class LectureContentResponse(BaseModel):
    """Схема ответа с текстовым содержимым лекции."""

    id: int
    title: str
    subject_id: int
    text_content: Optional[str]
    created_at: datetime

    class Config:
        from_attributes = True
