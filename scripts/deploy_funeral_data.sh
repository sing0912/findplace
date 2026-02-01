#!/bin/bash
# 장례식장 데이터를 서버에 업로드하고 DB에 import하는 스크립트

set -e

# 서버 정보
SERVER_HOST="1.234.5.95"
SERVER_USER="root"
SERVER_PATH="/home/findplace"

# 로컬 파일 경로
CSV_FILE="$HOME/Desktop/data.csv"
SCRIPT_FILE="$(dirname "$0")/import_funeral_homes.py"

echo "=========================================="
echo "장례식장 데이터 서버 배포"
echo "=========================================="

# 파일 존재 확인
if [ ! -f "$CSV_FILE" ]; then
    echo "❌ CSV 파일이 없습니다: $CSV_FILE"
    exit 1
fi

if [ ! -f "$SCRIPT_FILE" ]; then
    echo "❌ Python 스크립트가 없습니다: $SCRIPT_FILE"
    exit 1
fi

echo "📁 CSV 파일: $CSV_FILE"
echo "📁 스크립트: $SCRIPT_FILE"
echo "🖥️  서버: $SERVER_USER@$SERVER_HOST"
echo ""

# 1. 파일 전송
echo "📤 파일 전송 중..."
scp "$CSV_FILE" "$SERVER_USER@$SERVER_HOST:$SERVER_PATH/data.csv"
scp "$SCRIPT_FILE" "$SERVER_USER@$SERVER_HOST:$SERVER_PATH/import_funeral_homes.py"
echo "✅ 파일 전송 완료"
echo ""

# 2. 서버에서 스크립트 실행
echo "🚀 서버에서 import 실행 중..."
ssh "$SERVER_USER@$SERVER_HOST" << 'EOF'
cd /home/findplace

# Python 패키지 설치 (없으면)
pip install psycopg2-binary pyproj -q 2>/dev/null || pip3 install psycopg2-binary pyproj -q

# import 스크립트 실행
python import_funeral_homes.py data.csv || python3 import_funeral_homes.py data.csv

# 정리
rm -f data.csv import_funeral_homes.py

echo ""
echo "🎉 서버 DB import 완료!"
EOF

echo ""
echo "=========================================="
echo "✅ 배포 완료"
echo "=========================================="
