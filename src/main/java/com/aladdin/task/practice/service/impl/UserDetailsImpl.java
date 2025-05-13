package com.aladdin.task.practice.service.impl;

import com.aladdin.task.practice.entity.UsersEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.Collections;

public class UserDetailsImpl implements UserDetails {

    /**
	 * 
	 */
	private static final long serialVersionUID = 6677275914761356325L;
	
	private final UsersEntity user; // final 키워드 추가

    public UserDetailsImpl(UsersEntity user) {
        this.user = user;
    }

    // 권한은 필요시 구현
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    // UsersEntity의 password 필드를 반환
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    // UsersEntity의 username 필드를 반환 (UserDetails의 username 역할)
    @Override
    public String getUsername() {
        return user.getUserId();
    }

    // 계정 관련 상태는 필요에 따라 구현
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }

    // UsersEntity 객체 접근 getter
    public UsersEntity getUser() {
        return user;
    }

}
