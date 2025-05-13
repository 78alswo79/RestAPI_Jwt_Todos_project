package com.aladdin.task.practice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.aladdin.task.practice.entity.UsersEntity;

@Repository
public interface UsersRepository extends JpaRepository<UsersEntity, Long>{
	Optional<UsersEntity> findByUserId(String userId);
	
	@Transactional
	long deleteByUserId(String userId);
}
