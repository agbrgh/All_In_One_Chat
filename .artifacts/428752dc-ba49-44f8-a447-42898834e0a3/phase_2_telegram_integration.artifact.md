# Phase 2: 첫 번째 서비스 통합 - Telegram (3-4주차)

가장 개방적인 API를 제공하는 텔레그램을 첫 번째 연동 모델로 삼아 전체적인 데이터 흐름을 검증합니다.

## 📋 세부 작업 항목

### 1. Telegram API/SDK 연동
- [x] `my.telegram.org`에서 API ID 및 Hash 발급
- [x] `TDLib` (Telegram Database Library) 안드로이드 라이브러리 추가 및 초기화
- [x] `TelegramServiceAdapter` 구현: Phase 1에서 정의한 인터페이스 연결

### 2. 인증 및 계정 관리
- [x] 전화번호 입력 및 인증번호(OTP) 처리 UI/Logic
- [x] 2단계 인증(Password) 대응
- [x] 세션 토큰 보안 저장 (EncryptedSharedPreferences 활용)

### 3. 실시간 메시지 처리
- [x] TDLib 업데이트 핸들러 구현 (신규 메시지, 채팅방 변경 등)
- [x] 수신된 데이터를 공통 `ChatMessage` 모델로 매핑 및 로컬 DB 저장
- [x] 메시지 읽음 처리(Mark as Read) 동기화

### 4. 통합 UI 구현 (Compose)
- [x] **Timeline View**: 여러 서비스(현재는 Telegram)의 채팅방을 시간순으로 나열
- [x] **Chat Room View**: 말풍선 UI, 텍스트 메시지 렌더링, 시간 표시
- [x] **Account View**: 연결된 텔레그램 계정 상태 표시 및 로그아웃 기능

## 🛠 관련 기술 가이드
- **TDLib**: 네이티브 라이브러리(.so) 로딩 및 비동기 처리(Native Callback -> Kotlin Flow)에 주의
- **Coil**: 텔레그램 프로필 사진 및 이미지 메시지 렌더링 처리
- **MVI State**: 로딩, 인증 성공, 오류 상태를 명확히 구분하여 사용자 경험 향상
