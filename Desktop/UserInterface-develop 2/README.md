# 대학 인증 앱 (University Student Authentication App)

Android 대학생 인증 시스템

## 📱 프로젝트 구조

```
University Student/
├── app/
│   ├── src/main/
│   │   ├── java/com/university/student/
│   │   │   └── activities/
│   │   │       ├── MainActivity.java
│   │   │       ├── CollegeSelectionActivity.java
│   │   │       ├── StudentVerificationActivity.java
│   │   │       └── SettingsActivity.java
│   │   ├── res/
│   │   │   ├── layout/ (9개 레이아웃 파일)
│   │   │   ├── values/ (colors.xml, strings.xml, styles.xml)
│   │   │   └── drawable/ (11개 아이콘 및 drawable)
│   │   └── AndroidManifest.xml
│   ├── build.gradle
│   └── google-services.json
├── build.gradle
├── settings.gradle
└── gradle.properties
```

## 🚀 Android Studio에서 열기

### 1단계: Android Studio 설치
- https://developer.android.com/studio 에서 최신 버전 다운로드
- 설치 시 Android SDK도 함께 설치

### 2단계: 프로젝트 열기
1. Android Studio 실행
2. **"Open"** 클릭
3. `c:\Users\afghr\Desktop\user` 폴더 선택
4. **"OK"** 클릭

### 3단계: Gradle Sync
- Android Studio가 자동으로 Gradle 동기화를 시작합니다
- 처음 열 때는 몇 분 정도 소요될 수 있습니다
- 하단 "Build" 탭에서 진행 상황 확인 가능

### 4단계: SDK 경로 설정 (필요시)
`local.properties` 파일에서 SDK 경로 확인:
```
sdk.dir=C\:\\Users\\afghr\\AppData\\Local\\Android\\Sdk
```
- 실제 SDK 설치 경로와 다르면 수정 필요
- Android Studio: File → Project Structure → SDK Location에서 확인 가능

## 🎨 UI 디자인 미리보기

### 레이아웃 파일 미리보기
1. `app/src/main/res/layout/` 폴더 열기
2. 원하는 XML 파일 클릭 (예: `activity_main.xml`)
3. 우측 상단 **"Split"** 또는 **"Design"** 탭 클릭
4. 실시간 UI 미리보기 확인

### 주요 화면
- **activity_main.xml** - 로그인/회원가입 탭 화면
- **dialog_kakao_login.xml** - 카카오 로그인 다이얼로그
- **activity_college_selection.xml** - 대학 선택 화면
- **activity_student_verification.xml** - 학생 인증 화면
- **activity_settings.xml** - 마이페이지

## 📦 의존성

### Material Design 3
- Material Components 1.10.0

### Firebase
- Firebase Authentication
- Firebase Firestore

### 기타
- ViewPager2 (탭 전환)
- RecyclerView (리스트 표시)
- Glide (이미지 로딩)

## ⚙️ 설정 필요 사항

### Firebase 설정
1. Firebase Console (https://console.firebase.google.com) 접속
2. 새 프로젝트 생성
3. Android 앱 추가
   - 패키지 이름: `com.university.student`
4. `google-services.json` 다운로드
5. 현재 프로젝트의 `app/google-services.json` 파일 교체

### 주의사항
- 현재 `google-services.json`은 템플릿입니다
- 실제 Firebase 프로젝트를 생성하고 파일을 교체해야 합니다

## 🎯 주요 기능

### 구현된 UI
✅ 로그인/회원가입 탭 화면
✅ 카카오 로그인 다이얼로그 (3단계)
✅ 대학 선택 화면 (검색 기능)
✅ 학생 인증 화면 (학생증/유세인트)
✅ 마이페이지 (프로필, 인증 정보)

### TODO (Java 구현 필요)
- [ ] 탭 전환 로직
- [ ] 카카오 로그인 플로우
- [ ] 대학 목록 RecyclerView
- [ ] OCR Mock 구현
- [ ] Firebase 연동
- [ ] SharedPreferences (자동 로그인)

## 🔧 빌드 & 실행

### 에뮬레이터에서 실행
1. Android Studio 상단 툴바에서 기기 선택
2. "Run" 버튼 (녹색 재생 아이콘) 클릭
3. 또는 `Shift + F10` 단축키 사용

### 실제 기기에서 실행
1. 안드로이드 기기 USB 연결
2. 개발자 옵션 활성화
3. USB 디버깅 허용
4. Android Studio에서 연결된 기기 선택 후 실행

## 📝 다음 단계

1. **Android Studio에서 프로젝트 열기**
2. **UI 디자인 확인** (각 XML 파일의 Design 뷰)
3. **Java 코드 구현** (TODO 주석 참고)
4. **Firebase 설정 완료**
5. **테스트 & 디버깅**

## 🎨 색상 가이드

```xml
카카오 노란색: #FEE500
메인 블루: #2563eb
경고 오렌지: #f97316
성공 초록: #22c55e
에러 빨강: #ef4444
```

## 📞 문의

프로젝트 관련 문의사항이 있으시면 이슈를 등록해주세요.
