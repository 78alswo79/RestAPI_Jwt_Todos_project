package com.aladdin.task.practice.controller;

import java.util.List;

import javax.persistence.PersistenceException;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aladdin.task.practice.entity.TodosEntity;
import com.aladdin.task.practice.entity.UsersEntity;
import com.aladdin.task.practice.service.CommonService;
import com.aladdin.task.practice.service.TodosService;
import com.aladdin.task.practice.utils.jwt.JwtTokenProvider;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/todos")
public class RestTodosController {
//	 ○ POST /todos
//	 ○ GET /todos
//	 ○ GET /todos/{id}
//	 ○ PUT /todos/{id}
//	 ○ DELETE /todos/{id}
//	 ○ GET /todos/search
	
	private final CommonService commonService;
	private final TodosService todosService; 
	private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager; // AuthenticationManager 주입
    private final JwtTokenProvider jwtTokenProvider;
	
	public RestTodosController(CommonService commonService, PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtTokenProvider jwtTokenProvider, TodosService todosService) {
		this.commonService = commonService;
		this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.todosService = todosService;
	}
	
	@PostMapping
	public ResponseEntity<String> postTodos(@RequestBody TodosEntity todos) {
		
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName(); // 인증된 사용자의 username (UsersEntity.username)
        
        UsersEntity existingUser = commonService.getUser(currentUsername);
        
        if (existingUser == null) {
        	return ResponseEntity.status(HttpStatus.NOT_FOUND).body("유저 정보가 존재하지 않습니다.");
        }
        
        if (todosService.isNullCheck(todos)) {
        	return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("필수 체크값이 빠져있습니다. 확인 바랍니다.");
        }
        
        todosService.insertTodos(todos);
		
		return ResponseEntity.status(HttpStatus.CREATED).body("todos 리스트 생성 완료!");
	}
	
	@GetMapping
	public ResponseEntity<String> getTodos() {
		
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName(); // 인증된 사용자의 username (UsersEntity.username)
        
        UsersEntity existingUser = commonService.getUser(currentUsername);
        
        if (existingUser == null) {
        	return ResponseEntity.status(HttpStatus.NOT_FOUND).body("유저 정보가 존재하지 않습니다.");
        }
        
        List<TodosEntity> todoList = todosService.getTodoList();
        
        if (todoList == null || todoList.size() == 0) {
        	return ResponseEntity.status(HttpStatus.NOT_FOUND).body("조회되는 todo리스트가 없습니다.");
        }
        
        log.info("todoList : {} ", todoList);
        
		return ResponseEntity.status(HttpStatus.OK).body("todos 리스트 조회 성공! " + todoList);
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<String> getTodosBySeq(@PathVariable("id") Long seq) {
		
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName(); // 인증된 사용자의 username (UsersEntity.username)
        
        UsersEntity existingUser = commonService.getUser(currentUsername);
        
        if (existingUser == null) {
        	return ResponseEntity.status(HttpStatus.NOT_FOUND).body("유저 정보가 존재하지 않습니다.");
        }
        
        TodosEntity existingTodo = todosService.getTodosBySeq(seq);
        if (existingTodo == null) {
        	return ResponseEntity.status(HttpStatus.NOT_FOUND).body("조회되는 todo가 없습니다.");
        }
        
        log.info("existingTodo : {} ", existingTodo);
     
		return ResponseEntity.status(HttpStatus.OK).body("todo 조회 성공! " + existingTodo);
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<String> putTodosBySeq(@PathVariable("id") Long seq, @RequestBody TodosEntity updateTodos) {
		
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName(); // 인증된 사용자의 username (UsersEntity.username)
        
        UsersEntity existingUser = commonService.getUser(currentUsername);
        
        if (existingUser == null) {
        	return ResponseEntity.status(HttpStatus.NOT_FOUND).body("유저 정보가 존재하지 않습니다.");
        }
        
        TodosEntity existingTodo = todosService.getTodosBySeq(seq);
        if (existingTodo == null) {
        	return ResponseEntity.status(HttpStatus.NOT_FOUND).body("조회되는 todo가 없습니다.");
        }
        
        existingTodo.setContent(updateTodos.getContent());
        TodosEntity updatedTodo;
        try {
        	updatedTodo = todosService.updateTodo(existingTodo);
		} catch (DataAccessException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("DB에 접근할 수 없습니다!");
		} catch (PersistenceException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Jpa/Hibernate에 예외가 발생했습니다.");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("예외가 발생했습니다.!");
		}
        
        log.info("existingTodo : {} ", existingTodo);
		return ResponseEntity.status(HttpStatus.OK).body("todo 업데이트 성공!! " + updatedTodo);
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<String> deleteTodosBySeq(@PathVariable("id") Long seq) {
		
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName(); // 인증된 사용자의 username (UsersEntity.username)
        
        UsersEntity existingUser = commonService.getUser(currentUsername);
        
        if (existingUser == null) {
        	return ResponseEntity.status(HttpStatus.NOT_FOUND).body("유저 정보가 존재하지 않습니다.");
        }
        
        TodosEntity existingTodo = todosService.getTodosBySeq(seq);
        if (existingTodo == null) {
        	return ResponseEntity.status(HttpStatus.NOT_FOUND).body("조회되는 todo가 없습니다.");
        }
        
        long deleteCnt = 0;
        try {
        	deleteCnt = todosService.DeleteTodo(existingTodo);
        	log.info(">>>>>>>>>>>>>>삭제된 deleteCnt는??? {} ", deleteCnt);
		} catch (EmptyResultDataAccessException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("삭제하려는 seq가 없습니다!");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("예외가 발생했습니다.!");
		}
        if (deleteCnt > 0) return ResponseEntity.status(HttpStatus.OK).body("todo 삭제 성공!!");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("todo 삭제를 실패했습니다!!");
	}
	
	@GetMapping("/search")
	public ResponseEntity<String> getTodosSearch(@RequestParam(required = true) String content) {
		
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName(); // 인증된 사용자의 username (UsersEntity.username)
        
        UsersEntity existingUser = commonService.getUser(currentUsername);
        
        if (existingUser == null) {
        	return ResponseEntity.status(HttpStatus.NOT_FOUND).body("유저 정보가 존재하지 않습니다.");
        }
        if (content == null || content.isBlank()) {
        	return ResponseEntity.status(HttpStatus.NOT_FOUND).body("필수 체크값이 빠져있습니다. 확인 바랍니다.");
        }
        
        TodosEntity searchTodo = todosService.getSearchTodo(content);
        
        if (searchTodo == null) {
        	return ResponseEntity.status(HttpStatus.NOT_FOUND).body("조회되는 todo가 없습니다.");
        }
        
        log.info("searchTodo : {} ", searchTodo);
		return ResponseEntity.status(HttpStatus.OK).body("todos검색 성공! " + searchTodo);
	}
}
