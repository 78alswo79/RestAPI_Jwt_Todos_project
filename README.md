# 1. 실행 방법
### 1-3. SQLite3 초기화 관련 소스
``` 
- schema.sql
- application.properties 
  애플리케이션 실행하면 자동으로 설정 읽습니다.
- 위치 : src/main/resources
```

### 1-2. JDK version 맞추기
```
	version 11
```

### 1-3. SpringBoot Gradle 프로젝트 임포트
```
	IDE툴 실행 > Import projects > Gradle > Exiting Gradle Project
```

### 1-4. 테스트코드 설명
```
1. UserApiAndJwtAuthFlowTest.java
  - 위치 : src/test/java 하위 패키지 경로
  - 테스트 프로세스 : 회원가입->로그인->JWT 인증 흐름 테스트
  
2. TodosApiCrudFlowTest
  - 위치 : src/test/java 하위 패키지 경로
  - 테스트 프로세스 : TODO 생성->목록조회->수정->삭제 테스트

상세 설명은 2.Rest API 요약 명세 참조.
```

# 2. Rest API 요약 명세

### 2-1. 사용자 회원 가입 (Sign Up)
*   **URL:** `/users/signup`
*   **메소드:** `POST`
*   **설명:** 새로운 사용자를 시스템에 등록합니다.
*   **요청 본문 (Request Body):**
    *   MediaType: `application/json`
    *   `UsersEntity` 객체 (`userId`, `password` 필드를 포함하며, 필수 필드가 추가될 수 있음)

    ```json
    {
      "userId": "새로_가입할_사용자_아이디",
      "password": "새로_가입할_사용자_비밀번호"
      // UsersEntity에 정의된 다른 필수 또는 선택 필드 추가 (예: "email": "test@example.com")
    }
    ```
*   **요청 헤더 (Request Headers):** 필수 헤더 없음 (JWT 토큰 없이 접근)
*   **응답 (Responses):**
    *   `201 Created`: 회원 가입 성공.
        *   본문: 성공 메시지 문자열 (예: "회원 가입 성공!!")
    *   `400 Bad Request`:
        *   사용자 아이디가 이미 존재할 경우. (본문: "사용자 아이디가 이미 존재합니다.")
        *   필수 입력 값이 누락되었거나 형식이 잘못된 경우. (본문: "필수 체크값이 빠져있습니다. 확인 바랍니다.")
    *   `500 Internal Server Error`: 사용자 정보 저장 중 예외 발생 시.
        *   본문: 오류 메시지 문자열 (예: "예외가 발생했습니다.!")



### 2-2. 사용자 로그인 (Login)
*   **URL : ** `/users/login`
*   **메소드 : ** `POST`
*   **설명 : ** 등록된 사용자의 인증 정보를 확인하고, 성공 시 JWT 토큰을 발급합니다.
*   **요청 본문 (Request Body):**
    *   MediaType: `application/json`
    *   `LoginRequest` 객체 (`userId`, `password` 필드를 포함할 것으로 예상)

    ```json
    {
      "userId": "사용자_아이디",
      "password": "사용자_비밀번호"
    }
    ```
*   **요청 헤더 (Request Headers) : ** 필수 헤더 없음 (JWT 토큰 없이 접근)
*   **응답 (Responses) : **
    *   `201 Created` : 로그인 성공 및 JWT 토큰 발급.
        *   본문 : `JwtResponse` 객체 (`accessToken` 필드를 포함할 것으로 예상)
        ```json
        {
          "access_Token" : "발급된_JWT_토큰_문자열"
        }
        ```
    *   `400 Bad Request` : 필수 입력 값이 누락되었거나 형식이 잘못된 경우.
        *   본문: 오류 메시지 문자열 (예: "필수 체크값이 빠져있습니다. 확인 바랍니다.")
    *   `404 Not Found` : 사용자 정보가 일치하지 않아 인증에 실패한 경우.
        *   본문: 오류 메시지 문자열 (예: "로그인 실패: 사용자 정보 불일치")
        

### 2-3. 내 정보 조회 (Get My Info)
*   **URL : ** `/users/me`
*   **메소드 : ** `GET`
*   **설명 : ** 현재 로그인된 (JWT 인증이 완료된) 사용자의 정보를 조회합니다.
*   **요청 본문 (Request Body):** 없음
*   **요청 헤더 (Request Headers):**
    *   필수 : `Authorization: Bearer [JWT 토큰]` (로그인 후 발급받은 유효한 JWT 토큰 포함)
