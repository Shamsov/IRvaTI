"""
Сервис обработки файлов лекций.
Поддерживает форматы: PDF, DOCX, TXT.
Разбивает текст на чанки для векторного индексирования.
"""

import logging
from pathlib import Path
from typing import List

logger = logging.getLogger(__name__)


def extract_text_from_pdf(file_path: str) -> str:
    """
    Извлекает текст из PDF-файла с помощью PyPDF2.

    Аргументы:
        file_path: Путь к PDF-файлу.

    Возвращает:
        Извлечённый текст (строка).
    """
    import PyPDF2

    text_parts: List[str] = []
    with open(file_path, "rb") as f:
        reader = PyPDF2.PdfReader(f)
        for page in reader.pages:
            page_text = page.extract_text()
            if page_text:
                text_parts.append(page_text)

    return "\n".join(text_parts)


def extract_text_from_docx(file_path: str) -> str:
    """
    Извлекает текст из DOCX-файла с помощью python-docx.

    Аргументы:
        file_path: Путь к DOCX-файлу.

    Возвращает:
        Извлечённый текст (строка).
    """
    from docx import Document

    doc = Document(file_path)
    paragraphs = [para.text for para in doc.paragraphs if para.text.strip()]
    return "\n".join(paragraphs)


def extract_text_from_txt(file_path: str) -> str:
    """
    Читает текст из TXT-файла с автоопределением кодировки.

    Аргументы:
        file_path: Путь к TXT-файлу.

    Возвращает:
        Содержимое файла (строка).
    """
    # Пробуем UTF-8, затем latin-1 как запасной вариант
    for encoding in ("utf-8", "utf-8-sig", "latin-1"):
        try:
            with open(file_path, "r", encoding=encoding) as f:
                return f.read()
        except UnicodeDecodeError:
            continue

    # Если ни одна кодировка не подошла — читаем с заменой ошибок
    with open(file_path, "r", encoding="utf-8", errors="replace") as f:
        return f.read()


def process_file(file_path: str, file_extension: str) -> str:
    """
    Определяет тип файла и вызывает соответствующий экстрактор текста.

    Аргументы:
        file_path: Путь к файлу.
        file_extension: Расширение файла (например, '.pdf', '.docx', '.txt').

    Возвращает:
        Извлечённый текст.

    Вызывает:
        ValueError: Если формат файла не поддерживается.
    """
    ext = file_extension.lower().lstrip(".")

    extractors = {
        "pdf": extract_text_from_pdf,
        "docx": extract_text_from_docx,
        "txt": extract_text_from_txt,
    }

    if ext not in extractors:
        raise ValueError(
            f"Неподдерживаемый формат файла: .{ext}. "
            f"Допустимые форматы: {', '.join(f'.{e}' for e in extractors)}"
        )

    try:
        text = extractors[ext](file_path)
        logger.info("Извлечено %d символов из файла '%s'", len(text), Path(file_path).name)
        return text
    except Exception as exc:
        logger.error("Ошибка обработки файла '%s': %s", file_path, exc)
        raise


def split_text_into_chunks(
    text: str,
    chunk_size: int = 1000,
    overlap: int = 200,
) -> List[str]:
    """
    Разбивает текст на перекрывающиеся чанки для векторного поиска.

    Алгоритм предпочитает разрыв по символу новой строки,
    чтобы не разрывать предложения посередине.

    Аргументы:
        text: Исходный текст.
        chunk_size: Максимальный размер чанка в символах.
        overlap: Количество символов перекрытия между соседними чанками.

    Возвращает:
        Список непустых текстовых чанков.
    """
    if not text or not text.strip():
        return []

    chunks: List[str] = []
    start = 0
    text_length = len(text)

    while start < text_length:
        end = start + chunk_size

        if end < text_length:
            # Ищем ближайший разрыв строки назад от границы чанка
            newline_pos = text.rfind("\n", start, end)
            if newline_pos > start + chunk_size // 2:
                end = newline_pos + 1
            else:
                # Ищем ближайший пробел, чтобы не рвать слово
                space_pos = text.rfind(" ", start, end)
                if space_pos > start + chunk_size // 2:
                    end = space_pos + 1

        chunk = text[start:end].strip()
        if chunk:
            chunks.append(chunk)

        # Следующий чанк начинается с отступом назад на overlap символов.
        # Гарантируем продвижение вперёд, чтобы исключить бесконечный цикл.
        next_start = end - overlap
        start = next_start if next_start > start else start + 1

    return chunks
