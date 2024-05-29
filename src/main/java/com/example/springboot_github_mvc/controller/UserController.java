package com.example.springboot_github_mvc.controller;

import com.example.springboot_github_mvc.dtos.ResponseDto;
import com.example.springboot_github_mvc.dtos.UserDto;
import com.example.springboot_github_mvc.repository.GitHubRepository;
import com.example.springboot_github_mvc.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/User")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final IUserService userService;

    @PostMapping("/addUser")
    private ResponseEntity<?> addUser(@RequestBody UserDto user) {
        try {
            UserDto rs = userService.addUser(user);
            GitHubRepository.push2Git("insert","insert user vo DBGithub");
            return ResponseEntity.ok(new ResponseDto(List.of("get success"), HttpStatus.OK.value(), rs));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.ok(new ResponseDto(List.of(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR.value(), null));
        }
    }

    @PostMapping("/deleteUser")
    private ResponseEntity<?> deleteUser(@RequestBody UserDto user) {
        try {
            UserDto rs = userService.deleteUser(user);

            return ResponseEntity.ok(new ResponseDto(List.of("get success"), HttpStatus.OK.value(), rs));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.ok(new ResponseDto(List.of(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR.value(), null));
        }
    }
    @PostMapping("/updateUser")
    private ResponseEntity<?> updateUser(@RequestBody UserDto user) {
        try {
            UserDto rs = userService.updateUser(user);
            GitHubRepository.push2Git("update","update user vo DBGithub");
            return ResponseEntity.ok(new ResponseDto(List.of("get success"), HttpStatus.OK.value(), rs));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.ok(new ResponseDto(List.of(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR.value(), null));
        }
    }

    @GetMapping("/getAllUser")
    private ResponseEntity<?> getAllUser() {
        try {
            List<UserDto> userDtos =  userService.getAllUsers();
            return ResponseEntity.ok(new ResponseDto(List.of("get success"), HttpStatus.OK.value(), userDtos));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.ok(new ResponseDto(List.of(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR.value(), null));
        }
    }
}
