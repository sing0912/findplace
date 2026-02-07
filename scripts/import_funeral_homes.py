#!/usr/bin/env python3
"""
장례식장 CSV 데이터를 PostgreSQL에 import하는 스크립트

사용법:
    pip install psycopg2-binary pyproj
    python import_funeral_homes.py /path/to/data.csv

환경변수:
    DB_HOST: PostgreSQL 호스트 (기본: localhost)
    DB_PORT: PostgreSQL 포트 (기본: 5432)
    DB_NAME: 데이터베이스 이름 (기본: petpro)
    DB_USER: 사용자 (기본: petpro)
    DB_PASSWORD: 비밀번호 (필수, .env 파일 참고)
"""

import csv
import os
import sys
from datetime import datetime

try:
    import psycopg2
    from psycopg2.extras import execute_values
except ImportError:
    print("psycopg2 설치 필요: pip install psycopg2-binary")
    sys.exit(1)

try:
    from pyproj import Transformer
except ImportError:
    print("pyproj 설치 필요: pip install pyproj")
    sys.exit(1)


# TM좌표(EPSG:5174 - 중부원점) -> WGS84(EPSG:4326) 변환기
# 공공데이터는 보통 EPSG:5174 또는 EPSG:5186 사용
transformer = Transformer.from_crs("EPSG:5174", "EPSG:4326", always_xy=True)


def convert_tm_to_wgs84(x, y):
    """TM좌표를 WGS84 위도/경도로 변환"""
    if not x or not y:
        return None, None
    try:
        x = float(str(x).strip())
        y = float(str(y).strip())
        if x == 0 or y == 0:
            return None, None
        lon, lat = transformer.transform(x, y)
        # 한국 좌표 범위 검증 (위도 33~39, 경도 124~132)
        if 33 <= lat <= 39 and 124 <= lon <= 132:
            return round(lat, 7), round(lon, 7)
        return None, None
    except (ValueError, TypeError):
        return None, None


def parse_csv(file_path):
    """CSV 파일 파싱"""
    records = []

    # 인코딩 시도 순서
    encodings = ['utf-8', 'euc-kr', 'cp949']

    for encoding in encodings:
        try:
            with open(file_path, 'r', encoding=encoding) as f:
                reader = csv.DictReader(f)
                for row in reader:
                    # 영업상태 확인
                    status = row.get('영업상태명', '')
                    is_active = status == '영업/정상'

                    # 좌표 변환
                    x = row.get('좌표정보(X)', '')
                    y = row.get('좌표정보(Y)', '')
                    lat, lon = convert_tm_to_wgs84(x, y)

                    # 전화번호 정리
                    phone = row.get('전화번호', '').strip()
                    if phone and not phone.startswith('0'):
                        phone = '0' + phone

                    # 주소에서 지역명 추출
                    road_address = row.get('도로명주소', '').strip()
                    lot_address = row.get('지번주소', '').strip()
                    loc_name = ''
                    if road_address:
                        parts = road_address.split()
                        if parts:
                            loc_name = parts[0]
                    elif lot_address:
                        parts = lot_address.split()
                        if parts:
                            loc_name = parts[0]

                    record = {
                        'name': row.get('사업장명', '').strip(),
                        'road_address': road_address if road_address else None,
                        'lot_address': lot_address if lot_address else None,
                        'phone': phone if phone else None,
                        'loc_code': row.get('개방자치단체코드', '').strip() or None,
                        'loc_name': loc_name if loc_name else None,
                        'has_crematorium': False,  # CSV에 정보 없음
                        'has_columbarium': False,  # CSV에 정보 없음
                        'has_funeral': True,       # 장례식장 데이터이므로 True
                        'latitude': lat,
                        'longitude': lon,
                        'is_active': is_active,
                        'synced_at': datetime.now(),
                    }

                    # 이름이 있는 경우만 추가
                    if record['name']:
                        records.append(record)

                print(f"CSV 파싱 완료: {len(records)}건 (인코딩: {encoding})")
                return records
        except UnicodeDecodeError:
            continue

    print("CSV 파일 인코딩을 확인할 수 없습니다.")
    sys.exit(1)


def import_to_db(records):
    """PostgreSQL에 데이터 import"""

    # DB 연결 정보
    db_config = {
        'host': os.environ.get('DB_HOST', 'localhost'),
        'port': os.environ.get('DB_PORT', '5432'),
        'dbname': os.environ.get('DB_NAME', 'petpro'),
        'user': os.environ.get('DB_USER', 'petpro'),
        'password': os.environ.get('DB_PASSWORD', ''),
    }

    print(f"DB 연결: {db_config['host']}:{db_config['port']}/{db_config['dbname']}")

    try:
        conn = psycopg2.connect(**db_config)
        cur = conn.cursor()

        # 기존 데이터 삭제
        cur.execute("DELETE FROM funeral_homes")
        deleted_count = cur.rowcount
        print(f"기존 데이터 삭제: {deleted_count}건")

        # 시퀀스 리셋
        cur.execute("ALTER SEQUENCE funeral_homes_id_seq RESTART WITH 1")

        # 새 데이터 삽입
        insert_sql = """
            INSERT INTO funeral_homes (
                name, road_address, lot_address, phone,
                loc_code, loc_name,
                has_crematorium, has_columbarium, has_funeral,
                latitude, longitude,
                is_active, synced_at,
                created_at, updated_at
            ) VALUES %s
        """

        values = [
            (
                r['name'], r['road_address'], r['lot_address'], r['phone'],
                r['loc_code'], r['loc_name'],
                r['has_crematorium'], r['has_columbarium'], r['has_funeral'],
                r['latitude'], r['longitude'],
                r['is_active'], r['synced_at'],
                datetime.now(), datetime.now()
            )
            for r in records
        ]

        execute_values(cur, insert_sql, values)
        inserted_count = len(values)

        # 통계 출력
        cur.execute("SELECT COUNT(*) FROM funeral_homes WHERE is_active = true")
        active_count = cur.fetchone()[0]

        cur.execute("SELECT COUNT(*) FROM funeral_homes WHERE latitude IS NOT NULL")
        geocoded_count = cur.fetchone()[0]

        conn.commit()

        print(f"\n=== Import 완료 ===")
        print(f"총 삽입: {inserted_count}건")
        print(f"활성 업체: {active_count}건")
        print(f"좌표 있음: {geocoded_count}건")
        print(f"좌표 없음: {inserted_count - geocoded_count}건")

        cur.close()
        conn.close()

    except psycopg2.Error as e:
        print(f"DB 오류: {e}")
        sys.exit(1)


def main():
    if len(sys.argv) < 2:
        print("사용법: python import_funeral_homes.py <csv_file_path>")
        print("예시: python import_funeral_homes.py /path/to/data.csv")
        sys.exit(1)

    csv_path = sys.argv[1]

    if not os.path.exists(csv_path):
        print(f"파일이 존재하지 않습니다: {csv_path}")
        sys.exit(1)

    print(f"CSV 파일: {csv_path}")
    print("=" * 50)

    # CSV 파싱
    records = parse_csv(csv_path)

    if not records:
        print("파싱된 데이터가 없습니다.")
        sys.exit(1)

    # DB import
    import_to_db(records)


if __name__ == '__main__':
    main()
