# Tabilog 데이터베이스 스키마

## 파일 설명

- `schema.sql`: 테이블 생성 및 인덱스 생성
- `test-data.sql`: 테스트용 샘플 데이터
- `drop-tables.sql`: 테이블 삭제 (정리용)

## 사용 방법

### 1. 테이블 생성
```sql
-- MySQL/MariaDB에서 실행
SOURCE schema.sql;
```

### 2. 테스트 데이터 삽입
```sql
SOURCE test-data.sql;
```

### 3. 테이블 삭제 (정리)
```sql
SOURCE drop-tables.sql;
```

## 테이블 구조

### user (사용자)
- 기본 사용자 정보
- 이메일, 닉네임으로 로그인

### plan (여행 계획)
- 여행의 전체 계획
- 제목, 기간, 예산 정보

### daily_plan (일별 계획)
- 각 날짜별 세부 계획
- 출발 시간 설정

### spot (관광지)
- 방문할 관광지 정보
- 위치, 카테고리, 체류시간, 비용

### travel_segment (이동 구간)
- 관광지 간 이동 정보
- 이동 시간, 교통수단

## 테스트 데이터

### 사용자
- test@example.com / honggildong
- admin@example.com / admin
- 비밀번호: `password` (BCrypt 해시)

### 여행 계획
1. **도쿄 2박3일** (2026-05-13 ~ 2026-05-15)
   - 1일차: 도쿄역 → 아사쿠사 → 스카이트리
   - 2일차: 호텔 → 도쿄도청 → 이세탄 → 신주쿠어원 → 오모이데요코초
   - 3일차: 시부야 → 하라주쿠 → 메이지신궁

2. **오사카 1박2일** (2026-06-01 ~ 2026-06-02)
   - 1일차: 오사카성 → 도톤보리
   - 2일차: 유니버설 스튜디오 재팬

3. **후쿠오카 여행** (2026-07-10 ~ 2026-07-12)
   - 1일차: 후쿠오카 타워 → 하카타 라멘
   - 2일차: 다자이후 텐만구 → 후쿠오카 시장
   - 3일차: 오호리 공원

## API 테스트

테이블 생성 후 다음 API들을 테스트할 수 있습니다:

```bash
# 사용자 여행 계획 조회
GET /api/plans

# 특정 여행 계획 조회
GET /api/plans/1

# 일별 계획 조회
GET /api/daily-plans/1

# 관광지 검색
GET /api/spots/search?query=도쿄

# Google Places 검색 (API 키 필요)
GET /api/spots/google-search?query=도쿄타워
```
