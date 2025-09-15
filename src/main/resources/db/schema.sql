-- Tabilog 데이터베이스 스키마
-- 로컬 테스트용 테이블 생성 SQL

-- 사용자 테이블
CREATE TABLE IF NOT EXISTS user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    user_id VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    gender VARCHAR(10),
    phone_number VARCHAR(20),
    nickname VARCHAR(50) NOT NULL UNIQUE,
    privacy_agreement BOOLEAN NOT NULL DEFAULT FALSE,
    public_agreement BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 리프레시 토큰 테이블
CREATE TABLE IF NOT EXISTS refresh_token (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expiry_date TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
);

-- 여행 계획 테이블
CREATE TABLE IF NOT EXISTS plan (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    region_enum ENUM('東日本', '西日本', '南日本', '北日本') NOT NULL,
    prefecture VARCHAR(100) NOT NULL,
    prefecture_image_url VARCHAR(500),
    participant_count BIGINT NOT NULL,
    total_budget BIGINT NOT NULL,
    status ENUM('PLANNING', 'COMPLETED') NOT NULL,
    is_public BOOLEAN NOT NULL DEFAULT FALSE,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
);

-- 계획 멤버 테이블 (계획 참여자)
CREATE TABLE IF NOT EXISTS plan_member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role ENUM('OWNER', 'EDITOR', 'VIEWER') NOT NULL DEFAULT 'VIEWER',
    FOREIGN KEY (plan_id) REFERENCES plan(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    UNIQUE KEY unique_plan_user (plan_id, user_id)
);

-- 일별 계획 테이블
CREATE TABLE IF NOT EXISTS daily_plan (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan_id BIGINT NOT NULL,
    visit_date DATE NOT NULL,
    departure_time TIME NOT NULL DEFAULT '09:00:00',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (plan_id) REFERENCES plan(id) ON DELETE CASCADE,
    UNIQUE KEY unique_plan_date (plan_id, visit_date)
);

-- 관광지 테이블
CREATE TABLE IF NOT EXISTS spot (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address TEXT NOT NULL,
    category VARCHAR(50) NOT NULL,
    visit_order INT NOT NULL,
    duration VARCHAR(50) NOT NULL,
    cost BIGINT NOT NULL DEFAULT 0,
    latitude DOUBLE,
    longitude DOUBLE,
    daily_plan_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (daily_plan_id) REFERENCES daily_plan(id) ON DELETE CASCADE
);

-- 이동 구간 테이블
CREATE TABLE IF NOT EXISTS travel_segment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    from_spot_id BIGINT NOT NULL,
    to_spot_id BIGINT NOT NULL,
    duration VARCHAR(50) NOT NULL,
    travel_mode VARCHAR(20) NOT NULL,
    segment_order INT NOT NULL,
    daily_plan_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (from_spot_id) REFERENCES spot(id) ON DELETE CASCADE,
    FOREIGN KEY (to_spot_id) REFERENCES spot(id) ON DELETE CASCADE,
    FOREIGN KEY (daily_plan_id) REFERENCES daily_plan(id) ON DELETE CASCADE
);

-- 지출 테이블
CREATE TABLE IF NOT EXISTS expenses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan_id BIGINT NOT NULL,
    spot_id BIGINT,
    item VARCHAR(100) NOT NULL,
    amount INT NOT NULL DEFAULT 0,
    category VARCHAR(50) NOT NULL,
    expense_date DATE NOT NULL,
    created_at DATE DEFAULT (CURRENT_DATE),
    updated_at DATE DEFAULT (CURRENT_DATE),
    FOREIGN KEY (plan_id) REFERENCES plan(id) ON DELETE CASCADE,
    FOREIGN KEY (spot_id) REFERENCES spot(id) ON DELETE CASCADE
);

-- 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_user_email ON user(email);
CREATE INDEX IF NOT EXISTS idx_user_nickname ON user(nickname);
CREATE INDEX IF NOT EXISTS idx_plan_user_id ON plan(user_id);
CREATE INDEX IF NOT EXISTS idx_plan_dates ON plan(start_date, end_date);
CREATE INDEX IF NOT EXISTS idx_plan_region_enum ON plan(region_enum);
CREATE INDEX IF NOT EXISTS idx_plan_status ON plan(status);
CREATE INDEX IF NOT EXISTS idx_plan_is_public ON plan(is_public);
CREATE INDEX IF NOT EXISTS idx_plan_member_plan_id ON plan_member(plan_id);
CREATE INDEX IF NOT EXISTS idx_plan_member_user_id ON plan_member(user_id);
CREATE INDEX IF NOT EXISTS idx_plan_member_role ON plan_member(role);
CREATE INDEX IF NOT EXISTS idx_daily_plan_plan_id ON daily_plan(plan_id);
CREATE INDEX IF NOT EXISTS idx_daily_plan_visit_date ON daily_plan(visit_date);
CREATE INDEX IF NOT EXISTS idx_spot_daily_plan_id ON spot(daily_plan_id);
CREATE INDEX IF NOT EXISTS idx_spot_visit_order ON spot(visit_order);
CREATE INDEX IF NOT EXISTS idx_spot_category ON spot(category);
CREATE INDEX IF NOT EXISTS idx_travel_segment_daily_plan_id ON travel_segment(daily_plan_id);
CREATE INDEX IF NOT EXISTS idx_travel_segment_from_spot ON travel_segment(from_spot_id);
CREATE INDEX IF NOT EXISTS idx_travel_segment_to_spot ON travel_segment(to_spot_id);
CREATE INDEX IF NOT EXISTS idx_expenses_plan_id ON expenses(plan_id);
CREATE INDEX IF NOT EXISTS idx_expenses_spot_id ON expenses(spot_id);
CREATE INDEX IF NOT EXISTS idx_expenses_category ON expenses(category);
CREATE INDEX IF NOT EXISTS idx_expenses_date ON expenses(expense_date);
