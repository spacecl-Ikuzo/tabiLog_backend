-- 프로필 이미지 관련 컬럼 추가
ALTER TABLE user 
ADD COLUMN original_profile_image_url VARCHAR(500),
ADD COLUMN profile_image_url VARCHAR(500);

-- 기존 사용자들의 프로필 이미지 URL을 NULL로 초기화
UPDATE user 
SET original_profile_image_url = NULL, 
    profile_image_url = NULL 
WHERE original_profile_image_url IS NULL;