*   **응답 (Responses):**
    *   `200 OK` :  내 정보 조회 성공.
        *   본문 : 성공 메시지 및 사용자 ID 문자열 (예: "내 정보 조회 성공!! (JWT 유효 및 인증 완료) 사용자_아이디")
    *   `401 Unauthorized`: 유효한 JWT 토큰이 없거나 인증에 실패한 경우. (Spring Security 설정에 의해 반환)
        *   본문 : 오류 메시지 (Spring Security 기본 또는 커스텀 설정에 따름)
    *   `404 Not Found`: JWT 토큰의 사용자 ID에 해당하는 사용자가 시스템에 없는 경우.
        *   본문 : 오류 메시지 문자열 (예: "유저 정보가 존재하지 않습니다.")


### 2-4. 내 정보 수정 (Update My Info)
*   **URL : ** `/users/me`
*   **메소드 : ** `PUT`
*   **설명 : ** 현재 로그인된 (JWT 인증이 완료된) 사용자의 정보를 수정합니다.
*   **요청 본문 (Request Body):**
    *   MediaType : `application/json`
    *   수정할 사용자 정보 (`UsersEntity` 객체를 사용하며, `userId`, `password` 등을 포함할 것으로 예상. 코드상으로는 `userId`, `password`만 명시적으로 사용)

    ```json
    {
      "userId" : "새로운_사용자_아이디",
      "password" : "새로운_사용자_비밀번호"
      // 필요한 경우 다른 수정 가능한 필드 추가
    }
    ```
*   **요청 헤더 (Request Headers):**
    *   필수 : `Authorization: Bearer [JWT 토큰]` (로그인 후 발급받은 유효한 JWT 토큰 포함)
*   **응답 (Responses):**
    *   `200 OK`: 내 정보 수정 성공.
        *   본문 : 성공 메시지 및 수정된 사용자 정보 문자열 (예: "내 정보 수정 성공!! UsersEntity(...)")
    *   `400 Bad Request`: 필수 입력 값이 누락되었거나 형식이 잘못된 경우.
        *   본문 : 오류 메시지 문자열 (예: "필수 체크값이 빠져있습니다. 확인 바랍니다.")
    *   `401 Unauthorized`: 유효한 JWT 토큰이 없거나 인증에 실패한 경우. (Spring Security 설정에 의해 반환)
        *   본문 : 오류 메시지 (Spring Security 기본 또는 커스텀 설정에 따름)
    *   `404 Not Found`: JWT 토큰의 사용자 ID에 해당하는 사용자가 시스템에 없는 경우.
        *   본문 : 오류 메시지 문자열 (예: "유저 정보가 존재하지 않습니다.")
    *   `500 Internal Server Error`: 데이터베이스 접근, JPA/Hibernate 오류 등 서버 내부 오류 발생 시.
        *   본문 : 오류 메시지 문자열 (예: "DB에 접근할 수 없습니다!", "Jpa/Hibernate에 예외가 발생했습니다!", "예외가 발생했습니다.!")
        

### 2-5. 내 정보 삭제 (Delete My Info)
*   **URL:** `/users/me`
*   **메소드:** `DELETE`
*   **설명:** 현재 로그인된 (JWT 인증이 완료된) 사용자의 계정을 삭제합니다.
*   **요청 본문 (Request Body):** 없음
*   **요청 헤더 (Request Headers):**
    *   필수: `Authorization: Bearer [JWT 토큰]` (로그인 후 발급받은 유효한 JWT 토큰 포함)
*   **응답 (Responses):**
    *   `200 OK`: 내 정보 삭제 성공 (deleteCnt > 0 일 때).
        *   본문: 성공 메시지 문자열 (예: "내 정보 삭제 성공!!")
    *   `401 Unauthorized`: 유효한 JWT 토큰이 없거나 인증에 실패한 경우. (Spring Security 설정에 의해 반환)
        *   본문: 오류 메시지 (Spring Security 기본 또는 커스텀 설정에 따름)
    *   `404 Not Found`: JWT 토큰의 사용자 ID에 해당하는 사용자가 시스템에 없는 경우.
        *   본문: 오류 메시지 문자열 (예: "유저 정보가 존재하지 않습니다.")
    *   `500 Internal Server Error`: 데이터베이스 접근 또는 삭제 실패 시.
        *   본문: 오류 메시지 문자열 (예: "삭제하려는 UserId가 없습니다!", "예외가 발생했습니다.!", "내 정보 삭제를 실패했습니다!!")
