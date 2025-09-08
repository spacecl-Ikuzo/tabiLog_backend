-- Tabilog 데이터베이스 테이블 삭제
-- 테스트 후 정리용

-- 외래키 제약조건 때문에 역순으로 삭제
DROP TABLE IF EXISTS travel_segment;
DROP TABLE IF EXISTS spot;
DROP TABLE IF EXISTS daily_plan;
DROP TABLE IF EXISTS plan;
DROP TABLE IF EXISTS refresh_token;
DROP TABLE IF EXISTS user;
