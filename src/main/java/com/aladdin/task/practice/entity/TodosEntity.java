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
@Table(name = "todos")
@Data
public class TodosEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long seq;
	
	@Column(nullable = false)
	String content;
	

	public TodosEntity() {}
	
	public TodosEntity(String content) {
		this.content = content;
	}
}
