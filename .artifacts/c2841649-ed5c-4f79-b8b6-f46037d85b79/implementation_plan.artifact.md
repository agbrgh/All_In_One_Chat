# 올인원챗 로드맵 조정 및 UX/안정성 강화 계획

AI 관련 기능(Phase 5)을 후순위로 미루고, 사용자가 요청한 **개인화/테마화(Phase 6)**와 **안정성 강화(Phase 7 - Crashlytics)**를 우선적으로 진행합니다.

## User Review Required

> [!IMPORTANT]
> **Crashlytics 테스트**: 설정 완료 후 의도적인 크래시를 발생시켜 Firebase 콘솔에 정상적으로 집계되는지 확인하는 과정이 필요합니다. 테스트 후에는 해당 코드를 즉시 제거해야 합니다.

## Proposed Changes

### 1. 로드맵 및 태스크 리스트 업데이트

#### [MODIFY] [roadmap.artifact.md](file:///C:/Users/User/AndroidStudioProjects/All_In_One_Chat/.artifacts/428752dc-ba49-44f8-a447-42898834e0a3/roadmap.artifact.md)
- Phase 5(AI)를 '선택적/후순위' 섹션으로 이동.
- Phase 6(UX) 및 Phase 7(안정성)을 현재 진행 단계로 격상.

---

### 2. 개인화 및 테마화 (Phase 6 보완)

#### [MODIFY] [Theme.kt](file:///C:/Users/User/AndroidStudioProjects/All_In_One_Chat/app/src/main/java/com/example/all_in_one_chat/ui/theme/Theme.kt)
- 사용자가 Dynamic Color를 수동으로 켜고 끌 수 있는 설정 연동 준비.

#### [MODIFY] [Color.kt](file:///C:/Users/User/AndroidStudioProjects/All_In_One_Chat/app/src/main/java/com/example/all_in_one_chat/ui/theme/Color.kt)
- 서비스별 커스텀 색상 테마를 위한 색상 팔레트 확장.

---

### 3. 안정성 강화 (Phase 7 - Crashlytics)

#### [MODIFY] [libs.versions.toml](file:///C:/Users/User/AndroidStudioProjects/All_In_One_Chat/gradle/libs.versions.toml)
- `firebase-crashlytics` 라이브러리 및 플러그인 정의 추가.

#### [MODIFY] [build.gradle.kts (project)](file:///C:/Users/User/AndroidStudioProjects/All_In_One_Chat/build.gradle.kts)
- Crashlytics 플러그인 등록.

#### [MODIFY] [build.gradle.kts (app)](file:///C:/Users/User/AndroidStudioProjects/All_In_One_Chat/app/build.gradle.kts)
- Crashlytics 플러그인 적용 및 의존성 추가.
- Native(TDLib) 크래시 감지를 위한 NDK 설정 검토.

---

### 4. 설정 화면 고도화

#### [NEW] [AppearanceSettingsScreen.kt](file:///C:/Users/User/AndroidStudioProjects/All_In_One_Chat/app/src/main/java/com/example/all_in_one_chat/ui/settings/AppearanceSettingsScreen.kt)
- 테마(Light/Dark), Dynamic Color, 서비스별 강조 색상 설정을 위한 화면 추가.

## Verification Plan

### Automated Tests
- `ThemeTest`: 다양한 테마 설정값에 따른 ColorScheme 적용 여부 검증.

### Manual Verification
1. 설정 화면에서 Dynamic Color ON/OFF 시 UI 색상 즉각 반영 확인.
2. 다크 모드/라이트 모드 전환 시 서비스별 아이콘 가독성 확인.
3. 임시 크래시 코드 삽입 후 앱 실행하여 크래시 발생 및 Crashlytics 보고 확인.
