"""
Сервис аутентификации: хеширование паролей и работа с JWT-токенами.
"""

from datetime import datetime, timedelta, timezone
from typing import Optional

from jose import JWTError, jwt
from passlib.context import CryptContext

from config import settings
from schemas.user import TokenData

# Контекст хеширования паролей с использованием bcrypt
_pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")


def hash_password(plain_password: str) -> str:
    """Хеширует пароль с помощью bcrypt."""
    return _pwd_context.hash(plain_password)


def verify_password(plain_password: str, hashed_password: str) -> bool:
    """Проверяет соответствие открытого пароля его хешу."""
    return _pwd_context.verify(plain_password, hashed_password)


def create_access_token(
    user_id: int,
    email: str,
    role: str,
    expires_delta: Optional[timedelta] = None,
) -> str:
    """
    Создаёт подписанный JWT access-токен.

    Аргументы:
        user_id: Идентификатор пользователя.
        email: Электронная почта пользователя.
        role: Роль пользователя в системе.
        expires_delta: Срок действия токена (по умолчанию из настроек).

    Возвращает:
        Строку с JWT-токеном.
    """
    if expires_delta is None:
        expires_delta = timedelta(minutes=settings.ACCESS_TOKEN_EXPIRE_MINUTES)

    expire = datetime.now(timezone.utc) + expires_delta

    payload = {
        "sub": str(user_id),
        "email": email,
        "role": role,
        "exp": expire,
        "iat": datetime.now(timezone.utc),
    }

    return jwt.encode(payload, settings.SECRET_KEY, algorithm=settings.ALGORITHM)


def decode_access_token(token: str) -> Optional[TokenData]:
    """
    Декодирует и валидирует JWT-токен.

    Возвращает объект TokenData при успехе или None при ошибке.
    """
    try:
        payload = jwt.decode(
            token,
            settings.SECRET_KEY,
            algorithms=[settings.ALGORITHM],
        )
        user_id_str: Optional[str] = payload.get("sub")
        email: Optional[str] = payload.get("email")
        role: Optional[str] = payload.get("role")

        if user_id_str is None or email is None:
            return None

        return TokenData(
            user_id=int(user_id_str),
            email=email,
            role=role,
        )
    except JWTError:
        return None
