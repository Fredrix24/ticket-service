# Тест-план

Связь требований с автоматизированными проверками.

## Модульные тесты
| ID | Что проверяем | Тест | Статус |
|---|---|---|---|
| UT-1 | Отчёт: пустая выборка, отсутствие RESOLVED возвращает null | `ReportServiceTest.should calculate correct summary when no resolved tickets` | ✅ |
| UT-2 | Отчёт: корректный расчет среднего времени и просроченных | `ReportServiceTest.should calculate avg resolution time and overdue correctly` | ✅ |
| UT-3 | Отчёт: валидация периода (from >= to) | `ReportServiceTest.should throw exception when from is after to` | ✅ |
