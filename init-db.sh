#!/bin/bash
set -e

# Create both databases
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE DATABASE driver_db;
    CREATE DATABASE order_assignment_db;
    CREATE DATABASE driver_auth_db
EOSQL

# Set execution permission
chmod +x init-db.sh