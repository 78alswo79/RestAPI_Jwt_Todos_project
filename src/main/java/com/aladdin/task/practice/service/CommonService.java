package com.aladdin.task.practice.service;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.aladdin.task.practice.entity.UsersEntity;
import com.aladdin.task.practice.repository.UsersRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CommonService {
	
	private final UsersRepository usersRepository;
	
	public CommonService (UsersRepository usersRepository) {
		this.usersRepository = usersRepository;
	}
	
	public UsersEntity getUser(String userId) {		
		Optional<UsersEntity> userOptional =  usersRepository.findByUserId(userId);
		return userOptional.orElse(null);
	}
}
