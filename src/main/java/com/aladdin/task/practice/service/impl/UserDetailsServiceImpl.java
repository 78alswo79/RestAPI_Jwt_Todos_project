package com.aladdin.task.practice.service.impl;

import com.aladdin.task.practice.entity.UsersEntity;
import com.aladdin.task.practice.repository.UsersRepository;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service // Spring 빈으로 등록
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsersRepository usersRepository;

    public UserDetailsServiceImpl(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    // Spring Security가 사용자 이름을 기반으로 UserDetails를 로드할 때 호출
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UsersEntity user = usersRepository.findByUserId(username) // username 필드로 조회
                          .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return new UserDetailsImpl(user); // 조회된 UsersEntity로 UserDetailsImpl 객체 생성
    }
}
