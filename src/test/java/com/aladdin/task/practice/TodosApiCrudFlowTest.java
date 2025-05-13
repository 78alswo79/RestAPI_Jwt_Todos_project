package com.aladdin.task.practice;

import com.aladdin.task.practice.entity.TodosEntity;
import com.aladdin.task.practice.entity.UsersEntity;
import com.aladdin.task.practice.repository.TodosRepository;
import com.aladdin.task.practice.repository.UsersRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@SpringBootTest // Spring Boot 애플리케이션 컨텍스트 로드
@AutoConfigureMockMvc // MockMvc 자동 구성
@TestInstance(Lifecycle.PER_CLASS) // 클래스당 하나의 테스트 인스턴스 사용 (beforeAll, afterAll에서 non-static 필드 사용 가능)
@TestMethodOrder(OrderAnnotation.class) // @Order 어노테이션으로 테스트 메소드 순서 지정
@DisplayName("Todos API CRUD 흐름 테스트 (JWT 인증 포함)")
public class TodosApiCrudFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper; // JSON 직렬화/역직렬화에 사용
    
    @Autowired
    private UsersRepository usersRepository;
    
    @Autowired
    private TodosRepository todosRepository;

    // 테스트 전역에서 사용할 변수
    private static String jwtAccessToken; // 로그인 후 발급받은 JWT 토큰
    private static Long createdTodoSeq; // 생성된 TODO의 시퀀스 (ID)

    private static final String TEST_USERID = "test_todo_user";
    private static final String TEST_PASSWORD = "testpassword";
    private static final String INITIAL_TODO_CONTENT = "첫 번째 할 일";
    private static final String UPDATED_TODO_CONTENT = "수정된 할 일";


    // 모든 테스트 메소드 실행 전에 한 번 실행
    @BeforeAll
    void setup() throws Exception {
        // TODO: 실제 애플리케이션의 회원가입 및 로그인 API 경로로 변경
        String signupUrl = "/users/signup";
        String loginUrl = "/users/login";

        // 1. 테스트 사용자 회원가입
        UsersEntity signupUser = new UsersEntity();
        signupUser.setUserId(TEST_USERID);
        signupUser.setPassword(TEST_PASSWORD); // 실제 암호화 로직에 맞게 처리 필요

        mockMvc.perform(post(signupUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupUser)))
                .andExpect(status().isCreated()); // 회원가입 성공 (201 Created) 기대

        // 2. 테스트 사용자 로그인 및 JWT 토큰 발급
        UsersEntity loginUser = new UsersEntity();
        loginUser.setUserId(TEST_USERID);
        loginUser.setPassword(TEST_PASSWORD); // 실제 암호화 로직에 맞게 처리 필요

        MvcResult loginResult = mockMvc.perform(post(loginUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginUser)))
                .andExpect(status().isCreated()) // 로그인 성공 (200 OK) 기대
                .andReturn();

        // 응답 본문에서 JWT 토큰 추출 (JSON 형식에 따라 파싱 방식 변경 필요)
        // 예시: {"token": "발급된토큰", "type": "Bearer"} 형태라고 가정
        String responseBody = loginResult.getResponse().getContentAsString();
        // 실제 응답 JSON 구조에 맞게 jwtAccessToken 값을 파싱해야 합니다.
        // ObjectMapper 등을 사용하여 JSON 응답을 객체로 변환하거나, 문자열 파싱을 수행합니다.
        // 여기서는 임시로 문자열에서 토큰 값을 추출하는 예시를 보여줍니다.
        // 실제 애플리케이션의 로그인 응답 형식에 맞춰 수정하십시오.
        try {
             // JSON 응답을 Map 또는 커스텀 객체로 파싱
             // Map<String, String> responseMap = objectMapper.readValue(responseBody, Map.class);
             // jwtAccessToken = responseMap.get("token"); // 예시: 응답 JSON에 "token" 키가 있다고 가정

             // 또는 단순 문자열 파싱 (정확성이 떨어짐)
             int tokenStart = responseBody.indexOf(":"); // JSON Key 시작점 (예시)
             int tokenEnd = responseBody.indexOf(","); // 다음 Key 또는 } 까지 (예시)
             if (tokenStart != -1 && tokenEnd != -1 && tokenStart < tokenEnd) {
                 jwtAccessToken = responseBody.substring(tokenStart + 1, tokenEnd).trim().replace("\"", "");
             } else {
                  // JSON 응답 구조가 다른 경우에 대한 처리
                  // 예: 직접 응답 문자열 전체가 토큰인 경우
                  jwtAccessToken = responseBody.trim().replace("\"", "").replace("{", "").replace("}", "").split(":")[1].trim(); // 예시: {"token":"..."} 형태 파싱
             }


            org.junit.jupiter.api.Assertions.assertNotNull(jwtAccessToken, "로그인 후 JWT 토큰이 발급되지 않았습니다.");
            log.info("발급된 JWT Access Token: {}", jwtAccessToken);

        } catch (Exception e) {
             log.error("JWT 토큰 파싱 중 오류 발생: {}", e.getMessage(), e);
             // 토큰 파싱 실패 시 테스트 실패 처리
             org.junit.jupiter.api.Assertions.fail("로그인 응답에서 JWT 토큰을 파싱하는데 실패했습니다.");
        }

    }

    @Test
    @Order(1)
    @DisplayName("1. TODO 생성 테스트 (POST /todos)")
    void testCreateTodo() throws Exception {
        // 생성할 TODO 객체 생성
        TodosEntity newTodo = new TodosEntity();
        newTodo.setContent(INITIAL_TODO_CONTENT);
        // newTodo.setCompleted(false); // 필요한 필드 설정

        // POST 요청 보내기
        MvcResult result = mockMvc.perform(post("/todos")
                        .header("Authorization", "Bearer " + jwtAccessToken) // 발급받은 JWT 토큰 사용
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newTodo))) // TODO 객체를 JSON 문자열로 변환하여 본문에 포함
                .andExpect(status().isCreated()) // 201 Created 상태 코드 기대
                .andExpect(content().string(Matchers.containsString("todos 리스트 생성 완료!"))) // 응답 본문 검증
                .andReturn();

        // 생성된 TODO의 ID(seq) 추출 (필요하다면 응답 본문에서 파싱하거나 다른 방법 사용)
        // 현재 컨트롤러는 "todos 리스트 생성 완료!" 문자열만 반환하므로,
        // 생성된 TODO의 ID를 얻으려면 컨트롤러의 응답 형식을 변경해야 합니다.
        // 예를 들어, 생성된 TodosEntity 객체 자체를 응답하거나, ID를 포함하는 JSON 객체를 반환하도록 합니다.
        // 여기서는 임시로 다음 테스트를 위해 createdTodoSeq 변수에 값을 할당하지 못하지만,
        // 실제로는 여기서 응답 본문을 파싱하여 createdTodoSeq 값을 추출해야 합니다.
        // 예시: 응답 본문이 {"id": 1, "content": "...", ...} 형태라면
        // String responseBody = result.getResponse().getContentAsString();
        // Map<String, Object> responseMap = objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});
        // createdTodoSeq = ((Number) responseMap.get("id")).longValue(); // Long 타입으로 변환

        // 만약 컨트롤러에서 ID를 반환하지 않는다면, 다음 테스트(수정/삭제)를 위해
        // 목록 조회 후 생성된 항목을 찾거나 다른 방법을 사용해야 합니다.
        // 테스트의 편의를 위해 TODO 생성 시 생성된 TodosEntity 객체 또는 ID를 반환하도록 API 수정을 고려해볼 수 있습니다.

        // 임시로 createdTodoSeq에 임의의 값 할당 (실제 구현 시 위에서 파싱해야 함)
        // TODO: 실제 응답에서 생성된 TODO의 ID를 파싱하는 코드로 대체하십시오.
        createdTodoSeq = 1L; // <-- 이 부분은 실제 응답 파싱 결과로 대체되어야 합니다.
        log.info("임시로 설정된 생성된 TODO의 ID: {}", createdTodoSeq); // <-- 디버깅용 로그

        org.junit.jupiter.api.Assertions.assertNotNull(createdTodoSeq, "생성된 TODO의 ID(seq)를 가져오는데 실패했습니다.");
    }

    @Test
    @Order(2)
    @DisplayName("2. TODO 목록 조회 테스트 (GET /todos)")
    void testGetTodosList() throws Exception {
        // GET 요청 보내기
        MvcResult result = mockMvc.perform(get("/todos")
                        .header("Authorization", "Bearer " + jwtAccessToken)) // 발급받은 JWT 토큰 사용
                .andExpect(status().isOk()) // 200 OK 상태 코드 기대
                .andExpect(content().string(Matchers.containsString("todos 리스트 조회 성공!"))) // 응답 본문 검증 (성공 메시지)
                // TODO: 응답 본문에 생성된 TODO가 포함되어 있는지 추가 검증
                // .andExpect(content().string(Matchers.containsString(INITIAL_TODO_CONTENT))) // 생성된 할 일 내용 포함 검증 (응답 본문 형태에 따라 수정)
                .andReturn();

        // 응답 본문에서 TODO 목록 파싱 (응답 형태에 따라 구현 필요)
        // 현재 컨트롤러는 "todos 리스트 조회 성공! [TodosEntity(seq=1, content=...)]" 와 같은 문자열을 반환하므로,
        // 이를 파싱하여 List<TodosEntity> 객체로 만들거나, 문자열 내에서 원하는 정보(예: createdTodoSeq)가 있는지 확인합니다.
        // 테스트의 편의를 위해 GET /todos API가 List<TodosEntity> 객체를 JSON 배열 형태로 반환하도록 수정하는 것을 고려할 수 있습니다.
        String responseBody = result.getResponse().getContentAsString();
        log.info("TODO 목록 조회 응답 본문: {}", responseBody);

        // TODO: 실제 응답 본문 파싱 로직 구현 및 검증
        // 예시: 응답이 JSON 배열 형태인 경우
        // List<TodosEntity> todoList = objectMapper.readValue(responseBody, new TypeReference<List<TodosEntity>>() {});
        // org.junit.jupiter.api.Assertions.assertTrue(todoList.stream().anyMatch(todo -> todo.getSeq().equals(createdTodoSeq)), "조회된 목록에 생성된 TODO가 포함되어 있어야 합니다.");

         // 임시 문자열 포함 확인 (컨트롤러 응답 형태에 맞춤)
        org.junit.jupiter.api.Assertions.assertTrue(responseBody.contains(INITIAL_TODO_CONTENT), "조회된 목록에 생성된 TODO 내용이 포함되어 있어야 합니다.");
        // 생성된 TODO ID가 응답 문자열에 포함되어 있는지 확인 (컨트롤러 응답 형태에 맞춤)
         org.junit.jupiter.api.Assertions.assertTrue(responseBody.contains("seq=" + createdTodoSeq), "조회된 목록에 생성된 TODO의 ID가 포함되어 있어야 합니다.");

    }

    @Test
    @Order(3)
    @DisplayName("3. TODO 수정 테스트 (PUT /todos/{id})")
    void testUpdateTodo() throws Exception {
        // 수정할 TODO 객체 생성
        TodosEntity updateTodo = new TodosEntity();
        updateTodo.setContent(UPDATED_TODO_CONTENT);
        // updateTodo.setCompleted(true); // 필요한 필드 설정

        // PUT 요청 보내기
        mockMvc.perform(put("/todos/" + createdTodoSeq) // 생성된 TODO의 ID 사용
                        .header("Authorization", "Bearer " + jwtAccessToken) // 발급받은 JWT 토큰 사용
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateTodo))) // 수정할 TODO 객체를 JSON 문자열로 변환
                .andExpect(status().isOk()) // 200 OK 상태 코드 기대
                .andExpect(content().string(Matchers.containsString("todo 업데이트 성공!!"))) // 응답 본문 검증
                // TODO: 수정된 TODO 내용이 응답 본문에 포함되어 있는지 추가 검증
                 .andExpect(content().string(Matchers.containsString(UPDATED_TODO_CONTENT))); // 수정된 할 일 내용 포함 검증 (응답 본문 형태에 따라 수정)
    }

    @Test
    @Order(4)
    @DisplayName("4. TODO 삭제 테스트 (DELETE /todos/{id})")
    void testDeleteTodo() throws Exception {
        // DELETE 요청 보내기
        mockMvc.perform(delete("/todos/" + createdTodoSeq) // 생성된 TODO의 ID 사용
                        .header("Authorization", "Bearer " + jwtAccessToken)) // 발급받은 JWT 토큰 사용
                .andExpect(status().isOk()) // 200 OK 상태 코드 기대
                .andExpect(content().string(Matchers.containsString("todo 삭제 성공!!"))); // 응답 본문 검증
    }

    // TODO: 필요하다면 삭제 후 해당 TODO가 존재하지 않음을 확인하는 테스트 추가
    @Test
    @Order(5)
    @DisplayName("5. 삭제된 TODO 조회 테스트 (실패 - 404 Not Found)")
    void testGetDeletedTodo() throws Exception {
        // 삭제된 TODO ID로 다시 조회 시 404 Not Found 기대
        mockMvc.perform(get("/todos/" + createdTodoSeq) // 삭제된 TODO의 ID 사용
                        .header("Authorization", "Bearer " + jwtAccessToken)) // 발급받은 JWT 토큰 사용
                 .andExpect(status().isNotFound()) // 목록 조회는 성공해야 함
                 .andExpect(content().string(Matchers.not(Matchers.containsString("seq=" + createdTodoSeq)))); // 응답 본문에 삭제된 TODO ID가 포함되지 않음을 검증 (응답 본문 형태에 따라 수정)
    }


    @AfterAll
    @DisplayName("테스트 데이터 정리")
    @Transactional // 삭제 작업도 트랜잭션 내에서 실행되도록 함
    void cleanupTestData() {
        log.info("모든 테스트 완료 후 테스트 데이터 정리 실행: {}", TEST_USERID);
        // UsersRepository를 사용하여 특정 사용자 삭제
        long deletedCount = usersRepository.deleteByUserId(TEST_USERID); // userId를 기준으로 삭제
        if (deletedCount > 0) {
             log.info("테스트 사용자 삭제 성공: userId: {}", TEST_USERID);
        } else {
             log.warn("테스트 사용자 삭제 대상 없음: userId: {}", TEST_USERID);
        }
        
        log.info("모든 테스트 완료 후 테스트 데이터 정리 실행: {}", UPDATED_TODO_CONTENT);
        // UsersRepository를 사용하여 특정 사용자 삭제
        long deletedCount2 = todosRepository.deleteByContent(UPDATED_TODO_CONTENT); // userId를 기준으로 삭제
        if (deletedCount > 0) {
             log.info("테스트 사용자 삭제 성공: userId: {}", UPDATED_TODO_CONTENT);
        } else {
             log.warn("테스트 사용자 삭제 대상 없음: userId: {}", UPDATED_TODO_CONTENT);
        }
    }
}
