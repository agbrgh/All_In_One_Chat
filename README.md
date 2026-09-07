# All In One Chat - 통합 채팅 알림 대시보드 및 멀티 서비스 클라이언트

## 📱 프로젝트 개요

**All In One Chat**은 여러 채팅 앱(텔레그램, 디스코드, 카카오톡, 라인 등)을 한 곳에서 관리하고 소통할 수 있는 통합 안드로이드 애플리케이션입니다. 단순한 알림 수집을 넘어, 주요 서비스에 대한 직접적인 API 연동(TDLib 등)을 통해 강력한 통합 환경을 제공합니다.

### 🎯 핵심 기능

#### 1. 하이브리드 통합 아키텍처
- **Deep Integration**: **Telegram (TDLib)**을 통한 직접 연동으로 메시지 히스토리, 미디어 전송, 계정 관리 지원
- **Universal Listening**: `NotificationListenerService`를 활용해 카카오톡, LINE, WhatsApp, Discord 등의 알림 통합
- **Service Management**: 각 서비스별 계정 연결 및 상태 관리 기능

#### 2. 멀티 디바이스 & 어댑티브 UI
- **Adaptive UI**: Jetpack Compose Adaptive Layout을 사용하여 폰, 태블릿, 폴더블에 최적화된 화면 제공
- **Wear OS 지원**: 시계에서 실시간 채팅 확인 및 데이터 동기화 (`:wear` 모듈)
- **Home Widgets**: Jetpack Glance 기반의 홈 화면 위젯으로 실시간 채팅 현황 파악

#### 3. 고급 메시징 및 검색
- **통합 타임라인**: 모든 서비스의 메시지를 시간순으로 정렬하여 표시
- **Full-Text Search (FTS)**: Room FTS4를 활용한 오프라인 전체 메시지 검색
- **미디어 갤러리**: 이미지, 동영상(Media3/ExoPlayer) 통합 뷰어 제공

#### 4. 보안 및 성능
- **보안 스토리지**: Biometric 인증 및 Tink 기반 암호화 데이터 저장
- **배경 동기화**: WorkManager를 통한 주기적 데이터 싱크 및 백그라운드 처리
- **오프라인 모드**: Room Database를 통한 로컬 캐싱 및 오프라인 접근성 보장

## 🏗️ 아키텍처

### 기술 스택
- **언어**: Kotlin 2.0+
- **UI**: Jetpack Compose (Material 3, Adaptive, Glance)
- **아키텍처**: MVI (Model-View-Intent)
- **의존성 주입**: Hilt
- **데이터 저장**: Room (FTS 지원), DataStore (Preferences, Proto)
- **비동기 처리**: Coroutines & Flow
- **네트워킹**: Retrofit2, Moshi, TDLib (Telegram)
- **백엔드**: Firebase (Auth, Firestore, FCM, Analytics, Crashlytics)
- **미디어**: Media3 (ExoPlayer), Coil (Image Loading)
- **보안**: Biometric, Tink, Security Crypto

### 프로젝트 구조

```
All_In_One_Chat
├── app (Mobile Application)
│   ├── data (Local, Remote, Repository, Mapper, Service Adapters)
│   ├── domain (Model, Repository Interfaces, UseCases)
│   ├── ui (MVI Components, Screens, Navigation, Theme)
│   └── service (Notification Listener, Sync, Security)
├── wear (Wear OS Application)
│   ├── ui (Wearable Screens)
│   └── service (Data Layer Sync)
├── functions (Firebase Cloud Functions)
└── gradle (Build Configurations)
```

## 🚀 시작하기

### 사전 요구사항
- Android Studio Ladybug 이상 권장
- Android SDK 26 (minSdk) ~ 35 (compileSdk)
- Firebase 프로젝트 설정 (`google-services.json` 필요)
- Telegram API ID & Hash (Telegram 연동 필요 시)

### 설치 및 실행

1. **프로젝트 클론**
   ```bash
   git clone https://github.com/your-repo/All_In_One_Chat.git
   ```

2. **Firebase 설정**
   - `app/` 디렉토리에 `google-services.json` 추가

3. **Telegram API 설정**
   - `TelegramConfig.kt` (또는 local.properties)에 API_ID와 API_HASH 설정

4. **빌드 및 실행**
   ```bash
   ./gradlew :app:installDebug
   ```

## 🔐 주요 구현 상세

### MVI (Model-View-Intent)
모든 화면은 예측 가능한 상태 관리를 위해 MVI 패턴을 따릅니다.
- **Intent**: 사용자 액션 정의
- **State**: UI에 반영될 불변(Immutable) 데이터 모델
- **Effect**: 토스트, 네비게이션 등 단발성 이벤트 처리

### Telegram 통합 (TDLib)
`tdlib_tmp.aar`을 활용하여 네이티브 레벨에서 텔레그램 프로토콜을 처리합니다. 이를 통해 알림 없이도 메시지를 주고받고 미디어를 다운로드할 수 있습니다.

### Adaptive Layout
`ListDetailPaneScaffold` 등을 활용하여 화면 크기에 따라 리스트-상세 뷰가 자동으로 전환됩니다.

## 🛠️ 권한 요구사항
- `BIND_NOTIFICATION_LISTENER_SERVICE`: 알림 수집을 위한 필수 권한
- `USE_BIOMETRIC`: 보안 설정 시 생체 인증 사용
- `POST_NOTIFICATIONS`: Android 13+ 알림 표시 권한
- `INTERNET`: 서비스 연동 및 Firebase 통신

## 📊 로드맵 및 진행 현황

- [x] **Phase 1: 통합 알림 리스너 및 기본 UI**
- [x] **Phase 2: 로컬 캐싱 및 MVI 아키텍처 완성**
- [x] **Phase 3: Telegram 직접 연동 (TDLib) 및 Discord 연동**
- [x] **Phase 4: Wear OS 앱 및 홈 화면 위젯**
- [ ] **Phase 5: AI 기반 스마트 답장 및 메시지 요약**
- [ ] **Phase 6: 다양한 서비스(Slack, Teams 등) 직접 연동 확장**

## 📝 라이선스
이 프로젝트는 [MIT 라이선스](https://opensource.org/licenses/MIT)를 따릅니다.
TDLib 사용 시 [Telegram 라이선스](https://github.com/tdlib/td/blob/master/LICENSE_1_0.txt) 정책을 준수해야 합니다.
