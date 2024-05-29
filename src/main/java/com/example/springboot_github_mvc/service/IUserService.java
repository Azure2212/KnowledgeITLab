package com.example.springboot_github_mvc.service;

import com.example.springboot_github_mvc.dtos.UserDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface IUserService {
    public List<UserDto> getAllUsers();

    public UserDto getUserByid(String id);

    public UserDto addUser(UserDto userDto);

    public UserDto deleteUser(UserDto userDto);

    public UserDto updateUser(UserDto userDto);

    public UserDto checkLogin(String username, String password);

    public UserDto updateInformationUserWheneverLogin(UserDto userDto);

}
