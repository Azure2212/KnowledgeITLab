package com.example.springboot_github_mvc.service.impl;

import com.example.springboot_github_mvc.dtos.PaperDto;
import com.example.springboot_github_mvc.dtos.UserDto;
import com.example.springboot_github_mvc.enumm.PaperStatus;
import com.example.springboot_github_mvc.enumm.RoleUser;
import com.example.springboot_github_mvc.repository.PaperRepository;
import com.example.springboot_github_mvc.repository.UserRepository;
import com.example.springboot_github_mvc.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {
    private final UserRepository userRepository;
    private final PaperRepository paperRepository;
    @Override
    public List<UserDto> getAllUsers() {
        try {
            List<UserDto> listUser = userRepository.findAll();
            List<PaperDto> listPaper = paperRepository.findAll();
            for(UserDto user: listUser){
                List<PaperDto> papers = listPaper.stream()
                        .filter(paper -> paper.getAuthor().getId().equals(user.getId()))
                        .collect(Collectors.toList());
                user.setPapers(papers);
            }
            return listUser;
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    @Override
    public UserDto getUserByid(String id) {
        List<UserDto> users = getAllUsers();
        users = users.stream().filter(u -> u.getId().equals(id)).collect(Collectors.toList());
        if(users.size() == 0) return null;
        else return users.get(0);
    }

    @Override
    public UserDto addUser(UserDto userDto) {
        try {
            userDto.setId(UUID.randomUUID().toString());
            return userRepository.insert(userDto);
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }
    @Override
    public UserDto deleteUser(UserDto userDto) {
        try {
            return userRepository.delete(userDto);
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    @Override
    public UserDto updateUser(UserDto userDto) {
        try {
            return userRepository.update(userDto);
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    @Override
    public UserDto checkLogin(String username, String password) {
        List<UserDto> listUser = userRepository.findAll();
        List<UserDto> userFound = listUser.stream().filter(x -> x.getUsername().equals(username)).collect(Collectors.toList());
        if (userFound.size() == 0) return null;
        if(userFound.get(0).getPassword().equals(password))return userFound.get(0);
        return null;
    }

    @Override
    public UserDto updateInformationUserWheneverLogin(UserDto userDto) {
        UserDto user = getUserByid(userDto.getId());
        if(user == null) return null;
        if(user.getRole().equals(RoleUser.ADMIN) || user.getRole().equals(RoleUser.GLOBAL_ADMIN)) return user;
        user.setPapers(user.getPapers().stream().filter(p -> p.getStatus().equals(PaperStatus.ACCEPT)).collect(Collectors.toList()));
        if(user.getPapers().size() <= 3)
            user.setRole(RoleUser.BRONZE_USER);
        else if(user.getPapers().size() > 5 && user.getPapers().size() <= 10)
            user.setRole(RoleUser.SILVER_USER);
        else if (user.getPapers().size() >10 && user.getPapers().size() <= 20)
            user.setRole(RoleUser.GOLD_USER);
        else if (user.getPapers().size() >20)
            user.setRole(RoleUser.DIAMOND_USER);
        user = updateUser(user);
        return user;
    }


}
