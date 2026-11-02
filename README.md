# All In One Chat

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-2.0+-7F52FF?style=for-the-badge&logo=kotlin" alt="Kotlin" />
  <img src="https://img.shields.io/badge/Android-Compose-34A853?style=for-the-badge&logo=android" alt="Android Compose" />
  <img src="https://img.shields.io/badge/Firebase-Auth%20%7C%20Firestore%20%7C%20FCM-FFCA28?style=for-the-badge&logo=firebase" alt="Firebase" />
  <img src="https://img.shields.io/badge/Wear%20OS-Supported-5B6CFF?style=for-the-badge&logo=android" alt="Wear OS" />
  <img src="https://img.shields.io/badge/License-MIT-green?style=for-the-badge" alt="MIT License" />
</p>

<p align="center">
  <strong>여러 채팅 서비스를 한 앱에서 통합 관리하는 Android 멀티 채팅 대시보드</strong>
</p>

---

## 프로젝트 소개

All In One Chat은 다양한 메신저�� 채팅 서비스를 하나의 안드로이드 앱에서 통합적으로 확인하고 관리할 수 있도록 설계된 프로젝트입니다.

이 프로젝트는 단일 채팅 서비스 앱이 아니라, 여러 서비스의 채팅방, 메시지, 알림, 사용자 정보, 미디어를 하나의 흐름으로 통합 관리하는 "통합 채팅 대시보드"를 목표로 합니다.

핵심 목표는 다음과 같습니다.

- 여러 채팅 서비스의 메시지를 한 곳에서 확인
- 채팅 목록, 타임라인, 검색, 미디어를 통합 관리
- 로컬 저장소 기반 오프라인 접근
- Firebase 기반 인증 및 데이터 연동
- Wear OS 및 위젯 확장 지원
- Adaptive UI를 활용한 다양한 화면 크기 대응

---

## 주요 기능

### 1. 멀티 서비스 통합
- Telegram / Discord / WhatsApp 등 서비스별 Adapter 구조
- Notification Listener를 활용한 알림 기반 수집
- 서비스별 연결 상태와 계정 관리

### 2. 통합 채팅 타임라인
- 채팅방 목록 관리
- 메시지 기록 확인
- 사용자별 대화 흐름 추적
- 서비스별 타임라인 통합 화면 제공

### 3. 로컬 저장 및 검색
- Room Database 기반 로컬 저장
- FTS 기반 전체 텍스트 검색
- 로컬 캐시와 데이터 정리 기능
- 오프라인 메시지 접근

### 4. Firebase 기반 연동
- Firebase Authentication
- Firestore
- Firebase Cloud Messaging
- Analytics / Crashlytics

### 5. Wear OS 및 위젯
- 스마트워치용 모듈 구성
- 데이터 동기화 구조
- Glance 기반 위젯 지원

### 6. 보안 및 개인 정보 보호
- Biometric 인증
- 보안 저장소 및 암호화 기반 처리
- Android 권한 기반 접근 제어
- 민감 정보 보호 설계

---

## 기술 스택

### Android / UI
- Kotlin
- Android Jetpack
- Jetpack Compose
- Material 3
- Navigation Compose
- Adaptive Layout

### 아키텍처
- MVI
- Hilt
- Repository Pattern
- UseCase 기반 설계

### 데이터 및 저장
- Room
- DataStore
- Firebase Firestore
- Retrofit
- Moshi

### 미디어 및 확장
- Coil
- Media3 / ExoPlayer
- Glance Widgets
- Wear Compose

### 보안 / 서비스
- Android Biometric
- Tink
- Security Crypto
- WorkManager
- Notification Listener

### 외부 연동
- Telegram TDLib 기반 구조
- Firebase Messaging
- Firebase Auth / Firestore

---

## 프로젝트 구조

```text
All_In_One_Chat/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml
│   │   │   ├── java/com/example/all_in_one_chat/
│   │   │   │   ├── data/
│   │   │   │   │   ├── local/
│   │   │   │   │   ├── mapper/
│   │   │   │   │   ├── model/
│   │   │   │   │   ├── remote/
│   │   │   │   │   └── repository/
│   │   │   │   ├── di/
│   │   │   │   ├── domain/
│   │   │   │   │   ├── model/
│   │   │   │   │   ├── repository/
│   │   │   │   │   └── usecase/
│   │   │   │   ├── mvi/
│   │   │   │   ├── service/
│   │   │   │   │   ├── analytics/
│   │   │   │   │   ├── notification/
│   │   │   │   │   ├── security/
│   │   │   │   │   └── sync/
│   │   │   │   ├── ui/
│   │   │   │   │   ├── auth/
│   │   │   │   │   ├── chat/
│   │   │   │   │   ├── media/
│   │   │   │   │   ├── navigation/
│   │   │   │   │   ├── search/
│   │   │   │   │   ├── settings/
│   │   │   │   │   ├── timeline/
│   │   │   │   │   └── theme/
│   │   │   │   ├── AllInOneChatApp.kt
│   │   │   │   └── MainActivity.kt
│   │   │   └── res/
│   │   ├── test/
│   │   └── androidTest/
│   ├── build.gradle.kts
│   ├── google-services.json
│   ├── proguard-rules.pro
│   └── .gitignore
├── wear/
│   ├── src/
│   ├── build.gradle.kts
│   └── .gitignore
├── functions/
│   ├── index.js
│   ├── package.json
│   └── package-lock.json
├── build.gradle.kts
├── settings.gradle.kts
├── gradlew
├── gradlew.bat
├── gradle/
├── .firebaserc
├── firebase.json
├── README.md
├── tdlib_tmp.aar
├── tgx_version.txt
├── skills-lock.json
└── build_output.log
```

