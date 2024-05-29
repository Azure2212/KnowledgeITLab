package com.example.springboot_github_mvc.service.impl;

import com.example.springboot_github_mvc.dtos.UserDto;
import com.example.springboot_github_mvc.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

public class SystemServiceImpl {


    public UserDto getUserByid(String id) {
        UserRepository userRepository = new UserRepository();
        List<UserDto> users = userRepository.findAll();
        users = users.stream().filter(u -> u.getId().equals(id)).collect(Collectors.toList());
        if(users.size() == 0) return null;
        else return users.get(0);
    }
}
