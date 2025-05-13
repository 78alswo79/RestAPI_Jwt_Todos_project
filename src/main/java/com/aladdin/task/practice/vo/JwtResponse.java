package com.aladdin.task.practice.vo;

import lombok.Data;
@Data
public class JwtResponse {
    private String access_token;
    private String type = "Bearer";
    
    public JwtResponse(String accessToken) {
        this.access_token = accessToken;
    }
}