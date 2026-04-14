"""
Pydantic-схемы для учебных предметов.
"""

from datetime import datetime
from typing import Optional
from pydantic import BaseModel, Field


class SubjectCreate(BaseModel):
    """Схема для создания нового предмета."""

    name: str = Field(..., min_length=2, max_length=255, description="Название предмета")
    description: Optional[str] = Field(None, description="Описание предмета")


class SubjectResponse(BaseModel):
    """Схема ответа с данными предмета."""

    id: int
    name: str
    description: Optional[str]
    created_at: datetime

    class Config:
        from_attributes = True
