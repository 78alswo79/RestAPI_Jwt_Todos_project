package com.aladdin.task.practice.service;


import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aladdin.task.practice.entity.UsersEntity;
import com.aladdin.task.practice.repository.UsersRepository;
import com.aladdin.task.practice.vo.LoginRequest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UsersService {
	
	private final UsersRepository usersRepository;
	public UsersService(UsersRepository usersRepository) {
		this.usersRepository = usersRepository;
	}

	/**
	 * @param UsersEntity
	 * @author 이민재
	 * */
	public void insertUsers(UsersEntity users) {
		// TODO Auto-generated method stub
		usersRepository.save(users);
		
	}
	
	/**
	 * <p> userId 중복검사</p>
	 * */
	public boolean checkDuplicateUserId(String userId) {
		return usersRepository.findByUserId(userId).isPresent();
	}
	
	
	public boolean isNullCheck(UsersEntity user) {
		if (user.getUserId() == null || user.getUserId().isBlank()
				|| user.getPassword() == null || user.getPassword().isBlank()) {
			return true;
		} else {			
			return false;
		}
	}
	
	public boolean isNullCheck(LoginRequest user) {
		
		if (user.getUserId() == null || user.getUserId().isBlank()
				|| user.getPassword() == null || user.getPassword().isBlank()) {
			return true;
		} else {			
			return false;
		}
	}
	
	public UsersEntity updateUsers(UsersEntity existingUser) {
		return usersRepository.save(existingUser);
	}
		
	@Transactional
	public long deleteUser(UsersEntity existingUser) {
		return usersRepository.deleteByUserId(existingUser.getUserId());
	}

	
}
