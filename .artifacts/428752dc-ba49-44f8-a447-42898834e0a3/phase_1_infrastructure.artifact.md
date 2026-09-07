# Phase 1: 기반 강화 및 메시징 추상화 (1-2주차)

이 단계에서는 다양한 채팅 서비스를 하나의 인터페이스로 다루기 위한 설계와 데이터 영속성을 위한 로컬 인프라를 구축합니다.

## 📋 세부 작업 항목

### 1. 도메인 모델 설계 (`domain/model`)
- [x] `ChatServiceType`: 통합할 서비스 종류 정의 (TELEGRAM, DISCORD, WHATSAPP 등)
- [x] `ChatMessage`: 보낸 사람, 내용, 시간, 서비스 타입, 미디어 정보 등을 포함하는 공통 모델
- [x] `ChatRoom`: 채팅방 이름, 마지막 메시지, 읽지 않은 개수, 아이콘 등 정의
- [x] `User`: 각 서비스별 사용자 프로필 정보

### 2. 서비스 추상화 (Abstraction Layer)
- [x] `ChatServiceAdapter` 인터페이스 정의
    - `login()`: 서비스 인증
    - `getChatRooms()`: 채팅방 목록 가져오기
    - `getMessages(roomId)`: 특정 방의 메시지 이력 가져오기
    - `sendMessage(roomId, content)`: 메시지 발송
    - `observeNewMessages()`: 실시간 메시지 수신 (Flow)

### 3. 로컬 데이터베이스 구축 (Room)
- [x] `AppDatabase` 설정
- [x] `ChatMessageEntity`, `ChatRoomEntity` 정의 (메시지 캐싱용)
- [x] `MessageDao`, `ChatRoomDao` 구현 및 관계(Relationship) 설정
- [x] Repository Layer에서 `Remote`와 `Local` 데이터를 통합하는 로직 구현 (Single Source of Truth)

### 4. 백그라운드 동기화 (WorkManager)
- [x] `SyncWorker` 구현: 주기적으로 모든 연결된 서비스의 새 메시지 확인 및 DB 업데이트
- [x] `SyncConstraints`: Wi-Fi 연결 시에만 실행 등 조건 설정
- [x] 앱 실행 시 및 주기적 동기화 스케줄링 로직 (`service/sync`)

## 🛠 관련 기술 가이드
- **Room**: 복합 키(ServiceId + MessageId) 처리에 유의
- **Coroutines Flow**: 실시간 업데이트를 UI에 전달하기 위해 `SharedFlow` 또는 `StateFlow` 적극 활용
- **Hilt**: 각 서비스별 Adapter를 멀티바인딩(Multibinding)으로 관리하는 패턴 고려
