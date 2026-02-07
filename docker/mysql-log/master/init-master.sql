-- Replication 유저 생성 (mysql_native_password로 인증 호환성 확보)
CREATE USER IF NOT EXISTS 'repl_user'@'%' IDENTIFIED WITH mysql_native_password BY 'repl_pass123!';
GRANT REPLICATION SLAVE ON *.* TO 'repl_user'@'%';
FLUSH PRIVILEGES;

-- 애플리케이션 DB 및 유저 생성
CREATE DATABASE IF NOT EXISTS petpro_log CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'loguser'@'%' IDENTIFIED BY 'logpass123!';
GRANT ALL PRIVILEGES ON petpro_log.* TO 'loguser'@'%';
FLUSH PRIVILEGES;
