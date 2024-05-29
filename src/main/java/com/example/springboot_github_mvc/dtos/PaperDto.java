package com.example.springboot_github_mvc.dtos;

import com.example.springboot_github_mvc.enumm.CategoryPaper;
import com.example.springboot_github_mvc.enumm.PaperStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PaperDto {
    String id;
    String namePaper;
    PaperStatus status;
    UserDto author;
    CategoryPaper categoryPaper;
    LocalDateTime dateCreated;
    String url;
}
