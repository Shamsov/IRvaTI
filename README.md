# 📚 Банк лекций — ИИ-приложение для студентов

Мобильное приложение **"Банк лекций"** на базе искусственного интеллекта для студентов.  
Администратор загружает лекции, студенты — быстро находят нужную информацию с помощью ИИ (семантический поиск, RAG).

---

## 🏗️ Архитектура

| Компонент | Технологии |
|-----------|-----------|
| 📱 Мобильное приложение | Kotlin + Jetpack Compose + Material3 |
| ⚙️ Бэкенд | Python + FastAPI + PostgreSQL |
| 🤖 ИИ модуль | LangChain + FAISS + OpenAI API |
| 🔐 Аутентификация | JWT + bcrypt |
| 🐳 Деплой | Docker Compose |

---

## 👥 Роли пользователей

| Роль | Возможности |
|------|-------------|
| **Админ** | Загрузка лекций (PDF, DOCX, TXT), управление предметами, удаление материалов |
| **Студент** | ИИ-поиск по материалам, просмотр лекций, список предметов |

---

## 🤖 Как работает ИИ

```
Студент задаёт вопрос
        ↓
ИИ ищет релевантные фрагменты из загруженных лекций (FAISS Vector Search)
        ↓
LangChain + GPT формирует ответ на основе найденных материалов
        ↓
Студент получает ответ + ссылки на источники (названия лекций)
```

---

## 📁 Структура проекта

```
IRvaTI/
├── README.md
├── .env.example               # Пример переменных окружения
├── docker-compose.yml         # Docker setup
├── backend/
│   ├── Dockerfile
│   ├── requirements.txt
│   ├── main.py                # FastAPI entry point
│   ├── config.py              # Конфигурация
│   ├── database.py            # SQLAlchemy + PostgreSQL
│   ├── models/                # Модели БД (User, Subject, Lecture)
│   ├── schemas/               # Pydantic схемы
│   ├── routers/               # API эндпоинты
│   ├── services/              # Бизнес-логика (auth, AI, file processing)
│   ├── middleware/            # JWT аутентификация
│   └── uploads/               # Загруженные файлы
└── android/
    ├── settings.gradle.kts
    ├── build.gradle.kts
    └── app/
        └── src/main/
            ├── java/com/irvati/lecturebank/
            │   ├── data/      # API, модели, репозитории
            │   ├── ui/        # Экраны, компоненты, тема
            │   ├── viewmodel/ # ViewModels
            │   └── navigation/# Навигация
            └── res/           # Ресурсы
```

---

## 🚀 Запуск бэкенда

### Вариант 1: Docker Compose (рекомендуется)

```bash
# 1. Клонируйте репозиторий
git clone https://github.com/Shamsov/IRvaTI.git
cd IRvaTI

# 2. Создайте .env файл
cp .env.example .env
# Отредактируйте .env: добавьте OPENAI_API_KEY и смените SECRET_KEY

# 3. Запустите контейнеры
docker-compose up -d

# 4. Бэкенд доступен по адресу:
# http://localhost:8000
# Swagger UI: http://localhost:8000/docs
```

### Вариант 2: Ручной запуск

```bash
# 1. Создайте и активируйте виртуальное окружение
cd backend
python -m venv venv
source venv/bin/activate  # Linux/Mac
# venv\Scripts\activate   # Windows

# 2. Установите зависимости
pip install -r requirements.txt

# 3. Настройте PostgreSQL и заполните .env
cp ../.env.example .env

# 4. Запустите сервер
uvicorn main:app --reload --host 0.0.0.0 --port 8000
```

---

## 📱 Запуск Android приложения

### Требования:
- Android Studio Hedgehog или новее
- JDK 17+
- Android SDK 34

### Шаги:

```bash
# 1. Откройте папку android/ в Android Studio
# File → Open → выберите папку android/

# 2. Настройте BASE_URL в RetrofitClient.kt
# Для эмулятора: http://10.0.2.2:8000/
# Для реального устройства: http://YOUR_IP:8000/

# 3. Синхронизируйте Gradle
# File → Sync Project with Gradle Files

# 4. Запустите на эмуляторе или устройстве
# Run → Run 'app'
```

