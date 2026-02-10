#!/bin/bash
# MySQL Slave 초기화 스크립트
# 환경변수에서 replication 비밀번호를 읽어 설정

REPL_PASS="${MYSQL_REPL_PASSWORD:-repl_pass}"

mysql -u root -p"${MYSQL_ROOT_PASSWORD}" <<EOF
-- GTID 기반 복제 설정
CHANGE REPLICATION SOURCE TO
  SOURCE_HOST='mysql-log-master',
  SOURCE_USER='repl_user',
  SOURCE_PASSWORD='${REPL_PASS}',
  SOURCE_AUTO_POSITION=1,
  GET_SOURCE_PUBLIC_KEY=1;

START REPLICA;

-- 복제 설정 완료 후 read-only 활성화
SET GLOBAL read_only = ON;
SET GLOBAL super_read_only = ON;
EOF
