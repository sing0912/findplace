# 실서비스 환경 세팅 가이드

이 문서는 FindPlace 프로젝트를 새로운 서버에 배포할 때 발생할 수 있는 문제들과 해결 방법을 정리한 가이드입니다.

## 목차

1. [사전 준비](#1-사전-준비)
2. [프로젝트 클론](#2-프로젝트-클론)
3. [트러블슈팅](#3-트러블슈팅)
4. [서비스 실행](#4-서비스-실행)
5. [외부 접속 설정](#5-외부-접속-설정)
6. [로그 확인](#6-로그-확인)

---

## 1. 사전 준비

### 테스트 환경
- OS: CentOS/RHEL 8
- Server IP: 1.234.5.95

### 필수 소프트웨어
- Java 21
- Node.js 20+
- Docker
- docker-compose
- Git

---

## 2. 프로젝트 클론

```bash
cd /home
git clone https://github.com/<your-username>/findplace.git
cd findplace
chmod +x setup.sh
./setup.sh
```

---

## 3. 트러블슈팅

### 3.1 Git 설치 안 됨

**증상:**
```
bash: git: command not found
```

**해결:**
```bash
# CentOS/RHEL
yum install -y git

# Ubuntu/Debian
apt install -y git
```

---

### 3.2 docker-compose 패키지 없음

**증상:**
```
No match for argument: docker-compose
Error: Unable to find a match: docker-compose
```

**원인:** CentOS/RHEL 8+에서는 docker-compose가 기본 저장소에 없음

**해결:**
```bash
# docker-compose 수동 설치
curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose

# Docker 서비스 시작
systemctl start docker
systemctl enable docker

# 설치 확인
docker-compose --version
```

---

### 3.3 포트 80 이미 사용 중

**증상:**
```
Error starting userland proxy: listen tcp4 0.0.0.0:80: bind: address already in use
```

**원인:** Apache(httpd) 또는 다른 웹 서버가 80 포트 사용 중

**해결:**
```bash
# 포트 사용 프로세스 확인
lsof -i :80

# Apache 중지
systemctl stop httpd
systemctl disable httpd

# 또는 Nginx 중지
systemctl stop nginx
systemctl disable nginx
```

---

### 3.4 MinIO CPU 호환성 오류

**증상:**
```
Fatal glibc error: CPU does not support x86-64-v2
```

**원인:** 최신 MinIO 이미지가 x86-64-v2 CPU 명령어 세트 필요 (구형 CPU에서 지원 안 함)

**해결:** `docker-compose.yml`에서 MinIO 버전을 구버전으로 변경

```yaml
# 변경 전
minio:
  image: minio/minio:latest

minio-init:
  image: minio/mc:latest

# 변경 후
minio:
  image: minio/minio:RELEASE.2022-10-24T18-35-07Z

minio-init:
  image: minio/mc:RELEASE.2022-10-29T10-09-23Z
```

---

### 3.5 compose 파일 중복 경고

**증상:**
```
WARN[0000] Found multiple config files with supported names: /home/findplace/compose.yaml, /home/findplace/docker-compose.yml
WARN[0000] Using /home/findplace/compose.yaml
```

**원인:** `compose.yaml`과 `docker-compose.yml` 두 파일이 모두 존재

**해결:**
```bash
# compose.yaml 삭제 (docker-compose.yml 사용)
rm -f compose.yaml
```

---

### 3.6 Gradle Wrapper JAR 없음

**증상:**
```
Error: Unable to access jarfile /home/findplace/backend/gradle/wrapper/gradle-wrapper.jar
```

**원인:** `.gitignore`에 의해 `gradle-wrapper.jar`가 git에 포함되지 않음

**해결 방법 1 - 서버에서 Gradle 설치:**
```bash
cd /home/findplace/backend

# Gradle 다운로드 및 설치
wget https://services.gradle.org/distributions/gradle-8.7-bin.zip
unzip gradle-8.7-bin.zip
export PATH=$PATH:$(pwd)/gradle-8.7/bin

# Gradle Wrapper 생성
gradle wrapper
```

**해결 방법 2 - 로컬에서 JAR 파일 push:**
```bash
# 로컬 개발 환경에서
cd /path/to/findplace
git add -f backend/gradle/wrapper/gradle-wrapper.jar
git commit -m "Add gradle wrapper jar"
git push

# 서버에서
git pull
```

---

### 3.7 프론트엔드 allowedHosts 오류

**증상:**
```
Invalid options object. Dev Server has been initialized using an options object that does not match the API schema.
 - options.allowedHosts[0] should be a non-empty string.
```

**원인:** React 개발 서버가 외부 IP 접속을 차단

**해결:**
```bash
cd /home/findplace/frontend

# .env 파일 생성
cat > .env << 'EOF'
HOST=0.0.0.0
DANGEROUSLY_DISABLE_HOST_CHECK=true
WDS_SOCKET_HOST=0.0.0.0
EOF

# 프론트엔드 재시작
nohup npm start > /tmp/findplace-frontend.log 2>&1 &
```

---

### 3.8 node_modules 손상

**증상:**
```
Module not found: Error: Can't resolve 'react-hook-form'
Module not found: Error: Can't resolve './dom-utils/getCompositeRect.js'
```

**원인:** npm install 과정에서 패키지가 제대로 설치되지 않음

**해결:**
```bash
cd /home/findplace/frontend

# 기존 삭제
rm -rf node_modules package-lock.json

# 다시 설치
npm install

# 다시 실행
nohup npm start > /tmp/findplace-frontend.log 2>&1 &
```

---

### 3.9 방화벽 포트 차단

**증상:** 외부에서 서비스 접속 불가

**해결:**
```bash
# 필요한 포트 열기
firewall-cmd --permanent --add-port=80/tcp
firewall-cmd --permanent --add-port=3000/tcp
firewall-cmd --permanent --add-port=8080/tcp
firewall-cmd --permanent --add-port=9000/tcp
firewall-cmd --permanent --add-port=9001/tcp
firewall-cmd --reload

# 또는 방화벽 임시 중지 (테스트용)
systemctl stop firewalld
```

---

## 4. 서비스 실행

### setup.sh 사용

```bash
# 전체 설치 및 실행
./setup.sh

# 개별 명령
./setup.sh install   # 필수 소프트웨어 설치
./setup.sh start     # 서비스 시작
./setup.sh stop      # 서비스 중지
./setup.sh status    # 상태 확인
./setup.sh restart   # 재시작
./setup.sh clean     # 전체 초기화
```

### 수동 실행

**인프라 (Docker):**
```bash
cd /home/findplace
docker-compose up -d
```

**백엔드:**
```bash
cd /home/findplace/backend
chmod +x gradlew
./gradlew build -x test
nohup ./gradlew bootRun > /tmp/findplace-backend.log 2>&1 &
```

**프론트엔드:**
```bash
cd /home/findplace/frontend
npm install
nohup npm start > /tmp/findplace-frontend.log 2>&1 &
```

---

## 5. 외부 접속 설정

### 접속 URL (예: 서버 IP가 1.234.5.95인 경우)

| 서비스 | URL |
|--------|-----|
| 프론트엔드 | http://1.234.5.95:3000 |
| 백엔드 API | http://1.234.5.95:8080/api |
| Swagger UI | http://1.234.5.95:8080/api/swagger-ui.html |
| MinIO Console | http://1.234.5.95:9001 |

### MinIO 로그인 정보
- Username: `minioadmin`
- Password: `minioadmin123!`

---

## 6. 로그 확인

### 백엔드 로그
```bash
# 실시간 로그
tail -f /tmp/findplace-backend.log

# 최근 100줄
tail -100 /tmp/findplace-backend.log

# 에러만 확인
grep -i "error\|exception" /tmp/findplace-backend.log
```

### 프론트엔드 로그
```bash
# 실시간 로그
tail -f /tmp/findplace-frontend.log

# 최근 100줄
tail -100 /tmp/findplace-frontend.log
```

### Docker 컨테이너 로그
```bash
# PostgreSQL
docker logs findplace-postgres-master

# Redis
docker logs findplace-redis

# MinIO
docker logs findplace-minio
```

### 서비스 상태 확인
```bash
./setup.sh status
```

---

## 체크리스트

배포 전 확인 사항:

- [ ] Git 설치됨
- [ ] Docker 설치 및 실행 중
- [ ] docker-compose 설치됨
- [ ] 포트 80, 3000, 8080, 9000, 9001 사용 가능
- [ ] 방화벽 포트 열림
- [ ] gradle-wrapper.jar 존재
- [ ] frontend/.env 파일 생성됨

---

## 참고 사항

### 프로덕션 환경 권장 사항

1. **HTTPS 설정**: Nginx에 SSL 인증서 적용
2. **환경 변수**: 민감 정보는 환경 변수로 관리
3. **프론트엔드 빌드**: 개발 서버 대신 `npm run build` 후 Nginx로 서빙
4. **로그 관리**: 로그 로테이션 설정
5. **모니터링**: Prometheus + Grafana 연동

### 유용한 명령어

```bash
# 모든 Docker 컨테이너 상태
docker ps -a

# Docker 리소스 정리
docker system prune -a

# 디스크 사용량 확인
df -h

# 메모리 사용량 확인
free -m

# 프로세스 확인
ps aux | grep -E "java|node|docker"
```
