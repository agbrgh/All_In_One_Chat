# 올인원챗 (All-In-One Chat) 통합 로드맵

올인원챗은 파편화된 여러 메신저를 하나의 인터페이스로 통합하여 사용자에게 일관되고 지능적인 대화 경험을 제공하는 것을 목표로 합니다.

---

## ✅ 완료된 여정 (Completed)

### **Phase 1: 기반 강화 및 메시징 추상화**
다양한 서비스를 유연하게 수용할 수 있는 아키텍처와 로컬 캐싱 시스템을 구축했습니다.
- [x] 통합 도메인 모델 (`ChatMessage`, `ChatRoom` 등) 정의
- [x] 서비스 추상화 Layer (`ChatServiceAdapter`) 설계
- [x] Room을 활용한 로컬 DB 및 FTS 검색 인프라 구축
- [x] WorkManager 기반 백그라운드 동기화 시스템

### **Phase 2: 첫 번째 서비스 통합 - Telegram**
공식 SDK를 활용하여 텔레그램의 모든 기능을 안정적으로 통합했습니다.
- [x] TDLib 연동 및 네이티브 라이브러리 핸들링
- [x] 텔레그램 특화 인증 Flow (OTP, 2FA) 구현
- [x] 실시간 메시지 수신 및 로컬 DB 매핑

### **Phase 3: 멀티 서비스 확장 - WhatsApp & Discord**
Notification Listener를 활용한 창의적인 접근으로 API 제약을 극복하고 서비스를 확장했습니다.
- [x] WhatsApp & Discord 알림 감지 및 답장 연동
- [x] 통합 알림 시스템 및 Direct Reply 기능
- [x] 서비스별 아이콘 및 출처 표시 UI

### **Phase 4: 핵심 편의 기능 및 보안**
사용자가 매일 쓸 수 있는 실용적인 기능들을 완성했습니다.
- [x] **통합 검색**: 모든 서비스의 메시지를 한 번에 검색
- [x] **미디어 갤러리**: 통합 미디어 모아보기 및 관리
- [x] **보안**: 생체 인식 앱 잠금 및 안전한 토큰 저장

---

## 🚀 앞으로의 여정 (Future Roadmap)

새로운 도전 과제들은 아래의 세부 로드맵 파일에서 관리됩니다.

### **[Phase 5: AI 및 지능형 강화](file:///C:/Users/User/AndroidStudioProjects/All_In_One_Chat/.artifacts/428752dc-ba49-44f8-a447-42898834e0a3/phase_5_ai_intelligent_enhancement.artifact.md)**
- 온디바이스 AI를 활용한 스마트 답장 및 엔티티 추출
- 대화 요약(Summarization) 및 감정 분석
- 이미지 내 텍스트 인식(OCR) 및 객체 탐지

### **[Phase 6: 고도화된 UX 및 생태계 확장](file:///C:/Users/User/AndroidStudioProjects/All_In_One_Chat/.artifacts/428752dc-ba49-44f8-a447-42898834e0a3/phase_6_advanced_ux_ecosystem.artifact.md)**
- [x] 개인화 테마(Dynamic Color) 및 라이트/다크 모드 설정
- Wear OS 및 홈 화면 위젯 지원
- 멀티 디바이스 동기화 및 고급 애니메이션 적용

### **[Phase 7: 프로덕션 품질 및 안정성](file:///C:/Users/User/AndroidStudioProjects/All_In_One_Chat/.artifacts/428752dc-ba49-44f8-a447-42898834e0a3/phase_7_production_stability.artifact.md)**
- [x] Firebase Crashlytics(NDK 포함) 도입 및 예외 트래킹
- [x] Firebase Analytics 도입 및 주요 사용자 행동 로깅
- 단위/UI/스크린샷 테스트를 통한 품질 확보

---
*이 로드맵은 프로젝트의 성장 방향에 따라 지속적으로 업데이트됩니다.*
