package com.aladdin.task.practice.controller;

import javax.persistence.PersistenceException;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication; // 인증 결과 객체
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aladdin.task.practice.entity.UsersEntity;
import com.aladdin.task.practice.service.CommonService;
import com.aladdin.task.practice.service.UsersService;
import com.aladdin.task.practice.utils.jwt.JwtTokenProvider;
import com.aladdin.task.practice.vo.JwtResponse;
import com.aladdin.task.practice.vo.LoginRequest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/users")
public class RestUsersController {
	
	private final UsersService usersService;
	private final CommonService commonService;
	private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager; // AuthenticationManager 주입
    private final JwtTokenProvider jwtTokenProvider;
	
	public RestUsersController(UsersService usersService, PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtTokenProvider jwtTokenProvider, CommonService commonService) {
		this.usersService = usersService;
		this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.commonService = commonService;
	}


    // 회원 가입 (POST /users/signup)
    // SecurityConfig에서 permitAll() 설정
    @PostMapping("/signup")
    public ResponseEntity<String> postSignup (@RequestBody UsersEntity user) {
    	
    	if (usersService.checkDuplicateUserId(user.getUserId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("사용자 아이디가 이미 존재합니다.");
    	}
    	// 필수 값 발리데이션 체크
    	if (usersService.isNullCheck(user)) {
    		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("필수 체크값이 빠져있습니다. 확인 바랍니다.");
    	}
    	
        String rawPassword = user.getPassword();
        String encodedPassword = passwordEncoder.encode(rawPassword);
        user.setPassword(encodedPassword);

        usersService.insertUsers(user);
        
        log.info("회원 가입 사용자 : {}", user.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body("회원 가입 성공!!");
    }

    // 로그인 (POST /users/login)
    // SecurityConfig에서 permitAll() 설정
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
    	
    	// 필수 값 발리데이션 체크
    	if (usersService.isNullCheck(loginRequest)) {
    		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("필수 체크값이 빠져있습니다. 확인 바랍니다.");
    	}
    	
        try {
            // AuthenticationManager를 사용하여 인증 시도
            // loadUserByUsername(loginRequest.getUsername()) 호출 및 비밀번호 검증은 AuthenticationManager 내부에서 처리됨
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUserId(), // UserDetails.getUsername()과 매칭될 사용자 이름
                    loginRequest.getPassword()  // UserDetails.getPassword()와 비교될 원본 비밀번호
                )
            );

            // 인증 성공 시, 인증된 Authentication 객체를 Security Context에 설정 (선택 사항이지만 일반적)
            // 이후 SecurityContextHolder.getContext().getAuthentication()으로 인증 정보 접근 가능
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 인증 성공 시 JWT 토큰 생성 (Authentication 객체 사용)
            String jwt = jwtTokenProvider.createToken(authentication);

            // 클라이언트에 JWT 토큰 반환 (access_token 필드 사용)
            return ResponseEntity.status(HttpStatus.CREATED).body(new JwtResponse(jwt));

        } catch (org.springframework.security.core.AuthenticationException e) {
            // Spring Security 예외를 잡아 401 Unauthorized 응답 반환
            // UserDetailsServiceImpl의 UsernameNotFoundException 또는 DaoAuthenticationProvider의 BadCredentialsException 등
            System.err.println("Authentication failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("로그인 실패: 사용자 정보 불일치");
        }
    }

    // 내 정보 조회 (GET /users/me)
    // SecurityConfig에서 authenticated()로 보호됩니다.
    // JWT 필터를 통과하고 인증되면 이 메소드에 접근 가능합니다.
    @GetMapping("/me")
    public ResponseEntity<String> getMe() {
        // 현재 인증된 사용자 정보는 SecurityContextHolder에서 가져올 수 있습니다.
         Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
         String currentUsername = authentication.getName(); // 인증된 사용자의 username (UsersEntity.username)
         
         UsersEntity existingUser = commonService.getUser(currentUsername);
         if (existingUser == null) {
        	 return ResponseEntity.status(HttpStatus.NOT_FOUND).body("유저 정보가 존재하지 않습니다.");
         }
         
        return ResponseEntity.status(HttpStatus.OK).body("내 정보 조회 성공!! (JWT 유효 및 인증 완료) " + existingUser.getUserId());
    }

    // PUT /users/me, DELETE /users/me 등 다른 API 구현 시 필요에 따라 추가
    // 이 경로들도 SecurityConfig의 authenticated() 설정에 의해 자동 보호됩니다.
	
    @PutMapping("/me")
    public ResponseEntity<String> putMe(@RequestBody UsersEntity updateUser) {
        // 현재 인증된 사용자 정보는 SecurityContextHolder에서 가져올 수 있습니다.
         Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
         String currentUsername = authentication.getName(); // 인증된 사용자의 username (UsersEntity.username)
         
         UsersEntity existingUser = commonService.getUser(currentUsername);
         if (existingUser == null) {
        	 return ResponseEntity.status(HttpStatus.NOT_FOUND).body("유저 정보가 존재하지 않습니다.");
         }
         
         if (usersService.isNullCheck(updateUser)) {
        	 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("필수 체크값이 빠져있습니다. 확인 바랍니다.");
         }
         
         existingUser.setUserId(updateUser.getUserId());
         existingUser.setPassword(passwordEncoder.encode(updateUser.getPassword()));
         
         UsersEntity updatedUser;
         try {
        	 updatedUser = usersService.updateUsers(existingUser);
 		 } catch (DataAccessException e) {
 			 return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("DB에 접근할 수 없습니다!");
 		 } catch (PersistenceException e) {
 			 return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Jpa/Hibernate에 예외가 발생했습니다.");
 		 } catch (Exception e) {
 			 return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("예외가 발생했습니다.!");
 		}
         
        return ResponseEntity.status(HttpStatus.OK).body("내 정보 수정 성공!!" + updatedUser);
    }
    
    @DeleteMapping("/me")
    public ResponseEntity<String> deleteMe() {
        // 현재 인증된 사용자 정보는 SecurityContextHolder에서 가져올 수 있습니다.
         Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
         String currentUsername = authentication.getName(); // 인증된 사용자의 username (UsersEntity.username)

         UsersEntity existingUser = commonService.getUser(currentUsername);
         if (existingUser == null) {
        	 return ResponseEntity.status(HttpStatus.NOT_FOUND).body("유저 정보가 존재하지 않습니다.");
         }
         
         long deleteCnt = 0;
         try {
        	 deleteCnt = usersService.deleteUser(existingUser);
		 } catch (EmptyResultDataAccessException e) {
			 return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("삭제하려는 UserId가 없습니다!");
		 } catch (Exception e) {
			 return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("예외가 발생했습니다.!");
		 }
         
         if (deleteCnt > 0) return ResponseEntity.status(HttpStatus.OK).body("내 정보 삭제 성공!!");
         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("내 정보 삭제를 실패했습니다!!");
    }
}
