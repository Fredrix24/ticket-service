# Сервис заявок: аналитика

## Проблема
Заявки приходят из разных каналов. Нет единой системы:
- Теряются.
- Сроки не отслеживаются.
- Отчёты вручную.
- Нет истории изменений.

## Роли
- **Сотрудник поддержки**: создание, редактирование, статусы, история.
- **Руководитель**: статистика за период.

## Границы
**Входит:** REST API, Kotlin-отчёт, Flyway, Swagger, Docker, CI/CD (GH Actions + self-hosted runner).
**Не входит:** GUI, микросервисы, K8s, auth, уведомления, CRM.

## Ограничения
- 1 экземпляр, без масштабирования.
- Стенд: локальная ВМ + runner.
- Образы в GHCR.
- Время в тестах через `Clock`.

## Истории и критерии приёмки

**US-1. Создание**
Сотрудник создаёт заявку.
- `POST /api/tickets` + валидные данные → 201, `status=NEW`, `created_at` заполнен.
- Нет `title` или `categoryId` → 400, заявка не создана.

**US-2. Просмотр**
- `GET /api/tickets/{id}` → 200, поля из БД.
- Неизвестный ID → 404.

**US-3. Редактирование**
- `PATCH /api/tickets/{id}` для NEW/IN_PROGRESS → 200, поля обновлены.
- Для RESOLVED/CANCELLED → 409, без изменений.

**US-4. Смена статуса**
- `PATCH .../status` (NEW → IN_PROGRESS) → 200, запись в историю.
- RESOLVED → IN_PROGRESS → 409, статус и история не изменены.
- Переход в RESOLVED → `closed_at` заполнен.

**US-5. История**
- `GET .../history` при 2 сменах → 200, 2 записи (`from`, `to`, `changed_at`), отсортированы.

**US-6. Фильтрация**
- `GET /api/tickets?status=NEW&page=0&size=5` → 200, 5 элементов, `totalElements` = кол-во NEW.
- `GET ...?overdue=true` → включает заявки с `due_at < now` и статусом NEW/IN_PROGRESS.

**US-7. Статистика (Kotlin)**
- `GET /api/reports/summary?from=...&to=...` (from < to) → 200, счётчики, `avgResolution=null` если нет RESOLVED.
- `from >= to` → 400.