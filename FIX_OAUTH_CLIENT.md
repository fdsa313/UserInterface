# oauth_client 배열이 비어있는 문제 해결

## 문제
`google-services.json` 파일의 `oauth_client` 배열이 비어있습니다:
```json
"oauth_client": []
```

이것은 Firebase Console에 SHA-1 인증서 지문이 등록되지 않았음을 의미합니다.

## 해결 방법

### 1단계: Firebase Console에서 SHA-1 등록 확인

1. [Firebase Console](https://console.firebase.google.com/) 접속
2. 프로젝트 `userinterface-c864d` 선택
3. ⚙️ **프로젝트 설정** 클릭
4. 아래로 스크롤하여 **"내 앱"** 섹션 찾기
5. Android 앱 (`com.example.lonin`) 클릭
6. **"SHA 인증서 지문"** 섹션 확인

**확인 사항:**
- SHA-1 지문이 등록되어 있는지 확인
- 등록된 SHA-1 값: `E1:9D:07:4F:02:7F:C5:BB:E5:19:D6:7A:77:D7:B5:8C:E8:27:6D:98`

### 2단계: SHA-1 등록 (아직 등록하지 않은 경우)

1. **"SHA 인증서 지문"** 섹션에서 **"지문 추가"** 클릭
2. 다음 값 입력:
   ```
   E1:9D:07:4F:02:7F:C5:BB:E5:19:D6:7A:77:D7:B5:8C:E8:27:6D:98
   ```
3. **"저장"** 클릭
4. **몇 분 대기** (Firebase가 OAuth 클라이언트를 생성하는데 시간이 걸립니다)

### 3단계: google-services.json 파일 다시 다운로드

SHA-1 등록 후:

1. Firebase Console > 프로젝트 설정
2. **"내 앱"** 섹션에서 Android 앱 선택
3. **"google-services.json 다운로드"** 버튼 클릭
4. 다운로드한 파일로 `app/google-services.json` 교체

### 4단계: oauth_client 확인

새로 다운로드한 `google-services.json` 파일을 열어서 다음이 포함되어 있는지 확인:

```json
"oauth_client": [
  {
    "client_id": "111716822708-xxxxx.apps.googleusercontent.com",
    "client_type": 3
  }
]
```

**중요:** `oauth_client` 배열이 비어있지 않고 항목이 있어야 합니다!

### 5단계: 앱 재빌드

1. Android Studio에서 **Build > Clean Project**
2. **Build > Rebuild Project**
3. 앱 재실행

## 여전히 oauth_client가 비어있는 경우

### 확인 사항:
1. **SHA-1 등록 후 충분한 시간 대기** (5-10분)
2. **올바른 프로젝트 선택** 확인
3. **올바른 Android 앱 선택** 확인 (패키지명: `com.example.lonin`)

### 추가 조치:
1. Firebase Console에서 SHA-1을 **삭제 후 다시 등록**
2. **새로운 google-services.json 파일 다운로드**
3. 프로젝트를 **완전히 Clean** 후 재빌드

## 참고

- SHA-1 등록 후 OAuth 클라이언트 생성에는 몇 분이 걸릴 수 있습니다
- `google-services.json` 파일을 다시 다운로드해야 변경사항이 반영됩니다
- 파일을 교체한 후 프로젝트를 Clean하고 Rebuild해야 합니다

