#!/bin/bash
# 서비스 서버 SSH 키 인증 최초 설정 (1회만 실행)
# 실행 후 비밀번호 없이 접속 가능

SERVER="1.234.5.96"
USER="root"

# SSH 키가 없으면 생성
if [ ! -f ~/.ssh/id_rsa ]; then
    echo "SSH 키 생성 중..."
    ssh-keygen -t rsa -b 4096 -f ~/.ssh/id_rsa -N ""
fi

# 서버에 공개키 복사 (이때만 비밀번호 입력)
echo "서버에 공개키를 복사합니다. 비밀번호를 입력하세요."
ssh-copy-id -o StrictHostKeyChecking=no ${USER}@${SERVER}

echo ""
echo "설정 완료! 이제 ./scripts/server.sh 로 비밀번호 없이 접속 가능합니다."
