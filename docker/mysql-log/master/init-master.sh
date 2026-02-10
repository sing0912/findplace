#!/bin/bash
# MySQL Master 초기화 스크립트
# 환경변수에서 replication 비밀번호를 읽어 설정

REPL_PASS="${MYSQL_REPL_PASSWORD:-repl_pass}"

mysql -u root -p"${MYSQL_ROOT_PASSWORD}" <<EOF
-- Replication 유저 생성
CREATE USER IF NOT EXISTS 'repl_user'@'%' IDENTIFIED WITH mysql_native_password BY '${REPL_PASS}';
GRANT REPLICATION SLAVE ON *.* TO 'repl_user'@'%';
FLUSH PRIVILEGES;

-- 애플리케이션 DB 생성
CREATE DATABASE IF NOT EXISTS petpro_log CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
EOF
