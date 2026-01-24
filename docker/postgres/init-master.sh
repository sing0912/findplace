#!/bin/bash
set -e

# ============================================================
# PostgreSQL Master 초기화 스크립트
# ============================================================

echo "Initializing PostgreSQL Master..."

# Replication 사용자 생성
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    -- Replication 사용자 생성
    CREATE USER replicator WITH REPLICATION ENCRYPTED PASSWORD 'replicator123!';

    -- Replication Slot 생성
    SELECT pg_create_physical_replication_slot('replica_slot_1');
    SELECT pg_create_physical_replication_slot('replica_slot_2');

    -- 확장 설치
    CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
    CREATE EXTENSION IF NOT EXISTS "pg_trgm";

    -- 스키마 생성
    CREATE SCHEMA IF NOT EXISTS findplace;

    GRANT ALL PRIVILEGES ON SCHEMA findplace TO $POSTGRES_USER;
EOSQL

echo "PostgreSQL Master initialization completed."
