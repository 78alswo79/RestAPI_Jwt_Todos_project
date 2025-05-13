package com.aladdin.task.practice.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;



import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aladdin.task.practice.entity.TodosEntity;

import com.aladdin.task.practice.repository.TodosRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TodosService {

	private final TodosRepository todosRepository;
	
	public TodosService(TodosRepository todosRepository) {
		this.todosRepository = todosRepository;
	}
	
	public void insertTodos(TodosEntity todos) {
		// TODO Auto-generated method stub
		todosRepository.save(todos);
	}
	
	public boolean isNullCheck(TodosEntity todos) {
		if (todos.getContent() == null || todos.getContent().isBlank()) {
			return true;
		} else {			
			return false;
		}
	}

	public List<TodosEntity> getTodoList() {
		// TODO Auto-generated method stub
		List<TodosEntity> resList = new ArrayList<TodosEntity>();
		resList = todosRepository.findAll();
		return resList;
	}

	public TodosEntity getTodosBySeq(Long seq) {
		// TODO Auto-generated method stub
		Optional<TodosEntity> todoOptional = todosRepository.findBySeq(seq);
		return todoOptional.orElse(null);
		
	}

	public TodosEntity updateTodo(TodosEntity existingTodo) {
		// TODO Auto-generated method stub
		return todosRepository.save(existingTodo);
		
	}

	@Transactional
	public long DeleteTodo(TodosEntity existingTodo) {
		// TODO Auto-generated method stub
		return todosRepository.deleteBySeq(existingTodo.getSeq());
	}

	public TodosEntity getSearchTodo(String content) {
		// TODO Auto-generated method stub
		Optional<TodosEntity> todoOptional = todosRepository.findByContent(content);
		return todoOptional.orElse(null);
	}
	
	
}
