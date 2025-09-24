-- plan_member 테이블의 profile_image_url 컬럼 제거 (user 테이블의 profile_image_url을 직접 참조하도록 변경)
ALTER TABLE plan_member DROP COLUMN IF EXISTS profile_image_url;