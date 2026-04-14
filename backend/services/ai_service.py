"""
AI-сервис семантического поиска по лекциям.
Использует OpenAI Embeddings + FAISS для векторного поиска
и GPT для генерации ответов на основе найденных фрагментов.

При отсутствии OPENAI_API_KEY сервис деградирует до полнотекстового поиска.
"""

import logging
import pickle
from dataclasses import dataclass, field
from pathlib import Path
from typing import Dict, List, Optional, Tuple

from config import settings

logger = logging.getLogger(__name__)


@dataclass
class ChunkMeta:
    """Метаданные одного текстового чанка в индексе."""

    lecture_id: int
    lecture_title: str
    subject_id: int
    text: str


@dataclass
class SearchResult:
    """Результат семантического поиска."""

    lecture_id: int
    lecture_title: str
    subject_id: int
    excerpt: str
    score: float


class AIService:
    """
    Сервис AI-поиска на основе FAISS и OpenAI.

    При наличии OPENAI_API_KEY:
        - Эмбеддинги генерируются через OpenAI Embeddings.
        - Ответ формируется GPT-моделью.

    При отсутствии OPENAI_API_KEY:
        - Выполняется простой полнотекстовый поиск подстрок.
        - Ответ формируется конкатенацией найденных фрагментов.
    """

    # Путь для сохранения/восстановления индекса между перезапусками
    _INDEX_PATH = Path("faiss_index.pkl")

    def __init__(self) -> None:
        self._ai_available: bool = bool(settings.OPENAI_API_KEY)

        # Хранилище метаданных чанков: индекс FAISS → ChunkMeta
        self._chunks: List[ChunkMeta] = []

        # FAISS-индекс (None, если AI недоступен или ещё не инициализирован)
        self._index = None
        self._embeddings = None
        self._llm = None

        if self._ai_available:
            self._init_ai_components()
        else:
            logger.warning(
                "OPENAI_API_KEY не задан. AI-поиск работает в режиме полнотекстового поиска."
            )

    def _init_ai_components(self) -> None:
        """Инициализирует компоненты OpenAI и FAISS."""
        try:
            from langchain_openai import OpenAIEmbeddings, ChatOpenAI

            self._embeddings = OpenAIEmbeddings(
                openai_api_key=settings.OPENAI_API_KEY,
                model="text-embedding-ada-002",
            )
            self._llm = ChatOpenAI(
                openai_api_key=settings.OPENAI_API_KEY,
                model_name="gpt-3.5-turbo",
                temperature=0.3,
                max_tokens=1024,
            )

            # Восстанавливаем сохранённый индекс, если он существует
            self._load_index()
            logger.info("AI-компоненты успешно инициализированы.")
        except Exception as exc:
            logger.error("Ошибка инициализации AI-компонентов: %s", exc)
            self._ai_available = False

    def _load_index(self) -> None:
        """Загружает FAISS-индекс и метаданные с диска (если файл существует)."""
        if not self._INDEX_PATH.exists():
            return
        try:
            import faiss

            with open(self._INDEX_PATH, "rb") as f:
                saved = pickle.load(f)
            self._index = saved["index"]
            self._chunks = saved["chunks"]
            logger.info(
                "Загружен FAISS-индекс: %d чанков.", len(self._chunks)
            )
        except Exception as exc:
            logger.warning("Не удалось загрузить FAISS-индекс: %s", exc)

    def _save_index(self) -> None:
        """Сохраняет FAISS-индекс и метаданные на диск."""
        if self._index is None:
            return
        try:
            with open(self._INDEX_PATH, "wb") as f:
                pickle.dump({"index": self._index, "chunks": self._chunks}, f)
        except Exception as exc:
            logger.warning("Не удалось сохранить FAISS-индекс: %s", exc)

    def add_lecture_to_index(
        self,
        lecture_id: int,
        lecture_title: str,
        subject_id: int,
        chunks: List[str],
    ) -> None:
        """
        Добавляет чанки лекции в векторный индекс.

        Аргументы:
            lecture_id: ID лекции в базе данных.
            lecture_title: Название лекции.
            subject_id: ID предмета лекции.
            chunks: Список текстовых чанков для индексирования.
        """
        if not chunks:
            return

        if not self._ai_available:
            # В режиме полнотекстового поиска просто сохраняем чанки
            for chunk_text in chunks:
                self._chunks.append(
                    ChunkMeta(
                        lecture_id=lecture_id,
                        lecture_title=lecture_title,
                        subject_id=subject_id,
                        text=chunk_text,
                    )
                )
            return

        try:
            import numpy as np
            import faiss

            # Генерируем эмбеддинги для всех чанков
            vectors = self._embeddings.embed_documents(chunks)
            matrix = np.array(vectors, dtype="float32")

            # При первом добавлении создаём IndexFlatL2 с нужной размерностью
            if self._index is None:
                dim = matrix.shape[1]
                self._index = faiss.IndexFlatL2(dim)
            else:
                # Проверяем совместимость размерности векторов с существующим индексом
                if matrix.shape[1] != self._index.d:
                    raise ValueError(
                        f"Несовместимая размерность векторов: ожидалось {self._index.d}, "
                        f"получено {matrix.shape[1]}. Возможно, изменилась модель эмбеддингов."
                    )

            self._index.add(matrix)

            for chunk_text in chunks:
                self._chunks.append(
                    ChunkMeta(
                        lecture_id=lecture_id,
                        lecture_title=lecture_title,
                        subject_id=subject_id,
                        text=chunk_text,
                    )
                )

            self._save_index()
            logger.info(
                "В индекс добавлено %d чанков лекции '%s'.", len(chunks), lecture_title
            )
        except Exception as exc:
            logger.error("Ошибка добавления лекции в индекс: %s", exc)

    def search(
        self,
        query: str,
        subject_id: Optional[int] = None,
        top_k: int = 5,
    ) -> List[SearchResult]:
        """
        Семантический (или полнотекстовый) поиск по индексу.

        Аргументы:
            query: Текст поискового запроса.
            subject_id: Фильтр по предмету (None — искать по всем предметам).
            top_k: Количество возвращаемых результатов.

        Возвращает:
            Список SearchResult, отсортированный по релевантности.
        """
        if not self._chunks:
            return []

        if not self._ai_available or self._index is None:
            return self._fulltext_search(query, subject_id, top_k)

        try:
            import numpy as np

            # Эмбеддинг запроса
            query_vector = np.array(
                [self._embeddings.embed_query(query)], dtype="float32"
            )

            # Поиск top_k * 3 кандидатов для последующей фильтрации по subject_id
            k = min(top_k * 3, len(self._chunks))
            distances, indices = self._index.search(query_vector, k)

            results: List[SearchResult] = []
            for dist, idx in zip(distances[0], indices[0]):
                if idx < 0 or idx >= len(self._chunks):
                    continue
                meta = self._chunks[idx]
                if subject_id is not None and meta.subject_id != subject_id:
                    continue
                results.append(
                    SearchResult(
                        lecture_id=meta.lecture_id,
                        lecture_title=meta.lecture_title,
                        subject_id=meta.subject_id,
                        excerpt=meta.text[:500],
                        score=float(dist),
                    )
                )
                if len(results) >= top_k:
                    break

            return results
        except Exception as exc:
            logger.error("Ошибка векторного поиска: %s", exc)
            return self._fulltext_search(query, subject_id, top_k)

    def _fulltext_search(
        self,
        query: str,
        subject_id: Optional[int],
        top_k: int,
    ) -> List[SearchResult]:
        """
        Запасной полнотекстовый поиск подстрок.
        Используется когда AI недоступен или возникла ошибка.
        """
        query_lower = query.lower()
        query_words = query_lower.split()
        scored: List[Tuple[float, ChunkMeta]] = []

        for meta in self._chunks:
            if subject_id is not None and meta.subject_id != subject_id:
                continue
            text_lower = meta.text.lower()
            # Оценка = количество совпавших слов запроса в тексте
            score = sum(1 for w in query_words if w in text_lower)
            if score > 0:
                scored.append((score, meta))

        # Сортируем по убыванию оценки
        scored.sort(key=lambda x: x[0], reverse=True)

        return [
            SearchResult(
                lecture_id=meta.lecture_id,
                lecture_title=meta.lecture_title,
                subject_id=meta.subject_id,
                excerpt=meta.text[:500],
                score=score,
            )
            for score, meta in scored[:top_k]
        ]

    def generate_answer(
        self,
        query: str,
        context_chunks: List[SearchResult],
    ) -> str:
        """
        Генерирует ответ на основе найденных фрагментов лекций.

        При наличии LLM использует GPT для формулировки ответа.
        При его отсутствии возвращает агрегированные фрагменты.

        Аргументы:
            query: Исходный вопрос пользователя.
            context_chunks: Список релевантных фрагментов из поиска.

        Возвращает:
            Строку с ответом.
        """
        if not context_chunks:
            return (
                "По вашему запросу ничего не найдено. "
                "Попробуйте переформулировать вопрос или выбрать другой предмет."
            )

        # Формируем контекст из найденных фрагментов
        context_text = "\n\n---\n\n".join(
            f"[{r.lecture_title}]\n{r.excerpt}" for r in context_chunks
        )

        if not self._ai_available or self._llm is None:
            # Режим без LLM: возвращаем сводку найденных фрагментов
            titles = list(dict.fromkeys(r.lecture_title for r in context_chunks))
            return (
                f"Найдены релевантные фрагменты в лекциях: {', '.join(titles)}.\n\n"
                f"Наиболее подходящий фрагмент:\n\n{context_chunks[0].excerpt}"
            )

        try:
            from langchain.schema import HumanMessage, SystemMessage

            system_prompt = (
                "Ты — помощник-преподаватель. Отвечай на вопросы студентов "
                "строго на основе предоставленных фрагментов лекций. "
                "Если информации недостаточно, честно скажи об этом. "
                "Отвечай на русском языке."
            )
            user_prompt = (
                f"Контекст из лекций:\n{context_text}\n\n"
                f"Вопрос студента: {query}\n\n"
                "Дай чёткий и полный ответ на основе контекста выше."
            )

            response = self._llm(
                [
                    SystemMessage(content=system_prompt),
                    HumanMessage(content=user_prompt),
                ]
            )
            return response.content
        except Exception as exc:
            logger.error("Ошибка генерации ответа LLM: %s", exc)
            return (
                f"Найдены релевантные фрагменты. "
                f"Наиболее подходящий:\n\n{context_chunks[0].excerpt}"
            )

    def remove_lecture_from_index(self, lecture_id: int) -> None:
        """
        Удаляет все чанки указанной лекции из метаданных.

        Примечание: FAISS IndexFlatL2 не поддерживает удаление векторов.
        Чанки удаляются только из списка метаданных. Чтобы полностью убрать
        устаревшие векторы из индекса, необходимо пересоздать его вручную:
        заново вызвать add_lecture_to_index() для всех оставшихся лекций
        после сброса self._index = None и self._chunks = [].
        """
        self._chunks = [c for c in self._chunks if c.lecture_id != lecture_id]
        logger.info("Чанки лекции %d удалены из метаданных индекса.", lecture_id)


# Глобальный экземпляр AI-сервиса
ai_service = AIService()
