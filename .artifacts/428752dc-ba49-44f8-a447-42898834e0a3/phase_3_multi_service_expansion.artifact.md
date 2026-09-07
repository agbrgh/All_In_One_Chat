# Phase 3: 멀티 서비스 확장 (5-7주차)

텔레그램 외의 다양한 채팅 서비스를 추가하고, 통합 알림 시스템을 구축하여 "올인원"의 가치를 실현합니다.

## 📋 세부 작업 항목

### 1. WhatsApp & Discord 연동
- [x] **Discord**: Notification Listener 기반 연동 및 브라우저 인증 구현
- [x] **WhatsApp**: Notification Listener 기반 연동 및 답장 기능 구현
- [x] 각 서비스별 `ServiceAdapter` 구현 및 `SyncWorker` 등록

### 2. 통합 알림 시스템 (FCM & Local)
- [x] **Notification Channel**: 서비스별/채팅방별 알림 채널 분리
- [x] **Summary Notification**: 여러 통의 메시지를 하나로 묶어 보여주는 기능
- [x] **Direct Reply**: 알림창에서 앱을 열지 않고 바로 답장하는 기능 연동

### 3. 멀티 계정 관리 UI
- [x] '연결된 서비스' 설정 화면: 새로운 서비스 추가 및 기존 서비스 관리
- [x] 서비스별 활성/비활성 스위치 (특정 앱의 알림만 끄기 등)

### 4. UI/UX 정교화
- [x] 채팅방 목록에서 서비스 아이콘(Telegram/Discord 등) 표시
- [x] 대화 상대방별 서비스 출처 표시

## 🛠 관련 기술 가이드
- **Notification Manager**: 안드로이드 11 이상의 '대화' 섹션(Conversation Space) 연동 권장
- **Adaptive UI**: 태블릿이나 폴더블 폰에서 리스트-상세(List-Detail) 레이아웃 적용
- **Error Handling**: 특정 서비스의 서버 점검이나 토큰 만료 시 사용자에게 명확히 안내
