"""
Роутер AI-поиска по лекциям.
Выполняет семантический поиск и генерирует ответы с помощью LLM.
"""

from fastapi import APIRouter, Depends, HTTPException, status

from middleware.auth_middleware import get_current_user
from models.user import User
from schemas.ai import AIQuery, AIResponse, AISource
from services.ai_service import ai_service

router = APIRouter(prefix="/api/ai", tags=["AI-поиск"])


@router.post(
    "/search",
    response_model=AIResponse,
    summary="AI-поиск по лекциям",
)
def ai_search(
    query: AIQuery,
    _: User = Depends(get_current_user),
) -> AIResponse:
    """
    Выполняет семантический поиск по базе лекций и генерирует ответ.

    - При наличии OPENAI_API_KEY использует векторный поиск + GPT.
    - При отсутствии ключа выполняет полнотекстовый поиск.
    - Опциональный параметр subject_id ограничивает поиск одним предметом.
    """
    if not query.question.strip():
        raise HTTPException(
            status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
            detail="Вопрос не может быть пустым",
        )

    # Семантический (или полнотекстовый) поиск релевантных фрагментов
    search_results = ai_service.search(
        query=query.question,
        subject_id=query.subject_id,
        top_k=5,
    )

    # Генерация ответа на основе найденных фрагментов
    answer = ai_service.generate_answer(
        query=query.question,
        context_chunks=search_results,
    )

    # Формируем список источников для ответа
    sources = [
        AISource(
            lecture_title=result.lecture_title,
            excerpt=result.excerpt,
        )
        for result in search_results
    ]

    return AIResponse(answer=answer, sources=sources)
