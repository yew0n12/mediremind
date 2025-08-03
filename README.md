# MediRemind

**MediRemind**는 사용자의 약 복용 습관을 돕기 위한 **안드로이드 기반 약 복용 알림 앱**입니다.  
복용 기록을 한눈에 보고, 잊지 않고 챙길 수 있도록 도와줍니다.

---
## 프로젝트 기간

- 개발 기간: 2025.03 ~ 2025.08
---

## 주요 기능

### 구현 완료
- **홈 화면에서 오늘 복용할 약 리스트 표시**
- **약 추가 및 삭제 기능**
- **약 복용 스케줄 주기 설정 (매일/특정 요일 등)**
- **RecyclerView 기반 약 목록 표시**
- **Room Database 연동을 통한 데이터 저장 및 관리**

### 구현 중인 기능
- **약 복용 시간 알림 기능 (Notification)**
- **건강 습관 등록 기능 (매일)** 

---

## 기술 스택

- **Language**: Kotlin  
- **Architecture**: MVVM (Model-View-ViewModel)  
- **UI Framework**: Android XML Layout  
- **Database**: Room (SQLite 기반)  
- **Notification**: Android AlarmManager + NotificationCompat  
- **Etc**: ViewModel, LiveData, RecyclerView, Fragment

---

## 앱 실행 화면

| 홈 화면 | 약 추가 화면 | 알림 예시 |
|---------|---------------|-----------|
| <img src="https://github.com/user-attachments/assets/fe6d4fa5-e0e5-489d-8715-084e1bdaf89a" width="250"/> | <img src="https://github.com/user-attachments/assets/d889c8b9-429e-44d5-8eb1-722acd8bd715" width="250"/> | <img src="https://github.com/user-attachments/assets/43c2e386-8c96-4dd4-aead-c3125b9f67fa" width="250"/> |


---

## 실행 방법
### 코드 실행 
1. Android Studio에서 프로젝트 클론
2. `app/src/main/java/com/example/mediremind` 내 주요 클래스 확인
3. 에뮬레이터 또는 실제 기기에서 실행

### APK 다운로드  
[![Download APK](https://img.shields.io/badge/Download-APK-blue?logo=android)](./app-release.apk)

- 위 버튼을 클릭하여 APK 파일을 다운로드한 후, 안드로이드 휴대폰에서 설치해 주세요.
- **앱 권한 설정**이 필요할 수 있습니다.
---

## 팀원 소개

| 이름 | GitHub ID | 역할 |
|------|-----------|------|
| **나예원** | [yew0n12](https://github.com/yew0n12) | 팀장, 앱 구조 설계 & 메인 화면 개발 |
| 김동훈 | [donghunKim-ai](https://github.com/donghunKim-ai) | 약 복용 관리 기능 개발 |
| 정원영 | [lenis02](https://github.com/lenis02) | 앱 구조 설계 & 메인 화면 개발 |
| 박시훈 | [sihoon-0077](https://github.com/sihoon-0077) | 약 복용 관리 기능 개발 |
| 박예나 | [Yena114](https://github.com/Yena114) | 알림 기능 & 건강 습관 기록 개발 |
| 오영서 | [YoungSeo-coder](https://github.com/YoungSeo-coder) | 알림 기능 & 건강 습관 기록 개발 |




