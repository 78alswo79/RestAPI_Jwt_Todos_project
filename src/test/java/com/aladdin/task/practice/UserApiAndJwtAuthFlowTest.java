package com.aladdin.task.practice;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.aladdin.task.practice.entity.UsersEntity;
import com.aladdin.task.practice.repository.UsersRepository;
import com.aladdin.task.practice.vo.LoginRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.TestInstance.Lifecycle; // Lifecycle 임포트

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(Lifecycle.PER_CLASS) // <-- 추가: @AfterAll에서 @Autowired 사용 가능하게 함
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)  // 메소드 실행 순서를 직접 입력한다는 뜻.
@DisplayName("User API 및 JWT 인증 흐름 테스트") // 테스트 클래스 이름 설정
public class UserApiAndJwtAuthFlowTest {

	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Autowired
    private UsersRepository usersRepository;

	
	// 테스트에서 사용할 사용자 정보
    private String testUserId = "testuser123";
    private String testPassword = "testpassword";
    
    // 테스트 간 JWT 토큰 값을 저장할 변수
    private String jwtAccessToken = null;
    
    @Test
    @Order(1)		// 가장 먼저 실행됨.
    @DisplayName("1. 회원 가입 테스트")
    public void testSignup() throws JsonProcessingException, Exception {
    	UsersEntity newUser = new UsersEntity();
    	
    	newUser.setUserId(testUserId);
    	newUser.setPassword(testPassword);
    	
    	mockMvc.perform(post("/users/signup")
                .contentType(MediaType.APPLICATION_JSON) // 요청 본문 타입 JSON
                .content(objectMapper.writeValueAsString(newUser))) // UsersEntity 객체를 JSON 문자열로 변환하여 본문에 담음
                .andExpect(status().isCreated()) // 또는 .andExpect(status().isCreated()) - 응답 상태 코드 200 또는 201 기대
                .andExpect(content().string("회원 가입 성공!!")); // 응답 본문
    }
    
    @Test
    @Order(2)
    @DisplayName("2. 로그인 및 JWT 토큰 발급 테스트")
    public void testLoginAndJwtTokenGeneration() throws JsonProcessingException, Exception {
    	LoginRequest loginRequest = new LoginRequest();
    	loginRequest.setUserId(testUserId);
    	loginRequest.setPassword(testPassword);
    	
    	MvcResult result = mockMvc.perform(post("/users/login")
    			.contentType(MediaType.APPLICATION_JSON)
    			.content(objectMapper.writeValueAsString(loginRequest)))
    			.andExpect(status().isCreated())
    			.andExpect(jsonPath("$.access_token").exists())
    			.andExpect(jsonPath("$.type").value("Bearer"))
    			.andReturn();
    	
    	 		// 응답 본문에서 access_token 값 추출
		        String responseBody = result.getResponse().getContentAsString();
		        // LoginResponse DTO (access_token, type 필드 포함)가 있다면 해당 클래스로 매핑 가능
		        // JwtResponse jwtResponse = objectMapper.readValue(responseBody, JwtResponse.class);
		        // jwtAccessToken = jwtResponse.getAccessToken();
		
		        // JSON 문자열에서 access_token 필드 값만 직접 추출 (간단한 방법)
		        jwtAccessToken = objectMapper.readTree(responseBody).get("access_token").asText();
		
		        // 추출된 토큰 값이 null이 아니거나 비어있지 않은지 검증
		        org.junit.jupiter.api.Assertions.assertNotNull(jwtAccessToken, "JWT Access Token은 null이 아니어야 합니다.");
		        org.junit.jupiter.api.Assertions.assertFalse(jwtAccessToken.isEmpty(), "JWT Access Token은 비어있지 않아야 합니다.");
    }
    
    
    @Test
    @Order(3)
    @DisplayName("3. JWT 토큰을 사용한 보호된 엔드포인트 접근 테스트 (성공)")
    void testProtectedEndpointWithValidJwt() throws Exception {
        // 로그인 테스트에서 발급받은 JWT 토큰 사용
        org.junit.jupiter.api.Assertions.assertNotNull(jwtAccessToken, "JWT Access Token이 미리 발급되어 있어야 합니다.");

        mockMvc.perform(get("/users/me")
                .header("Authorization", "Bearer " + jwtAccessToken)) 	// Authorization 헤더에 "Bearer [토큰]" 형식으로 토큰 포함
                .andExpect(status().isOk()) 							// 응답 상태 코드 200 기대
                .andExpect(content().string(org.hamcrest.Matchers.containsString("내 정보 조회 성공!!")));
    }

     @Test
     @Order(4) // JWT 인증 성공 테스트 다음으로 실행
     @DisplayName("4. JWT 토큰 없이 보호된 엔드포인트 접근 테스트 (실패 - 401 Unauthorized)")
     void testProtectedEndpointWithoutJwt() throws Exception {
         mockMvc.perform(get("/users/me")) // Authorization 헤더 없이 요청
                 .andExpect(status().isUnauthorized()); // 401 Unauthorized 상태 코드 기대
     }

     @Test
     @Order(5) // JWT 인증 성공 테스트 다음으로 실행
     @DisplayName("5. 잘못된 JWT 토큰으로 보호된 엔드포인트 접근 테스트 (실패 - 401 Unauthorized)")
     void testProtectedEndpointWithInvalidJwt() throws Exception {
         String invalidToken = "invalid.jwt.token"; // 잘못된 형식 또는 서명의 토큰

         mockMvc.perform(get("/users/me")
                 .header("Authorization", "Bearer " + invalidToken)) // 잘못된 토큰 사용
                 .andExpect(status().isUnauthorized()); // 401 Unauthorized 상태 코드 기대
     }
    
    @AfterAll
    @DisplayName("테스트 데이터 정리")
    @Transactional // 삭제 작업도 트랜잭션 내에서 실행되도록 함
    void cleanupTestData() {
        log.info("모든 테스트 완료 후 테스트 데이터 정리 실행: {}", testUserId);
        // UsersRepository를 사용하여 특정 사용자 삭제
        long deletedCount = usersRepository.deleteByUserId(testUserId); // userId를 기준으로 삭제
        if (deletedCount > 0) {
             log.info("테스트 사용자 삭제 성공: userId: {}", testUserId);
        } else {
             log.warn("테스트 사용자 삭제 대상 없음: userId: {}", testUserId);
        }
    }
}
