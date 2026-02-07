-- GTID 기반 복제 설정
CHANGE REPLICATION SOURCE TO
  SOURCE_HOST='mysql-log-master',
  SOURCE_USER='repl_user',
  SOURCE_PASSWORD='repl_pass',
  SOURCE_AUTO_POSITION=1,
  GET_SOURCE_PUBLIC_KEY=1;

START REPLICA;

-- 복제 설정 완료 후 read-only 활성화
SET GLOBAL read_only = ON;
SET GLOBAL super_read_only = ON;
