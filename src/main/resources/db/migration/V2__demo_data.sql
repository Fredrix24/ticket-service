INSERT INTO category (name) VALUES
                                ('Hardware'), ('Software'), ('Network');

INSERT INTO ticket (title, description, category_id, priority, status, created_at, due_at)
VALUES
    ('Не работает мышь', 'Левая кнопка не кликает', 1, 'HIGH', 'NEW', NOW(), NOW() + INTERVAL '1 day'),
    ('Ошибка в 1С', 'Вылетает при проведении документа', 2, 'MEDIUM', 'IN_PROGRESS', NOW() - INTERVAL '1 day', NOW() + INTERVAL '2 days');