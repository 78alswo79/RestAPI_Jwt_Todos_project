package com.aladdin.task.practice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.aladdin.task.practice.entity.TodosEntity;

public interface TodosRepository extends JpaRepository<TodosEntity, Long>{
	Optional<TodosEntity> findBySeq(Long seq);
	
	Optional<TodosEntity> findByContent(String content);
	
	@Transactional
	long deleteBySeq(Long seq);
	
	@Transactional
	long deleteByContent(String content);
}
