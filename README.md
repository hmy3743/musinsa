# Musinsa 채용 과제 프로젝트

이 프로젝트는 무신사의 채용 과제 프로젝트로, 과제 1번부터 4번까지 모두 구현되었습니다. Kotlin과 Spring Boot를 사용하여 개발되었으며, H2 데이터베이스를 사용하여 데이터를 관리합니다. Swagger를 통해 API를 쉽게 확인하고 테스트할 수 있습니다.

## 구현 범위

이 프로젝트는 다음과 같은 기능을 포함합니다:

1. **과제 1**: 카테고리 별 최저가격 브랜드와 상품 가격, 총액을 조회하는 API
2. **과제 2**: 단일 브랜드로 모든 카테고리 상품을 구매할 때 최저가격에 판매하는 브랜드와 카테고리의 상품가격, 총액을
   조회하는 API
3. **과제 3**: 카테고리 이름으로 최저, 최고 가격 브랜드와 상품 가격을 조회하는 API

4. **과제 4**: 브랜드 및 상품을 추가 / 업데이트 / 삭제하는 API

## 코드 빌드, 테스트, 실행 방법

### Pre-requisites
- Docker version 27.4.1, build b9d17ea (사용 환경에서 정상 동작 확인)

### 빌드 방법

프로젝트는 Docker를 사용하여 빌드할 수 있습니다. 아래 명령어를 사용하여 Docker 이미지를 빌드하세요:

```bash
docker build -t musinsa:1.0 .
```

### 실행 방법

빌드된 Docker 이미지를 실행하려면 아래 명령어를 사용하세요:

```bash
docker run -p 8080:8080 --rm musinsa:1.0
```

이 명령어는 애플리케이션을 실행하고, `http://localhost:8080`에서 접속할 수 있도록 합니다.

### 테스트 실행 방법

프로젝트의 테스트는 `src/test/kotlin/myhan/musinsa` 디렉토리에서 확인할 수 있습니다. 테스트를 실행하려면 아래 명령어를 사용하세요:

```bash
./gradlew test
```

### Swagger를 통한 API 확인

애플리케이션을 실행한 후, 브라우저에서 `http://localhost:8080/swagger-ui/index.html`로 접속하면 Swagger UI를 통해 API를 확인하고 테스트할 수 있습니다.

## 기타 추가 정보

### 사용 기술 스택

- **언어**: Kotlin 2.1.0
- **JDK**: OpenJDK 21
- **프레임워크**: Spring Boot 3.4.1
- **데이터베이스**: H2
- **빌드 도구**: Gradle, Docker
- **API 문서화**: Swagger (Springdoc OpenAPI)

### 주요 설정 파일

- **`build.gradle.kts`**: 프로젝트의 빌드 설정 및 의존성 관리
- **`application.properties`**: 애플리케이션 설정 및 데이터베이스 연결 정보
- **`Dockerfile`**: Docker 이미지 빌드 및 실행 설정

### 데이터베이스 마이그레이션

Flyway를 사용하여 데이터베이스 스키마를 관리합니다. 마이그레이션 스크립트는 `src/main/resources/db/migration` 디렉토리에 위치합니다.

### jOOQ 코드 생성

jOOQ를 사용하여 데이터베이스 스키마에 기반한 타입 안전한 코드를 자동으로 생성합니다. 생성된 코드는 `build/generated-src/jooq/main` 디렉토리에 위치합니다.

### 테스트

테스트는 JUnit 5를 사용하여 작성되었으며, `src/test/kotlin/myhan/musinsa` 디렉토리에서 확인할 수 있습니다. 테스트 실행 시 H2 데이터베이스를 사용합니다.

## 결론

이 프로젝트는 무신사의 채용 과제를 위해 개발되었으며, Kotlin과 Spring Boot를 사용하여 다양한 기능을 구현했습니다. Docker를 통해 쉽게 빌드하고 실행할 수 있으며, Swagger를 통해 API를 테스트할 수 있습니다. 추가적인 질문이나 문제가 있다면 언제든지 문의해주세요.