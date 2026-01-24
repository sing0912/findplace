#!/bin/bash
set -e

# ============================================================
# PostgreSQL Slave 초기화 스크립트
# ============================================================

echo "PostgreSQL Slave is ready for replication."

# Slave는 Master에서 베이스 백업을 받아 시작하므로
# 별도의 초기화가 필요 없습니다.
# docker-compose.yml의 command에서 pg_basebackup을 실행합니다.
