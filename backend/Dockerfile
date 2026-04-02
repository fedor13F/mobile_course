FROM python:3.11-slim

WORKDIR /code

# Установка зависимостей
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Копируем исходники приложения
COPY app ./app

EXPOSE 8000

# FastAPI подключен к модулю app.main
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]

