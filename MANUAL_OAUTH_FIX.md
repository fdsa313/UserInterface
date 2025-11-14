# oauth_client 수동 생성 가이드

30분이 지나도 oauth_client가 비어있는 경우, Google Cloud Console에서 수동으로 생성해야 합니다.

## 방법 1: Google Cloud Console에서 OAuth 클라이언트 생성

### 1단계: Google Cloud Console 접속
1. [Google Cloud Console](https://console.cloud.google.com/) 접속
2. 프로젝트 선택: `userinterface-c864d` (또는 상단에서 프로젝트 선택)

### 2단계: API 및 서비스 > 사용자 인증 정보
1. 왼쪽 메뉴에서 **"API 및 서비스"** 클릭
2. **"사용자 인증 정보"** 클릭

### 3단계: OAuth 클라이언트 ID 생성
1. 상단 **"+ 사용자 인증 정보 만들기"** 클릭
2. **"OAuth 클라이언트 ID"** 선택
3. **애플리케이션 유형**: **"Android"** 선택
4. **이름**: `com.example.lonin` (또는 원하는 이름)
5. **패키지 이름**: `com.example.lonin`
6. **SHA-1 인증서 지문**: 
   ```
   E1:9D:07:4F:02:7F:C5:BB:E5:19:D6:7A:77:D7:B5:8C:E8:27:6D:98
   ```
7. **만들기** 클릭

### 4단계: google-services.json 다시 다운로드
1. Firebase Console로 돌아가기
2. 프로젝트 설정 > 내 앱 > Android 앱
3. **"google-services.json 다운로드"** 클릭
4. 파일 교체

## 방법 2: Firebase Console에서 다시 시도

### 1단계: SHA-1 삭제 후 재등록
1. Firebase Console > 프로젝트 설정
2. Android 앱 선택
3. SHA-1 지문 **삭제**
4. 다시 **등록**:
   ```
   E1:9D:07:4F:02:7F:C5:BB:E5:19:D6:7A:77:D7:B5:8C:E8:27:6D:98
   ```
5. **저장**

### 2단계: Firebase Authentication 확인
1. Firebase Console > **Authentication**
2. **"Sign-in method"** 탭
3. **"이메일/비밀번호"** 클릭
4. **"사용 설정"** 토글이 **ON**인지 확인
5. **"저장"** 클릭

### 3단계: 몇 시간 대기
때로는 Firebase가 OAuth 클라이언트를 생성하는데 더 오래 걸릴 수 있습니다 (최대 24시간).

## 방법 3: 임시 해결책 - 테스트 모드 사용

만약 급하다면, Firebase Authentication의 테스트 모드를 사용할 수 있습니다:

1. Firebase Console > Authentication
2. Settings 탭
3. "Authorized domains" 확인
4. 로컬 테스트를 위해 `localhost` 추가

하지만 이 방법은 프로덕션에는 적합하지 않습니다.

## 확인 사항

### Firebase Console에서 확인:
1. 프로젝트 설정 > 내 앱 > Android 앱
2. SHA-1 인증서 지문이 등록되어 있는지 확인
3. 등록된 지문이 정확한지 확인:
   ```
   E1:9D:07:4F:02:7F:C5:BB:E5:19:D6:7A:77:D7:B5:8C:E8:27:6D:98
   ```

### Google Cloud Console에서 확인:
1. API 및 서비스 > 사용자 인증 정보
2. OAuth 2.0 클라이언트 ID 목록 확인
3. Android 타입의 클라이언트가 있는지 확인

## 권장 사항

**가장 확실한 방법**: Google Cloud Console에서 직접 OAuth 클라이언트를 생성하는 것입니다. 이렇게 하면 즉시 생성되고 google-services.json에 반영됩니다.

