# Ticket Service

Сервис обработки заявок службы технической поддержки. REST API на Java (Spring Boot) с блоком статистики на Kotlin.

## Стек

| Направление | Технологии |
|---|---|
| Языки | Java 21, Kotlin 2.x |
| Фреймворк | Spring Boot 3.x |
| БД | PostgreSQL 16 + Flyway |
| Сборка | Gradle (Kotlin DSL) + Wrapper |
| Тесты | JUnit 5, Mockito, MockK, Testcontainers |
| Документация API | OpenAPI (springdoc) |
| Контейнеризация | Docker, Docker Compose |
| CI/CD | GitHub Actions (self-hosted runner на локальной ВМ) |

## Документация

- [Аналитика и требования](docs/analysis.md)
- [Тест-план](docs/test-plan.md)
- [Отчёт](docs/report.md)

## Быстрый старт

Инструкция появится по мере готовности (День 10).

## Версии инструментов

Зафиксированы в начале разработки:

- JDK: **Temurin 21 (LTS)**
- Gradle: **8.10+**
- Spring Boot: **3.3.x**
- PostgreSQL: **16**
- Kotlin: **2.0.x**
- Docker: **27.x**
- Testcontainers: **1.20+**

