# 🚀 Tabilog Backend 배포 가이드 (환경 변수 적용)

## 📋 개요

환경 변수를 분리한 후 Cloud Run에 배포하는 방법입니다. 기존 방법에서 **환경 변수 설정 단계**가 추가되었습니다.

## 🔧 배포 단계

### 1. Maven으로 .jar 파일 빌드

```powershell
mvn clean package -DskipTests
```

`target` 폴더에 최신 코드가 반영된 `.jar` 파일을 생성합니다.

### 2. Docker 이미지 빌드

```powershell
docker build -t asia-northeast3-docker.pkg.dev/tabilog-471102/tabilog-repo/tabilog-backend:latest .
```

새로 만든 `.jar` 파일을 포함하는 Docker 이미지를 생성합니다.

### 3. Docker 인증

```powershell
gcloud auth configure-docker asia-northeast3-docker.pkg.dev
```

`asia-northeast3` 리전의 Artifact Registry에 접근할 수 있도록 인증합니다.

### 4. Docker 이미지 푸시

```powershell
docker push asia-northeast3-docker.pkg.dev/tabilog-471102/tabilog-repo/tabilog-backend:latest
```

빌드한 이미지를 Google Artifact Registry에 업로드합니다.

### 5. 🆕 **환경 변수 설정** (새로 추가된 단계!)

Cloud Run 서비스 배포 시 **모든 필요한 환경 변수를 설정**해야 합니다:

```powershell
gcloud run deploy tabilog-backend `
  --image asia-northeast3-docker.pkg.dev/tabilog-471102/tabilog-repo/tabilog-backend:latest `
  --region asia-northeast3 `
  --project=tabilog-471102 `
  --allow-unauthenticated `
  --platform managed `
  --port 8080 `
  --set-env-vars="SPRING_PROFILES_ACTIVE=prod,DB_URL=jdbc:mysql:///tabilog?cloudSqlInstance=tabilog-471102:asia-northeast3:tabilog-mysql-db&socketFactory=com.google.cloud.sql.mysql.SocketFactory&useSSL=false,DB_USERNAME=root_jsh,DB_PASSWORD=Ikuzo0901!,JWT_SECRET=dGFiaWxvZ1Byb2RTZWN1cml0eUtleTEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MA==,GOOGLE_MAPS_API_KEY=AIzaSyDAp-vdj3Z805yuPNFN_xQTYvjyxJxV6Fc,GMAIL_USERNAME=ikuzojsh@gmail.com,GMAIL_APP_PASSWORD=zvinfmktszymlgzc"
```

## 🔐 환경 변수 목록

다음 환경 변수들이 필요합니다:

| 환경 변수 | 값 | 설명 |
|-----------|-----|------|
| `SPRING_PROFILES_ACTIVE` | `prod` | 프로덕션 프로파일 활성화 |
| `DB_URL` | `jdbc:mysql:///tabilog?cloudSqlInstance=...` | 데이터베이스 연결 URL |
| `DB_USERNAME` | `root_jsh` | 데이터베이스 사용자명 |
| `DB_PASSWORD` | `Ikuzo0901!` | 데이터베이스 비밀번호 |
| `JWT_SECRET` | `dGFiaWxvZ1Byb2RTZWN1cml0eUtleTEy...` | JWT Secret 키 |
| `GOOGLE_MAPS_API_KEY` | `AIzaSyDAp-vdj3Z805yuPNFN_xQTYvjyxJxV6Fc` | Google Maps API 키 |
| `GMAIL_USERNAME` | `ikuzojsh@gmail.com` | Gmail 계정 |
| `GMAIL_APP_PASSWORD` | `zvinfmktszymlgzc` | Gmail 앱 비밀번호 |

## 🛡️ 보안 개선 방법 (권장)

### 방법 1: Google Secret Manager 사용

환경 변수를 Secret Manager에 저장하여 더 안전하게 관리할 수 있습니다:

```powershell
# Secret Manager에 비밀 저장
gcloud secrets create db-password --data-file=- <<< "Ikuzo0901!"
gcloud secrets create jwt-secret --data-file=- <<< "dGFiaWxvZ1Byb2RTZWN1cml0eUtleTEy..."
gcloud secrets create gmail-app-password --data-file=- <<< "zvinfmktszymlgzc"
gcloud secrets create google-maps-api-key --data-file=- <<< "AIzaSyDAp-vdj3Z805yuPNFN_xQTYvjyxJxV6Fc"

# Cloud Run에서 Secret Manager 사용
gcloud run deploy tabilog-backend `
  --image asia-northeast3-docker.pkg.dev/tabilog-471102/tabilog-repo/tabilog-backend:latest `
  --region asia-northeast3 `
  --project=tabilog-471102 `
  --allow-unauthenticated `
  --platform managed `
  --port 8080 `
  --set-env-vars="SPRING_PROFILES_ACTIVE=prod,DB_USERNAME=root_jsh,GMAIL_USERNAME=ikuzojsh@gmail.com" `
  --set-secrets="DB_PASSWORD=db-password:latest,JWT_SECRET=jwt-secret:latest,GMAIL_APP_PASSWORD=gmail-app-password:latest,GOOGLE_MAPS_API_KEY=google-maps-api-key:latest"
```