---

## 설치 및 실행

### 요구 사항
- Android Studio 최신 버전 권장
- JDK 17 이상 권장
- Android SDK 26 이상
- Firebase 프로젝트
- Google Services 설정 파일
- Telegram 연동을 위한 API 정보 (필요한 경우)

### 1. 저장소 클론

```bash
git clone https://github.com/agbrgh/All_In_One_Chat.git
cd All_In_One_Chat
```

### 2. Firebase 설정
프로젝트 루트의 `app/` 폴더에 `google-services.json` 파일을 추가해야 합니다.

```bash
# 예시
# app/google-services.json
```

Firebase Console에서 Android 앱을 등록한 뒤 다운로드한 파일을 아래 경로에 저장합니다.

```text
app/google-services.json
```

### 3. 빌드

```bash
./gradlew assembleDebug
```

또는 디버그 설치용 실행:

```bash
./gradlew :app:installDebug
```

### 4. Wear 모듈 빌드

```bash
./gradlew :wear:assembleDebug
```

---

## 주요 모듈 설명

### app 모듈
메인 모바일 앱이 위치하는 모듈입니다.

- UI 화면 구성
- 채팅 메시지 처리
- Firebase 연동
- 로컬 DB 및 검색
- MVI 기반 상태 관리
- Notification 및 Sync 관련 서비스

### wear 모듈
스마트워치 전용 앱 모듈입니다.

- Wear OS 화면 구성
- 메시지 확인 흐름
- 데이터 동기화 처리
- 경량 사용자 경험 제공

### functions 모듈
Firebase Functions 기반 백엔드 구성 요소입니다.

- 서버 로직 확장
- Firebase와 연결된 백엔드 기능
- 모바일 앱 연동을 위한 확장 처리

---

## 아키텍처 개요

```text
UI Layer
  └── Compose Screens / Navigation / MVI

Domain Layer
  └── Model / Repository Interface / UseCase

Data Layer
  ├── Room
  ├── Firebase
  ├── Retrofit / Moshi
  └── Service Adapters

Service Layer
  ├── Notification Listener
  ├── Sync Manager
  ├── Security
  └── Background Worker
```

이 구조는 기능별 분리와 확장성을 위해 설계되었으며, 각 서비스와 UI 로직을 독립적으로 관리할 수 있도록 구성되어 있습니다.

---

## 권한 및 보안

Android 앱은 실시간 메시지 수집과 사용자 경험을 위해 일부 권한을 활용할 수 있습니다.

필수 또는 권장 권한:

- `INTERNET`
- `POST_NOTIFICATIONS` (Android 13+)
- `USE_BIOMETRIC`
- `BIND_NOTIFICATION_LISTENER_SERVICE`

보안 관련 구성:

- Biometric 인증
- 암호화 저장소
- 로컬 DB 기반 저장
- Firebase 보안 설정 적용

---

## 로드맵

- [x] 통합 채팅 구조 설계
- [x] 로컬 데이터 저장 및 검색 기능 구성
- [x] Firebase 연동 구조 설계
- [x] Wear OS 모듈 추가
- [x] 위젯 및 통합 UI 구성
- [ ] AI 기반 메시지 요약 기능
- [ ] 추가 서비스 연동 (Slack, Teams 등)
- [ ] 보안 및 운영 기능 강화

---

## 라이선스

이 프로젝트는 [MIT 라이선스](https://opensource.org/licenses/MIT)를 적용합니다.

---

## 기여하기

기여를 환영합니다.

```bash
git checkout -b feature/my-feature
git commit -m "Add my feature"
git push origin feature/my-feature
```

Pull Request를 생성해 주세요.

---

## 연락처 및 참고

이 프로젝트는 개인 개발 프로젝트로 시작되었으며, 확장형 멀티채팅 앱을 목표로 설계되고 있습니다.  
서비스 연동, 보안, UI/UX 개선, 검색 최적화, AI 기능 확장에 대한 아이디어를 적극 환영합니다.

---

<p align="center">
  <strong>Made with ❤️ for a unified messaging experience</strong>
</p>