---

## 🔌 API документация

Swagger UI автоматически доступен по адресу: **http://localhost:8000/docs**

### Эндпоинты:

#### 🔐 Аутентификация
| Метод | URL | Описание |
|-------|-----|----------|
| POST | `/api/auth/register` | Регистрация нового пользователя |
| POST | `/api/auth/login` | Вход (возвращает JWT токен) |

#### 📚 Предметы
| Метод | URL | Описание |
|-------|-----|----------|
| GET | `/api/subjects` | Список всех предметов |
| POST | `/api/subjects` | Создать предмет (только админ) |
| DELETE | `/api/subjects/{id}` | Удалить предмет (только админ) |

#### 📄 Лекции
| Метод | URL | Описание |
|-------|-----|----------|
| GET | `/api/lectures/{subject_id}` | Лекции предмета |
| POST | `/api/lectures/upload` | Загрузить лекцию (только админ) |
| GET | `/api/lectures/{id}/content` | Содержимое лекции |
| DELETE | `/api/lectures/{id}` | Удалить лекцию (только админ) |

#### 🤖 ИИ-поиск
| Метод | URL | Описание |
|-------|-----|----------|
| POST | `/api/ai/search` | ИИ-поиск по лекциям |

**Пример запроса к ИИ:**
```json
POST /api/ai/search
{
  "question": "Что такое машинное обучение?",
  "subject_id": 1
}
```

**Пример ответа:**
```json
{
  "answer": "Машинное обучение — это раздел искусственного интеллекта...",
  "sources": [
    {
      "lecture_title": "Введение в ИИ",
      "excerpt": "Машинное обучение позволяет компьютерам..."
    }
  ]
}
```

---

## 📱 Экраны приложения

| Экран | Описание |
|-------|----------|
| **LoginScreen** | Вход в систему (email + пароль) |
| **RegisterScreen** | Регистрация (имя, email, пароль, роль) |
| **HomeScreen** | Главная: список предметов + кнопка ИИ-поиска |
| **SubjectScreen** | Список лекций выбранного предмета |
| **LectureScreen** | Просмотр содержимого лекции |
| **AiSearchScreen** | ИИ-поиск: вопрос → ответ с источниками |
| **AdminDashboard** | Панель админа: статистика, управление |
| **UploadLectureScreen** | Загрузка новой лекции (файл + метаданные) |
| **ManageSubjectsScreen** | Управление предметами (добавить/удалить) |

---

## ⚙️ Переменные окружения

Создайте файл `.env` в корне проекта (см. `.env.example`):

| Переменная | Описание | Пример |
|-----------|----------|--------|
| `POSTGRES_USER` | Пользователь БД | `lecturebank_user` |
| `POSTGRES_PASSWORD` | Пароль БД | `strong_password` |
| `POSTGRES_DB` | Название БД | `lecturebank_db` |
| `DATABASE_URL` | URL подключения к БД | `postgresql://user:pass@localhost/db` |
| `SECRET_KEY` | Секретный ключ JWT | Случайная строка 32+ символов |
| `OPENAI_API_KEY` | Ключ OpenAI API | `sk-...` |
| `UPLOAD_DIR` | Директория для файлов | `uploads` |

---

## 🛠️ Технологии

### Бэкенд:
- **FastAPI** — высокопроизводительный веб-фреймворк
- **SQLAlchemy** — ORM для работы с БД
- **PostgreSQL** — реляционная база данных
- **PyPDF2** — извлечение текста из PDF
- **python-docx** — работа с DOCX файлами
- **LangChain** — фреймворк для RAG pipeline
- **FAISS** — векторный поиск (Facebook AI Similarity Search)
- **OpenAI API** — языковая модель для генерации ответов

### Android:
- **Kotlin** — основной язык разработки
- **Jetpack Compose** — декларативный UI фреймворк
- **Material3** — дизайн-система Google
- **Retrofit** — HTTP клиент
- **Navigation Compose** — навигация между экранами
- **Kotlin Coroutines + Flow** — асинхронность
- **DataStore** — хранение токена

---

## 📋 Лицензия

MIT License