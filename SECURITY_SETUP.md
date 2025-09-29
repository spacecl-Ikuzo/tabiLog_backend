# 🔒 보안 설정 가이드

## ⚠️ 중요: GitHub Public 저장소 전환 전 필수 작업

이 저장소를 GitHub에서 private에서 public으로 변경하기 전에 **반드시** 아래 작업을 완료해야 합니다.

## 🚨 발견된 보안 문제들

다음 민감한 정보들이 코드에 하드코딩되어 있었습니다:

1. **데이터베이스 비밀번호**: `Ikuzo0901!`
2. **Gmail 앱 비밀번호**: `zvinfmktszymlgzc`
3. **Google Maps API 키**: `AIzaSyDAp-vdj3Z805yuPNFN_xQTYvjyxJxV6Fc`
4. **JWT Secret 키들**: 여러 환경별 키
5. **Gmail 계정**: `ikuzojsh@gmail.com`

## ✅ 해결 완료된 사항

- [x] 모든 하드코딩된 비밀번호를 환경 변수로 변경
- [x] `.gitignore`에 환경 변수 파일들 추가
- [x] `env.template` 파일 생성

## 🛠️ 환경 설정 방법

### 1. 환경 변수 파일 생성

```bash
# env.template을 복사하여 .env 파일 생성
cp env.template .env
```

### 2. .env 파일에 실제 값 입력

`.env` 파일을 열고 다음 값들을 실제 값으로 변경하세요:

```bash
# 데이터베이스 비밀번호 (실제 비밀번호로 변경)
DB_PASSWORD=your_actual_database_password

# JWT Secret (새로운 키 생성 권장)
JWT_SECRET=your_new_jwt_secret_base64_encoded

# Google Maps API 키 (새로운 키 생성 권장)
GOOGLE_MAPS_API_KEY=your_new_google_maps_api_key

# Gmail 계정 정보
GMAIL_USERNAME=your_email@gmail.com
GMAIL_APP_PASSWORD=your_gmail_app_password
```

### 3. 새로운 비밀번호/키 생성 방법

#### JWT Secret 생성
```bash
# Linux/Mac
echo -n "your_very_long_and_complex_secret_string_here" | base64

# Windows PowerShell
[Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes("your_very_long_and_complex_secret_string_here"))
```

#### Gmail 앱 비밀번호 생성
1. Google 계정 설정 → 보안
2. 2단계 인증 활성화
3. 앱 비밀번호 생성
4. 생성된 16자리 비밀번호를 `GMAIL_APP_PASSWORD`에 입력

#### Google Maps API 키 생성
1. Google Cloud Console → API 및 서비스 → 사용자 인증 정보
2. API 키 생성
3. 적절한 제한 설정 (IP 주소, HTTP 리퍼러 등)

## 🚀 애플리케이션 실행

### 로컬 개발 환경
```bash
# 환경 변수 파일 로드 후 실행
./gradlew bootRun
```

### 프로덕션 환경
```bash
# 환경 변수 설정 후 실행
export DB_PASSWORD=your_password
export JWT_SECRET=your_secret
export GOOGLE_MAPS_API_KEY=your_key
export GMAIL_USERNAME=your_email
export GMAIL_APP_PASSWORD=your_app_password
./gradlew bootRun
```

## 🔐 추가 보안 권장사항

1. **데이터베이스 비밀번호 변경**: 현재 사용 중인 `Ikuzo0901!` 비밀번호를 더 강력한 비밀번호로 변경
2. **Gmail 앱 비밀번호 재생성**: 현재 사용 중인 앱 비밀번호를 무효화하고 새로 생성
3. **Google Maps API 키 제한**: API 키에 IP 주소, HTTP 리퍼러 등 적절한 제한 설정
4. **JWT Secret 변경**: 모든 환경에서 새로운 JWT Secret 사용
5. **정기적인 비밀번호 변경**: 보안을 위해 정기적으로 비밀번호 변경

## 📋 체크리스트

Public 저장소 전환 전 확인사항:

- [ ] `.env` 파일 생성 및 실제 값 입력
- [ ] 데이터베이스 비밀번호 변경
- [ ] Gmail 앱 비밀번호 재생성
- [ ] Google Maps API 키 제한 설정
- [ ] JWT Secret 변경
- [ ] 애플리케이션 정상 동작 확인
- [ ] `.env` 파일이 Git에 추가되지 않았는지 확인

## ⚠️ 주의사항

- `.env` 파일은 **절대** Git에 커밋하지 마세요
- 운영 환경에서는 더 강력한 비밀번호를 사용하세요
- API 키에는 적절한 제한을 설정하세요
- 정기적으로 보안 검토를 수행하세요

---

**이 모든 작업을 완료한 후에만 GitHub 저장소를 public으로 변경하세요!**
