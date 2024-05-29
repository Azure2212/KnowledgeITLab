package com.example.springboot_github_mvc.dtos;

import com.example.springboot_github_mvc.enumm.FlagUser;
import com.example.springboot_github_mvc.enumm.RoleUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    String id;
    String avatar;
    String username;
    String password;
    String fullName;
    String gmail;
    LocalDateTime birthday;
    RoleUser role;
    LocalDateTime dataCreated;
    LocalDateTime lastUserCreated;
    LocalDateTime lastUpdated;
    FlagUser flagUser;
    List<PaperDto> papers;
}
