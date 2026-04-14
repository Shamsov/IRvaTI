"""
Модель пользователя системы.
Поддерживает роли: admin (администратор) и student (студент).
"""

from datetime import datetime
from sqlalchemy import Column, Integer, String, DateTime, Enum as SAEnum
from sqlalchemy.sql import func
import enum

from database import Base


class UserRole(str, enum.Enum):
    """Роли пользователей в системе."""
    admin = "admin"
    student = "student"


class User(Base):
    """ORM-модель пользователя."""

    __tablename__ = "users"

    id: int = Column(Integer, primary_key=True, index=True)
    name: str = Column(String(255), nullable=False)
    email: str = Column(String(255), unique=True, index=True, nullable=False)
    hashed_password: str = Column(String(255), nullable=False)
    role: UserRole = Column(
        SAEnum(UserRole, name="userrole"),
        default=UserRole.student,
        nullable=False,
    )
    created_at: datetime = Column(
        DateTime(timezone=True),
        server_default=func.now(),
        nullable=False,
    )
