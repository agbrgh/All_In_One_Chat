# Phase 5: AI 및 지능형 강화 (AI & Intelligent Enhancement)

단순한 메시지 수신을 넘어, AI를 활용하여 사용자에게 더 스마트한 채팅 경험을 제공합니다.

## 📋 세부 작업 항목

### 1. 온디바이스 AI 활용 (ML Kit / Vertex AI for Firebase)
- [ ] **Smart Reply**: 수신된 메시지에 대한 적절한 답장 후보군 3개 생성
- [ ] **Entity Extraction**: 메시지 내의 날짜, 주소, 전화번호 등을 인식하여 캘린더/지도 앱 연동 제안
- [ ] **Translation**: 해외 메시지에 대한 실시간 번역 기능 (On-device)

### 2. 메시지 요약 및 분석
- [ ] **Chat Summary**: 특정 기간 동안 쌓인 긴 대화를 3줄 요약 (Gemini Nano/Pro 연동)
- [ ] **Sentiment Analysis**: 대화의 전반적인 분위기(긍정/부정)를 분석하여 시각화
- [ ] **Priority Sorting**: 중요한 인물이나 급한 메시지를 상단으로 자동 배치

### 3. 미디어 인텔리전스
- [ ] **Object Detection**: 갤러리 내 사진의 객체(강아지, 음식 등)를 인식하여 검색 키워드로 활용
- [ ] **Text in Image (OCR)**: 사진 속 텍스트를 추출하여 검색 및 복사 가능하게 구현

## 🛠 관련 기술 가이드
- **Google ML Kit**: Smart Reply, Entity Extraction 모듈 활용
- **Vertex AI for Firebase**: 대규모 언어 모델(LLM)을 활용한 요약 및 복합 작업 수행
- **Generative AI SDK**: 스트리밍 응답 처리를 위한 `collect` 로직 구현
