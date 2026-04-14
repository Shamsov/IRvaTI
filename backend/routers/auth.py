"""
Роутер аутентификации.
Обрабатывает регистрацию и вход пользователей.
"""

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session

from database import get_db
from models.user import User
from schemas.user import UserCreate, UserLogin, UserResponse, Token
from services.auth_service import hash_password, verify_password, create_access_token

router = APIRouter(prefix="/api/auth", tags=["Аутентификация"])


@router.post(
    "/register",
    response_model=UserResponse,
    status_code=status.HTTP_201_CREATED,
    summary="Регистрация нового пользователя",
)
def register(
    user_data: UserCreate,
    db: Session = Depends(get_db),
) -> User:
    """
    Регистрирует нового пользователя.

    - Проверяет уникальность email.
    - Хеширует пароль перед сохранением.
    """
    # Проверяем, не занят ли email
    existing = db.query(User).filter(User.email == user_data.email).first()
    if existing:
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail=f"Пользователь с email '{user_data.email}' уже существует",
        )

    new_user = User(
        name=user_data.name,
        email=user_data.email,
        hashed_password=hash_password(user_data.password),
        role=user_data.role,
    )
    db.add(new_user)
    db.commit()
    db.refresh(new_user)

    return new_user


@router.post(
    "/login",
    response_model=Token,
    summary="Вход в систему",
)
def login(
    credentials: UserLogin,
    db: Session = Depends(get_db),
) -> Token:
    """
    Аутентифицирует пользователя и возвращает JWT-токен.
    """
    user = db.query(User).filter(User.email == credentials.email).first()

    if user is None or not verify_password(credentials.password, user.hashed_password):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Неверный email или пароль",
            headers={"WWW-Authenticate": "Bearer"},
        )

    access_token = create_access_token(
        user_id=user.id,
        email=user.email,
        role=user.role.value,
    )

    return Token(
        access_token=access_token,
        token_type="bearer",
        user=UserResponse.model_validate(user),
    )
