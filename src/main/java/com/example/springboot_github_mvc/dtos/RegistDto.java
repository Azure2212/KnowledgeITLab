package com.example.springboot_github_mvc.dtos;

import lombok.Data;

@Data
public class RegistDto {
    private String username;
    private String password;
    private String fullName;
    private String gmail;
    private String birthday;
}
