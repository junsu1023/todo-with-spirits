# todo-with-spirits(Andoid)

### ✉️ Commit Message Convention
| 태그 | 설명 |
| :--- | :--- |
| feat | 새로운 기능 추가 |
| fix | 버그 수정 |
| docs | 문서 수정 |
| refactor | 코드 리펙토링 |
| chore | 설정 변경 |

모노레포이기 때문에 태그 뒤 (android)를 붙여 스코프를 명시하기 

### 🧩 Module Overview

| 모듈 | 타입 | 역할 및 설명 |
| :--- | :--- | :--- |
| **[:app](android/app)** | `com.android.application` | **Presentation Layer**: 앱의 진입점. UI(Compose), Navigation, ViewModels가 위치합니다. |
| **[:domain](android/domain)** | `kotlin-library` | **Domain Layer**: 비즈니스 로직의 핵심. UseCase, 엔티티(Entity), 리포지토리 인터페이스를 포함하며 안드로이드 의존성이 없는 순수 Kotlin 모듈입니다. |
| **[:data](android/data)** | `com.android.library` | **Data Layer**: 데이터 소스 관리. API 통신(Retrofit), DI 설정, 로컬 DB(Room), 리포지토리 구현체 및 데이터 모델(DTO)이 위치합니다. |
| **[:core](android/core)** | `com.android.library` | **Common Layer**: 프로젝트 전반에서 사용되는 공통 유틸리티. Base 클래스, 확장 함수, 공통 UI 컴포넌트, 디자인 시스템 등을 포함합니다. |
