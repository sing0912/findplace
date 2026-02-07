-- Replication 유저 생성 (mysql_native_password로 인증 호환성 확보)
-- 비밀번호는 환경변수 MYSQL_REPL_PASSWORD로 설정 (docker-compose.yml 참고)
CREATE USER IF NOT EXISTS 'repl_user'@'%' IDENTIFIED WITH mysql_native_password BY 'repl_pass';
GRANT REPLICATION SLAVE ON *.* TO 'repl_user'@'%';
FLUSH PRIVILEGES;

-- 애플리케이션 DB 및 유저 생성
-- 비밀번호는 환경변수 LOG_DB_PASSWORD로 설정 (docker-compose.yml의 MYSQL_PASSWORD)
CREATE DATABASE IF NOT EXISTS petpro_log CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- loguser는 MYSQL_USER/MYSQL_PASSWORD 환경변수로 자동 생성됨