### 방법 2: 환경 변수 파일 사용

PowerShell에서 환경 변수 파일을 만들어 사용:

```powershell
# env-vars.txt 파일 생성
@"
SPRING_PROFILES_ACTIVE=prod
DB_URL=jdbc:mysql:///tabilog?cloudSqlInstance=tabilog-471102:asia-northeast3:tabilog-mysql-db&socketFactory=com.google.cloud.sql.mysql.SocketFactory&useSSL=false
DB_USERNAME=root_jsh
DB_PASSWORD=Ikuzo0901!
JWT_SECRET=dGFiaWxvZ1Byb2RTZWN1cml0eUtleTEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MA==
GOOGLE_MAPS_API_KEY=AIzaSyDAp-vdj3Z805yuPNFN_xQTYvjyxJxV6Fc
GMAIL_USERNAME=ikuzojsh@gmail.com
GMAIL_APP_PASSWORD=zvinfmktszymlgzc
"@ | Out-File -FilePath env-vars.txt -Encoding utf8

# 환경 변수 파일 사용하여 배포
gcloud run deploy tabilog-backend `
  --image asia-northeast3-docker.pkg.dev/tabilog-471102/tabilog-repo/tabilog-backend:latest `
  --region asia-northeast3 `
  --project=tabilog-471102 `
  --allow-unauthenticated `
  --platform managed `
  --port 8080 `
  --env-vars-file=env-vars.txt
```

## 📝 주요 변경사항

### 이전 방법과의 차이점:

1. **환경 변수 설정 추가**: `--set-env-vars` 옵션으로 모든 민감한 정보를 환경 변수로 전달
2. **보안 강화**: 하드코딩된 비밀번호가 제거되고 환경 변수로 분리
3. **유연성 향상**: 환경에 따라 다른 값을 쉽게 설정 가능

### Dockerfile 변경사항:

현재 Dockerfile은 그대로 사용 가능합니다. 환경 변수는 Cloud Run에서 주입되므로 Dockerfile 수정이 필요하지 않습니다.

## ⚠️ 주의사항

1. **환경 변수 파일 관리**: `env-vars.txt` 파일을 Git에 커밋하지 마세요
2. **비밀번호 변경**: 새로운 비밀번호/키 사용 시 환경 변수 값도 함께 업데이트
3. **배포 전 테스트**: 로컬에서 환경 변수와 함께 애플리케이션 테스트 권장

## 🚀 빠른 배포 명령어

```powershell
# 전체 배포 프로세스 (한 번에 실행)
mvn clean package -DskipTests && `
docker build -t asia-northeast3-docker.pkg.dev/tabilog-471102/tabilog-repo/tabilog-backend:latest . && `
gcloud auth configure-docker asia-northeast3-docker.pkg.dev && `
docker push asia-northeast3-docker.pkg.dev/tabilog-471102/tabilog-repo/tabilog-backend:latest && `
gcloud run deploy tabilog-backend --image asia-northeast3-docker.pkg.dev/tabilog-471102/tabilog-repo/tabilog-backend:latest --region asia-northeast3 --project=tabilog-471102 --allow-unauthenticated --platform managed --port 8080 --set-env-vars="SPRING_PROFILES_ACTIVE=prod,DB_URL=jdbc:mysql:///tabilog?cloudSqlInstance=tabilog-471102:asia-northeast3:tabilog-mysql-db&socketFactory=com.google.cloud.sql.mysql.SocketFactory&useSSL=false,DB_USERNAME=root_jsh,DB_PASSWORD=Ikuzo0901!,JWT_SECRET=dGFiaWxvZ1Byb2RTZWN1cml0eUtleTEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MA==,GOOGLE_MAPS_API_KEY=AIzaSyDAp-vdj3Z805yuPNFN_xQTYvjyxJxV6Fc,GMAIL_USERNAME=ikuzojsh@gmail.com,GMAIL_APP_PASSWORD=zvinfmktszymlgzc"
```
