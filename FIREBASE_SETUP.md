# Firebase 설정 가이드

## 1. Firebase Console에서 프로젝트 생성

1. [Firebase Console](https://console.firebase.google.com/)에 접속
2. "프로젝트 추가" 클릭
3. 프로젝트 이름 입력 (예: "lonin")
4. Google Analytics 설정 (선택사항)
5. 프로젝트 생성 완료

## 2. Android 앱 추가

1. Firebase 프로젝트 대시보드에서 "Android 앱 추가" 클릭
2. 패키지 이름 입력: `com.example.lonin`
3. 앱 닉네임 입력 (선택사항)
4. `google-services.json` 파일 다운로드

## 3. google-services.json 파일 추가

1. 다운로드한 `google-services.json` 파일을 `app/` 디렉토리에 복사
2. 파일 경로: `app/google-services.json`

## 4. SHA-1 인증서 지문 등록 (중요!)

**이 단계를 반드시 먼저 완료해야 합니다!**

### 방법 1: Android Studio에서 확인
1. Android Studio에서 프로젝트 열기
2. 오른쪽 Gradle 탭 클릭
3. `app` > `Tasks` > `android` > `signingReport` 더블클릭
4. Run 창에서 SHA-1 값 복사 (예: `A1:B2:C3:...`)

### 방법 2: 터미널에서 확인
```bash
cd android
./gradlew signingReport
```
출력에서 `SHA1:` 값 찾기

### Firebase Console에 SHA-1 등록
1. Firebase Console > 프로젝트 설정 (톱니바퀴 아이콘)
2. "내 앱" 섹션에서 Android 앱 선택
3. "SHA 인증서 지문 추가" 클릭
4. SHA-1 값 붙여넣기
5. 저장

**중요**: 디버그와 릴리즈 키 모두 등록하는 것을 권장합니다.

## 5. Firebase Authentication 설정

1. Firebase Console에서 "Authentication" 메뉴로 이동
2. "시작하기" 클릭 (처음 사용하는 경우)
3. "Sign-in method" 탭 클릭
4. "이메일/비밀번호" 제공업체 클릭
5. "이메일/비밀번호" 토글 ON
6. "저장" 클릭

## 6. Firestore Database 설정

1. Firebase Console에서 "Firestore Database" 메뉴로 이동
2. "데이터베이스 만들기" 클릭
3. 프로덕션 모드로 시작 (나중에 규칙 수정 가능)
4. 위치 선택 (예: asia-northeast3 - 서울)

## 7. Firestore 보안 규칙 설정

Firestore Database > 규칙 탭에서 다음 규칙 설정:

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Users 컬렉션: 로그인한 사용자만 자신의 데이터 읽기/쓰기
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Partners 컬렉션: 모든 사용자가 읽기 가능, 파트너만 자신의 데이터 쓰기
    match /partners/{partnerId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == partnerId;
    }
    
    // Events 컬렉션: 모든 사용자가 읽기 가능, 파트너만 생성/수정 가능
    match /events/{eventId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null;
    }
    
    // Reviews 컬렉션: 모든 사용자가 읽기 가능, 로그인한 사용자만 작성 가능
    match /reviews/{reviewId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null;
      allow update, delete: if request.auth != null && 
        resource.data.userId == request.auth.uid;
    }
  }
}
```

## 8. 프로젝트 빌드 및 실행

1. Android Studio에서 프로젝트 동기화 (Sync Project)
2. 앱 빌드 및 실행
3. 회원가입 및 로그인 테스트

## 주의사항

- `google-services.json` 파일은 Git에 커밋하지 않도록 `.gitignore`에 추가 권장
- Firestore 보안 규칙은 프로덕션 환경에 맞게 수정 필요
- 테스트 모드에서는 모든 읽기/쓰기가 허용되지만, 프로덕션에서는 보안 규칙 적용 필수

