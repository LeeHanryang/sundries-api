# TodoList 서비스

---

## ✅ 웹 UI 및 API 문서

* 🌐 **웹 UI**: [http://34.44.10.121](http://34.44.10.121)
* 📘 **Swagger 문서**: [http://34.44.10.121:8080/swagger-ui/index.html](http://34.44.10.121:8080/swagger-ui/index.html)

---

## 🔐 개발용 OAuth2.0 인증 안내

| 플랫폼        | 지원 여부 | 설명                                              |
|------------|-------|-------------------------------------------------|
| 🟢 **카카오** | 정상 작동 | 클라이언트 등록 후 즉시 사용 가능                             |
| 🔴 **구글**  | 제한됨   | 공개 서버에서 사용하려면 **최상위 도메인 필요** (예: `example.com`) |
| 🟡 **네이버** | 설정 필요 | **네이버 개발자센터에 테스트 할 ID 등록 필요**                   |

> 로컬 환경에서는 카카오를 제외한 OAuth2이 로그인 정상 작동합니다.
> 공개 URL에서는 **카카오 사용을 권장**합니다.

---

## 개요

Spring Boot 기반의 TodoList RESTful API 프로젝트입니다. 로그인 방식으로 JWT 인증과 OAuth2(구글, 네이버, 카카오)를 지원하며, SQLite3를 데이터베이스로 사용합니다.

* **Spring Boot 버전**: 3.2.3
* **Java 버전**: 17
* **빌드 도구**: Gradle
* **데이터베이스**: SQLite3 (JPA 사용)
* **인증 방식**: Spring Security (JWT) 및 OAuth2.0
* **DB 모드**: `create-drop` (애플리케이션 실행 시 DB 생성, 종료 시 삭제)

## 주요 기능

1. **회원 가입 / 로그인**

    * 이메일/비밀번호 기반
    * JWT 토큰 발급 및 검증

2. **OAuth2 로그인**

    * Google, Naver, Kakao OAuth2 제공자 연동
    * 소셜 계정 최초 로그인 시 사용자 정보 저장 **[⚠️회원 가입 생략]**
    * 소셜 계정 이메일 존재 시 연동 **[⚠️동의 화면 생략]**

3. **Todo 관리**

    * CRUD(Create, Read, Update, Delete)
    * JWT 인증 필요

4. **계정 관리**

    * CRUD(Read, Update, Delete)
    * JWT 인증 필요

## 기술 스택

| Layer  | 기술                           |
|--------|------------------------------|
| API 서버 | Spring Boot 3.2.3            |
| 언어     | Java 17                      |
| 빌드 도구  | Gradle                       |
| 데이터 접근 | Spring Data JPA              |
| 데이터베이스 | SQLite3 (`create-drop` 모드)   |
| 보안     | Spring Security, JWT, OAuth2 |
| 문서화    | Swagger UI                   |

## 요구사항

* Java 17 이상
* Gradle
* Internet 연결 (OAuth2 인증)

## 설치 및 실행

⚠️ **주의사항**
> * 클론한 경로나 프로젝트 디렉토리 경로에 **공백 또는 한글**이 포함되어 있으면 빌드 및 SQLite 파일 접근에 문제가 발생할 수 있습니다.
> * 예: `C:/Users/홍길동/문서` → ❌, `C:/dev/todolist` → ✅

1. 레포지토리 클론

   ```bash
   git clone <레포지토리 URL>
   cd <프로젝트 디렉토리>
   ```

2. 의존성 다운로드 및 빌드

   ```bash
   ./gradlew build
   ```

3. 애플리케이션 실행

   ```bash
   ./gradlew bootRun
   ```

   또는 `.jar` 실행:

   ```bash
   java -jar build/libs/TodoList-0.0.1-SNAPSHOT.jar
   ```

4. 실행 후 자동으로 SQLite3 파일 기반 DB가 생성되며, **종료 시 삭제**됩니다.

> ⚠️ **리눅스 환경에서 실행 시 주의:**
> SQLite는 파일 기반 DB이므로 실행 디렉토리(`/src/main/resources/db`)가 존재하지 않으면 애플리케이션이 정상 실행되지 않을 수 있습니다.
> **Windows 또는 macOS 환경에서의 실행을 권장**합니다.
---

## 🧪 테스트 코드 커버리지 확인 명령어

   ```bash
   ./gradlew clean test jacocoTestReport
   ```

↳ 리포트 위치: [디렉터리/build/reports/jacoco/test/html/index.html]
> ℹ️ `main()` 함수 기반의 실행 테스트는 실제 서버 기동을 포함하므로, 테스트 커버리지 집계에서 **제외**하였습니다.
>

## 추가 설정

* 필요 시 `application.properties`에서 OAuth2 클라이언트 ID, 시크릿 등을 설정해주세요.
* JWT key, 만료 시간 등도 설정할 수 있습니다.

---