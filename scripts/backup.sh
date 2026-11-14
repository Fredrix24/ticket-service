#!/bin/bash
BACKUP_FILE="backup_$(date +%Y%m%d_%H%M%S).sql"
docker exec -t ticket-postgres pg_dump -U postgres tickets > "$BACKUP_FILE"
echo "Backup saved to $BACKUP_FILE"