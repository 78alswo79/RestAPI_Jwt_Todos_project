package com.aladdin.task.practice.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


import lombok.Data;

@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@Table(name = "users")
@Data
public class UsersEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long seq;
	
	@Column(nullable = false, unique = true)
	String userId;
	
	@Column(nullable = false)
	String password;

	public UsersEntity() {}
	
	public UsersEntity(String userId, String password) {
		this.userId = userId;
		this.password = password;
	}
	
	
}
