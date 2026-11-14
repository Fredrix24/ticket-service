#!/bin/bash
# Восстановление базы данных из файла с предварительной очисткой
if [ -z "$1" ]; then
  echo "Usage: ./scripts/restore.sh <backup_file.sql>"
  exit 1
fi

# Очищаем базу и заливаем дамп
docker exec -i ticket-postgres psql -U postgres -d tickets -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public;"
docker exec -i ticket-postgres psql -U postgres -d tickets < "$1"
echo "Restore completed from $1"