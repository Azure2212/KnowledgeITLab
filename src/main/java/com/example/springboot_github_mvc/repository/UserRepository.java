package com.example.springboot_github_mvc.repository;

import com.example.springboot_github_mvc.dtos.UserDto;
import com.example.springboot_github_mvc.environment;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Iterator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserRepository {
    private String userTableUrl = environment.userTableUrl;

    public List<UserDto> findAll() {
        try {
            // Convert the list to JSON string
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());

            System.out.println(userTableUrl);
            // Write JSON string to file
            List<UserDto> userDtos = objectMapper.readValue(new File(userTableUrl),
                    new TypeReference<List<UserDto>>() {
                    });

            System.out.println("JSON data has been written to users.json file.");
            return userDtos;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public UserDto insert(UserDto userDto) {
        try {
//            GitHubRepository.pullDB(enviroment.folderContentGithub);
            List<UserDto> users = findAll();

            int index = users.indexOf(users.stream()
                    .filter(user -> user.getUsername() != null)
                    .filter(user -> user.getUsername().equals(userDto.getUsername()))
                    .findFirst()
                    .orElse(null));

            if(index != -1) {
                System.out.println("exist element with email found.");
                return null;
            }
                users.add(userDto);
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule());

                String jsonString = objectMapper.writeValueAsString(users);

                // Write JSON string to file
                objectMapper.writeValue(new File(userTableUrl), users);
              
                return userDto;

        } catch (Exception e) {
            System.out.println("error :"+e);
            return null;
        }
    }

    public UserDto delete(UserDto userDto) {
        try {
//            GitHubRepository.pullDB(enviroment.folderContentGithub);
            List<UserDto> users = findAll();
            if(users.size() == 0)return new UserDto();

            Iterator<UserDto> iterator = users.iterator();
            while (iterator.hasNext()) {
                UserDto user = iterator.next();
                if (user.getId().equals(userDto.getId())) {
                    iterator.remove();
                    System.out.println("User with id: " +user.getId() + " is removed successfully.");
                }
            }

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            String jsonString = objectMapper.writeValueAsString(users);

            // Write JSON string to file
            objectMapper.writeValue(new File(userTableUrl), users);
          
            return userDto;
        } catch (Exception e) {
            System.out.println("error :"+e);
            return null;
        }
    }

    public UserDto update(UserDto userDto) {
        try {
//            GitHubRepository.pullDB(enviroment.folderContentGithub);
            List<UserDto> users = findAll();
            if(users.size() == 0)return new UserDto();

            int index = users.indexOf(users.stream()
                    .filter(user -> user.getId().equals(userDto.getId()))
                    .findFirst()
                    .orElse(null));

            if (index == -1) {
                return null;
            }

            users.set(index,userDto);
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());

            String jsonString = objectMapper.writeValueAsString(users);

            // Write JSON string to file
            objectMapper.writeValue(new File(userTableUrl), users);
          
            return userDto;
        } catch (Exception e) {
            System.out.println("error :"+e);
            return null;
        }
    }

}
