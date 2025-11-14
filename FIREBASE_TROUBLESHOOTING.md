# Firebase CONFIGURATION_NOT_FOUND 오류 해결 가이드

## 현재 오류
```
RecaptchaCallWrapper E Initial task failed for action RecaptchaAction(action=signUpPassword)with exception - An internal error has occurred. [ CONFIGURATION_NOT_FOUND ]
```

## 해결 방법 (순서대로 진행)

### 1단계: SHA-1 인증서 지문 확인 및 등록

#### SHA-1 값 확인
터미널에서 다음 명령어 실행:
```bash
cd /Users/choemyeong-il/Desktop/UserInterface/TeamProject
./gradlew signingReport
```

**확인된 SHA-1 값:**
```
E1:9D:07:4F:02:7F:C5:BB:E5:19:D6:7A:77:D7:B5:8C:E8:27:6D:98
```

#### Firebase Console에 등록
1. [Firebase Console](https://console.firebase.google.com/) 접속
2. 프로젝트 선택: **userinterface-c864d**
3. 왼쪽 상단 **⚙️ 프로젝트 설정** 클릭
4. 아래로 스크롤하여 **"내 앱"** 섹션 찾기
5. Android 앱 (`com.example.lonin`) 선택
6. **"SHA 인증서 지문"** 섹션에서 **"지문 추가"** 클릭
7. 다음 값 입력:
   ```
   E1:9D:07:4F:02:7F:C5:BB:E5:19:D6:7A:77:D7:B5:8C:E8:27:6D:98
   ```
8. **"저장"** 클릭

### 2단계: google-services.json 파일 다시 다운로드

SHA-1 등록 후:
1. Firebase Console > 프로젝트 설정
2. **"내 앱"** 섹션에서 Android 앱 선택
3. **"google-services.json 다운로드"** 클릭
4. 다운로드한 파일로 `app/google-services.json` 교체

### 3단계: Firebase Authentication 활성화 확인

1. Firebase Console > **Authentication** 메뉴
2. **"Sign-in method"** 탭 클릭
3. **"이메일/비밀번호"** 제공업체 확인
4. 클릭하여 설정 열기
5. **"이메일/비밀번호 사용 설정"** 토글이 **ON**인지 확인
6. **"저장"** 클릭

### 4단계: 앱 완전 재시작

1. Android Studio에서 앱 **완전 종료** (프로세스 종료)
2. 에뮬레이터/기기에서 앱 **완전 삭제** (설정 > 앱 > 제거)
3. Android Studio에서 **Clean Project**: `Build > Clean Project`
4. **Rebuild Project**: `Build > Rebuild Project`
5. 앱 다시 설치 및 실행

### 5단계: 확인 사항

SHA-1 등록이 성공하면 `google-services.json` 파일의 `oauth_client` 배열에 항목이 추가됩니다:

```json
"oauth_client": [
  {
    "client_id": "...",
    "client_type": 3
  }
]
```

현재 파일에는 `"oauth_client": []`로 비어있으므로, SHA-1 등록이 아직 완료되지 않은 것입니다.

## 추가 확인 사항

### Firebase 프로젝트 상태 확인
- Firebase Console > 프로젝트 설정에서 다음 확인:
  - 프로젝트 ID: `userinterface-c864d`
  - 프로젝트 번호: `111716822708`
  - Android 앱이 정상적으로 등록되어 있는지

### 네트워크 연결 확인
- 인터넷 연결이 정상인지 확인
- 방화벽이나 VPN이 Firebase 서비스 접근을 차단하지 않는지 확인

## 여전히 문제가 발생하는 경우

1. **Firebase Console에서 프로젝트 삭제 후 재생성** (최후의 수단)
2. **새로운 Firebase 프로젝트 생성** 후 위 단계 반복
3. **Firebase 지원팀에 문의**

## 참고

- SHA-1 등록 후 변경사항이 반영되는데 몇 분 정도 걸릴 수 있습니다
- `google-services.json` 파일을 다시 다운로드해야 변경사항이 반영됩니다
- 앱을 완전히 재설치해야 새로운 설정이 적용됩니다

