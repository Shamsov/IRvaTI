"""
Pydantic-схемы для пользователей: регистрация, вход, ответы и токены.
"""

from datetime import datetime
from typing import Optional
from pydantic import BaseModel, EmailStr, Field

from models.user import UserRole


class UserCreate(BaseModel):
    """Схема для регистрации нового пользователя."""

    name: str = Field(..., min_length=2, max_length=255, description="Имя пользователя")
    email: EmailStr = Field(..., description="Электронная почта")
    password: str = Field(..., min_length=6, description="Пароль (минимум 6 символов)")
    role: UserRole = Field(default=UserRole.student, description="Роль пользователя")


class UserLogin(BaseModel):
    """Схема для входа пользователя."""

    email: EmailStr = Field(..., description="Электронная почта")
    password: str = Field(..., description="Пароль")


class UserResponse(BaseModel):
    """Схема ответа с данными пользователя (без пароля)."""

    id: int
    name: str
    email: str
    role: UserRole
    created_at: datetime

    class Config:
        from_attributes = True


class Token(BaseModel):
    """Схема JWT-токена, возвращаемого при успешной аутентификации."""

    access_token: str = Field(..., description="JWT access-токен")
    token_type: str = Field(default="bearer", description="Тип токена")
    user: UserResponse = Field(..., description="Данные аутентифицированного пользователя")


class TokenData(BaseModel):
    """Данные, хранящиеся внутри JWT-токена."""

    user_id: Optional[int] = None
    email: Optional[str] = None
    role: Optional[str] = None
