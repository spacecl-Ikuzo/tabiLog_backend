# 여행 플랜 초대 기능 가이드

## 개요
TabiLog 백엔드에 이메일을 통한 여행 플랜 멤버 초대 기능이 구현되었습니다. 사용자는 이메일과 역할을 지정하여 다른 사용자를 여행 플랜에 초대할 수 있습니다.

## 주요 기능

### 1. 이메일 초대 전송
- **엔드포인트**: `POST /api/plans/{planId}/invitations`
- **권한**: OWNER 또는 EDITOR만 초대 가능
- **요청 본문**:
```json
{
  "inviteeEmail": "friend@example.com",
  "role": "VIEWER"  // OWNER, EDITOR, VIEWER 중 선택
}
```

### 2. 초대 링크 처리
사용자가 이메일의 초대 링크를 클릭하면:
- **URL**: `GET /invitation/{token}`
- **동작**:
  - 해당 이메일로 가입된 사용자가 있으면 → 로그인 페이지로 리다이렉트
  - 가입된 사용자가 없으면 → 회원가입 페이지로 리다이렉트

### 3. 초대 정보 확인
- **엔드포인트**: `GET /api/plans/invitations/{token}/check`
- **응답**:
```json
{
  "inviteeEmail": "friend@example.com",
  "planTitle": "도쿄 여행",
  "inviterName": "홍길동",
  "role": "VIEWER",
  "userExists": true,
  "redirectType": "login"  // "login" 또는 "signup"
}
```

### 4. 초대 수락
- **엔드포인트**: `POST /api/plans/invitations/{token}/accept`
- **인증 필요**: 로그인된 사용자만 가능
- **동작**: 사용자를 플랜 멤버로 추가

### 5. 자동 초대 수락
#### 회원가입 시
```json
{
  "email": "friend@example.com",
  "userId": "friend123",
  "password": "password123",
  "firstName": "길동",
  "lastName": "홍",
  "nickname": "친구",
  "privacyAgreement": true,
  "invitationToken": "abc123def456"  // 선택적 필드
}
```

#### 로그인 시
```json
{
  "id": "friend@example.com",
  "password": "password123",
  "invitationToken": "abc123def456"  // 선택적 필드
}
```

### 6. 초대 목록 조회
#### 플랜의 초대 목록
- **엔드포인트**: `GET /api/plans/{planId}/invitations`
- **권한**: OWNER 또는 EDITOR만 조회 가능

#### 사용자의 대기 중인 초대 목록
- **엔드포인트**: `GET /api/plans/invitations`
- **인증 필요**: 로그인된 사용자

## 이메일 설정

### 환경 변수 설정
```properties
# Gmail SMTP 설정
GMAIL_USERNAME=your-email@gmail.com
GMAIL_APP_PASSWORD=your-app-password

# 프론트엔드 URL
FRONTEND_URL=http://localhost:3000
```

### Gmail 앱 비밀번호 생성
1. Google 계정 설정으로 이동
2. "보안" → "2단계 인증" 활성화
3. "앱 비밀번호" 생성
4. 생성된 16자리 비밀번호를 `GMAIL_APP_PASSWORD`에 설정

## 데이터베이스 스키마

### plan_invitation 테이블
```sql
CREATE TABLE plan_invitation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan_id BIGINT NOT NULL,
    invitee_email VARCHAR(255) NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    role ENUM('OWNER', 'EDITOR', 'VIEWER') NOT NULL DEFAULT 'VIEWER',
    status ENUM('PENDING', 'ACCEPTED', 'REJECTED', 'EXPIRED') NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    FOREIGN KEY (plan_id) REFERENCES plan(id) ON DELETE CASCADE,
    UNIQUE KEY unique_plan_email (plan_id, invitee_email)
);
```

## 프론트엔드 연동 가이드

### 1. 초대 보내기
```javascript
const inviteUser = async (planId, email, role) => {
  const response = await fetch(`/api/plans/${planId}/invitations`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${accessToken}`
    },
    body: JSON.stringify({
      inviteeEmail: email,
      role: role
    })
  });
  return response.json();
};
```

### 2. 초대 링크 처리
```javascript
// URL: /invitation/{token}
const handleInvitationLink = async (token) => {
  // 초대 정보 확인
  const response = await fetch(`/api/plans/invitations/${token}/check`);
  const invitationInfo = await response.json();
  
  if (invitationInfo.data.userExists) {
    // 로그인 페이지로 이동 (토큰 포함)
    window.location.href = `/login?invitation=${token}&email=${invitationInfo.data.inviteeEmail}`;
  } else {
    // 회원가입 페이지로 이동 (토큰과 이메일 포함)
    window.location.href = `/signup?invitation=${token}&email=${invitationInfo.data.inviteeEmail}`;
  }
};
```

### 3. 로그인/회원가입 시 토큰 처리
```javascript
// 회원가입
const signup = async (userData, invitationToken) => {
  const signupData = {
    ...userData,
    invitationToken: invitationToken  // URL 파라미터에서 가져온 토큰
  };
  
  const response = await fetch('/api/auth/signup', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(signupData)
  });
  
  if (response.ok) {
    // 회원가입 성공 시 자동으로 초대가 수락됨
    // 플랜 페이지로 리다이렉트
  }
};

// 로그인
const login = async (credentials, invitationToken) => {
  const loginData = {
    ...credentials,
    invitationToken: invitationToken  // URL 파라미터에서 가져온 토큰
  };
  
  const response = await fetch('/api/auth/signin', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(loginData)
  });
  
  if (response.ok) {
    // 로그인 성공 시 자동으로 초대가 수락됨
    // 플랜 페이지로 리다이렉트
  }
};
```

## 보안 고려사항

1. **토큰 만료**: 초대 토큰은 7일 후 자동 만료
2. **권한 검증**: OWNER/EDITOR만 초대 가능
3. **이메일 검증**: 초대 수락 시 이메일 일치 확인
4. **중복 방지**: 동일한 이메일로 중복 초대 불가
5. **HTTPS 필수**: 프로덕션 환경에서는 HTTPS 사용 권장

## 트러블슈팅

### 이메일 전송 실패
- Gmail 앱 비밀번호 확인
- 방화벽에서 SMTP 포트(587) 허용 확인
- Gmail 계정의 "보안 수준이 낮은 앱의 액세스" 설정 확인

### 초대 토큰 오류
- 토큰 만료 여부 확인
- 이미 처리된 초대인지 확인
- 데이터베이스에서 토큰 존재 여부 확인

### 권한 오류
- 사용자가 해당 플랜의 OWNER 또는 EDITOR인지 확인
- JWT 토큰 유효성 확인
