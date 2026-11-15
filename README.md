# Ticket Service

Сервис обработки заявок службы технической поддержки. REST API на Java 21 (Spring Boot 4) с блоком статистики на Kotlin.

## Стек

- **Языки:** Java 21, Kotlin 2.x
- **Фреймворк:** Spring Boot 4.1.2
- **БД:** PostgreSQL 16 + Flyway
- **Сборка:** Gradle (Kotlin DSL) + Wrapper
- **Тесты:** JUnit 5, Mockito, Testcontainers (PostgreSQL)
- **Документация API:** OpenAPI (Swagger UI)
- **Контейнеризация:** Docker, Docker Compose
- **CI/CD:** GitHub Actions (self-hosted runner на WSL) + GHCR

## Документация

Вся проектная документация находится в папке [`docs/`](docs/):

- [Аналитика и требования](docs/analysis.md) — проблема, границы, пользовательские истории, критерии приёмки.
- [Заметки по Kotlin](docs/kotlin-notes.md) — обоснование решений (nullable-типы, data class).
- [Тест-план](docs/test-plan.md) — связь требований с тестами.
- [Найденные дефекты](docs/bugs.md) — описание багов и регрессионных тестов.
- [Отчёт](docs/report.md) — итоги и ограничения.

## Быстрый старт

Убедитесь, что у вас запущен Docker Desktop.

Запустите базу данных:
```bash
docker compose up -d postgres
```
Запустите приложение:

```bash
./gradlew bootRun
```
После запуска Swagger UI доступен по адресу: http://localhost:8080/swagger-ui/index.html

## Запуск в Docker
```bash
docker compose up -d --build
```
## Тестирование
```bash
./gradlew test
```
Отчет о покрытии (JaCoCo) находится в build/reports/jacoco/test/html/index.html.

## CI/CD
- **Pull Request:** запускаются только тесты (без деплоя).

- **Push в main:** сборка Docker-образа, публикация в GHCR, автоматический деплой на self-hosted runner (WSL) и health check.

- **Откат:** вкладка Actions -> workflow Rollback -> Run workflow (указать тег latest или SHA коммита).

## Диагностика и эксплуатация
1. **База данных недоступна**

- **Симптомы:** Приложение падает с ошибкой Connection refused.

- **Решение:** Проверьте docker ps. Если контейнер ticket-postgres не запущен, выполните docker compose up -d postgres.

2. **Health check не проходит**

- **Симптомы:** Приложение запускается, но /actuator/health возвращает DOWN.

- **Решение:** Посмотрите логи приложения (docker logs ticket-app или вывод в терминале). Часто проблема в неверных переменных окружения (пароль к БД).

3. **Миграция завершается ошибкой**

- **Симптомы:** В логах Flyway пишет Migration failed.

- **Решение:** Проверьте таблицу flyway_schema_history в БД. В учебном проекте проще всего сбросить базу: docker compose down -v && docker compose up -d.