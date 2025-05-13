## 과제 수행 논의 이력 요약

### JWT (JSON Web Token) 인증 구현

- Spring Security를 함께 사용하여 JWT 발급 및 검증 로직을 구현하는 방법.
- JwtAuthenticationFilter의 역할과 SecurityContextHolder를 통한 인증 정보 설정.
- 로그인 및 보호된 엔드포인트 접근 시 JWT가 처리되는 흐름.
- JWT Secret Key 길이 문제 및 안전한 키 설정 방법.
- JWT 발급 및 검증 흐름에 대한 시퀀스 다이어그램 표현.

### 테스트 코드 작성 및 디버깅

- MockMvc를 사용한 API 테스트 코드 작성 방법.
- 테스트 실패 시 발생하는 AssertionError (예: 상태 코드 불일치) 원인 분석 및 해결.
- java.lang.NullPointerException 발생 원인 진단 (특히 SecurityContextHolder 관련) 및 해결 방법.
- 테스트 환경 (@SpringBootTest, @AutoConfigureMockMvc) 설정 및 주의사항 (수동 MockMvc 설정 문제 등).
- Todos API CRUD 흐름에 대한 통합 테스트 코드 작성.

### 데이터베이스 초기화 (SQLite3)

- schema.sql 및 data.sql 스크립트를 사용한 데이터베이스 스키마 및 데이터 초기화 방법.
- Spring Boot의 spring.sql.init.mode 설정 활용.
- JPA spring.jpa.hibernate.ddl-auto 설정 (update, none 등)과 스크립트 초기화의 관계.
- 다른 PC에서 실행 가능하도록 초기화 설정 적용.
- Entity 정의를 기반으로 schema.sql 작성.

### API 명세 작성

- 작성하신 Users 및 Todos 관련 REST API 엔드포인트의 기능, 요청/응답 형식 등을 README.md에 바로 사용할 수 있는 Markdown 형태로 요약.

### 애플리케이션 배포

- GitHub 제출 관련 논의 (소스 코드 ZIP 다운로드 및 제출 방법).
