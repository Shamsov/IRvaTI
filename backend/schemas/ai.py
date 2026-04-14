"""
Pydantic-схемы для AI-поиска по лекциям.
"""

from typing import Optional, List
from pydantic import BaseModel, Field


class AIQuery(BaseModel):
    """Схема запроса к AI-поиску."""

    question: str = Field(
        ...,
        min_length=3,
        max_length=1000,
        description="Вопрос для AI-поиска",
    )
    subject_id: Optional[int] = Field(
        None,
        gt=0,
        description="ID предмета для фильтрации поиска (необязательно)",
    )


class AISource(BaseModel):
    """Один источник из результатов AI-поиска."""

    lecture_title: str = Field(..., description="Название лекции-источника")
    excerpt: str = Field(..., description="Релевантный фрагмент текста")


class AIResponse(BaseModel):
    """Схема ответа AI-поиска."""

    answer: str = Field(..., description="Ответ, сгенерированный языковой моделью")
    sources: List[AISource] = Field(
        default_factory=list,
        description="Список источников, использованных при генерации ответа",
    )
