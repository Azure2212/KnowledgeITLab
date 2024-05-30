package com.example.springboot_github_mvc.controller;

import com.example.springboot_github_mvc.dtos.UserDto;
import com.example.springboot_github_mvc.enumm.FlagUser;
import com.example.springboot_github_mvc.enumm.RoleUser;
import com.example.springboot_github_mvc.enviroment;
import com.example.springboot_github_mvc.repository.GitHubRepository;
import com.example.springboot_github_mvc.service.IUserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Controller
@RequiredArgsConstructor
public class AuthorPageUIController {

    private static final Logger logger = LoggerFactory.getLogger(AuthorPageUIController.class);
    private final IUserService userService;

    @GetMapping("")
    public String initialAppView() {
        return "redirect:/login";
    }

    @GetMapping("login")
    public String viewLoginPage(HttpSession session, Model model, @RequestParam(required = false) String error) {
        try {
            //GitHubRepository.removeDB();
//            GitHubRepository.removeDB().thenAccept(result -> {
//            });
            SystemPageUIController.removeUser(session);

            boolean check = GitHubRepository.setup();
            if (!check) return "redirect:/system/error404";
            model.addAttribute("error", error);
            return "UI/pages/loginPage";
        } catch (Exception e) {
            logger.error(e.getMessage());
            return "UI/pages/loginPage";
        }
    }

//    @GetMapping("regist")
//    public String viewRegistPage(@RequestParam(required = false) String error, Model model) {
//        try {
//            Path directory = Paths.get(enviroment.folderContainGithub);
//            if (!Files.exists(directory))
//                GitHubRepository.setup();
//            model.addAttribute("error", error);
//            return "UI/pages/article_module/register";
//        } catch (Exception e) {
//            return "";
//        }
//    }

    @GetMapping("userList")
    public String viewUserPage(Model model, HttpSession session) {
        try {
            if (SystemPageUIController.getUserSession(session) == null)
                return "redirect:/login";
            GitHubRepository.pullDB(enviroment.folderContainGithub);
            model.addAttribute("title", "Danh sách các người dùng");
            model.addAttribute("sidebarIndex", "4");
            setupData4Page(model, session);
            return "UI/pages/article_module/userListPage";

        } catch (Exception e) {
            logger.error(e.getMessage());
            return "redirect:/system/error404";
        }
    }

    @PostMapping("/register")
    public String registerUser(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("fullName") String fullName,
            @RequestParam("gmail") String gmail,
            @RequestParam("birthday") String birthday,
            @RequestParam MultipartFile img
    ) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        String formattedDateTime = now.format(formatter);
        LocalDateTime dateTime = LocalDateTime.parse(formattedDateTime, formatter);


        String dateTimeString = birthday + "T00:00:00";
        LocalDateTime birthdayy = LocalDateTime.parse(dateTimeString, formatter);

        UserDto newUser = new UserDto();
        newUser.setUsername(username);
        newUser.setPassword(password);
        newUser.setFullName(fullName);
        newUser.setRole(RoleUser.NONE);
        newUser.setFlagUser(FlagUser.NONE);
        newUser.setBirthday(birthdayy);
        newUser.setGmail(gmail);
        newUser.setPapers(new ArrayList<>());
        newUser.setLastUserCreated(dateTime);
        newUser.setLastUpdated(dateTime);
        newUser.setDataCreated(dateTime);

        try {
            GitHubRepository.pullDB(enviroment.folderContainGithub);
            String fileName = img.getOriginalFilename();
            String fileExtension = getFileExtension(fileName);
            if (fileExtension != null && SystemPageUIController.ALLOWED_EXTENSIONS.contains(fileExtension.toLowerCase())) {
                System.out.println("File is an image.");
            } else {
                return "redirect:/login?error=4";
            }

            fileName = StringUtils.cleanPath(img.getOriginalFilename());
            Path uploadDir = Paths.get(enviroment.UserDirectory);
            Path filePath = uploadDir.resolve(fileName);
            Files.copy(img.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String directoryName = filePath.getParent().toString();
            String oldFile = directoryName + File.separator + fileName;
            String newFile = directoryName + File.separator + username + "_avatar" + "." + fileExtension;
            Resource oldResource = new FileSystemResource(oldFile);
            Resource newResource = new FileSystemResource(newFile);
            if (oldResource.getFile().renameTo(newResource.getFile())) {
                System.out.println("File renamed successfully");
            }

            LocalDate today = LocalDate.now();
            String avatarUrl = username + "_avatar_" + today.toString().replace("-", "_") + "." + fileName.split("\\.")[1];

            Path destinationPath = Paths.get(enviroment.avatarUrl);
            if (!checkIfPageNameExist(avatarUrl, destinationPath)) {
                newUser.setAvatar(enviroment.gitHub_userAvatar_Url + File.separator + avatarUrl);
                UserDto rs = userService.addUser(newUser);
                if (rs == null) return "redirect:/login?error=4";
                Path sourcePath = Paths.get(newFile);
                String destinationFilePath = destinationPath + File.separator + avatarUrl;
                destinationPath = Paths.get(destinationFilePath);
                Files.copy(sourcePath, destinationPath);
                GitHubRepository.push2Git("add paper", newUser.getFullName());
                return "redirect:/login";
            } else {
                return "redirect:/login?error=4";
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            return "redirect:/login?error=3";
        }

    }


    @PostMapping("checkLogin")
    public String checkLogin(@RequestParam String username, @RequestParam String password, Model model, HttpSession session) {
        try {
            boolean check = GitHubRepository.setup();
            if (!check) return "redirect:/system/error404";
            SystemPageUIController.removeUser(session);
            GitHubRepository.setup();
            UserDto user = userService.checkLogin(username, password);
            if (user == null) return "redirect:/login?error=5";

            if (!user.getRole().equals(RoleUser.GLOBAL_ADMIN) && user.getFlagUser().equals(FlagUser.NONE))
                return "redirect:/login?error=6";
            int checkExpire = checkExpireUser(user);
            if (checkExpire != 1) return "redirect:/login?error=" + checkExpire;

            user = userService.updateInformationUserWheneverLogin(user);
            session.setAttribute("sessionTime", SystemPageUIController.getNow());
            session.setAttribute("user", user);
            model.addAttribute("user", user);
            GitHubRepository.push2Git("update user", "azure");
            return "redirect:/articlePage/viewUploadPage";
        } catch (Exception e) {
            logger.error(e.getMessage());
            return "redirect:/system/error404";
        }
    }

    @PostMapping("updateUserInformation")
    public String updateUserInformation(HttpSession session, @RequestParam String gmail, @RequestParam String password, @RequestParam String idUser) {
        try {
            if (SystemPageUIController.getUserSession(session) == null)
                return "redirect:/login";
            GitHubRepository.pullDB(enviroment.folderContainGithub);
            UserDto user = userService.getUserByid(idUser);
            user.setGmail(gmail);
            user.setPassword(password);
            user = userService.updateUser(user);
            GitHubRepository.push2Git("update user", "azure");
            if (user == null) return "redirect:/system/error404";
            return "redirect:/userList";
        } catch (Exception e) {
            logger.error(e.getMessage());
            return "redirect:/system/error404";
        }
    }

    @GetMapping("setRoleUser")
    public String setRoleUser(HttpSession session, @RequestParam String type, @RequestParam String value, @RequestParam String idUser) {
        try {
            if (SystemPageUIController.getUserSession(session) == null)
                return "redirect:/login";
            GitHubRepository.pullDB(enviroment.folderContainGithub);
            UserDto user = userService.getUserByid(idUser);
            if (user == null) return "redirect:/system/error404";
            if (type.equals("FlagUser") && !user.getRole().equals(RoleUser.GLOBAL_ADMIN)) {
                FlagUser oldFlag = user.getFlagUser();
                user.setFlagUser(SystemPageUIController.flagUser.entrySet().stream()
                        .filter(entry -> Objects.equals(value, entry.getValue()))
                        .map(Map.Entry::getKey)
                        .findFirst()
                        .orElse(null));
                if (oldFlag.equals(FlagUser.BAN_FOREVER_FOREVER) && user.getFlagUser().equals(FlagUser.BAN_FOREVER_FOREVER)) {
                    user.setFlagUser(FlagUser.ACCEPT);
                    user.setLastUpdated(SystemPageUIController.getNow());
                    user.setLastUserCreated(SystemPageUIController.getNow());
                }
            }
            if (type.equals("RoleUser") && !user.getRole().equals(RoleUser.GLOBAL_ADMIN)) {
                if (user.getRole().equals(RoleUser.ADMIN)) {
                    user.setRole(RoleUser.BRONZE_USER);
                } else {
                    user.setRole(RoleUser.ADMIN);
                }
            }
            UserDto rs = userService.updateUser(user);
            if (rs == null) return "redirect:/system/error404";
            GitHubRepository.push2Git("update User paper", "unknow");
            return "redirect:/userList";
        } catch (Exception e) {
            logger.error(e.getMessage());
            return "redirect:/system/error404";
        }
    }

    public void setupData4Page(Model model, HttpSession session) {
        UserDto myUser = SystemPageUIController.getUserSession(session);
        List<String> columnName = List.of("Avatar", "Họ và Tên", "cấp bật", "Ngày sinh", "Trạng thái", "Số bài đã đăng", "lần cuối cập nhật");
        List<List<String>> data = new ArrayList<>();
        List<String> idUsers = new ArrayList<>();
        List<UserDto> users = userService.getAllUsers();
        // Print the sorted list
        Collections.sort(users, Comparator.comparingInt((UserDto user) -> user.getPapers().size()).reversed());
        for (int i = 0; i < users.size(); i++) {
            UserDto user = users.get(i);
            String avatar = user.getAvatar().replace("https://github.com", "https://raw.githubusercontent.com").replace("/blob/", File.separator);
            data.add(List.of(avatar, user.getFullName(), SystemPageUIController.roleUser.get(user.getRole()), user.getBirthday().toString(), SystemPageUIController.flagUser.get(user.getFlagUser()),
                    String.valueOf(user.getPapers().size()), user.getLastUpdated().toString()));
            //shortUrl.add("https://" + paper.getId());
            idUsers.add(user.getId());
        }
        model.addAttribute("columnName", columnName);
        model.addAttribute("idUsers", idUsers);
        model.addAttribute("data", data);
        if (myUser.getRole().equals(RoleUser.GLOBAL_ADMIN))
            model.addAttribute("editRight", true);
        else
            model.addAttribute("editRight", false);
    }

    // Helper method to extract file extension
    private String getFileExtension(String fileName) {
        if (fileName != null && fileName.lastIndexOf(".") != -1) {
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        }
        return null;
    }

    private boolean checkIfPageNameExist(String nameFile, Path destinationPath) {
        try {
            String filePath = destinationPath.toFile().getPath().toString() + File.separator + nameFile;
            File file = new File(filePath);
            if (file.exists()) {
                System.out.println("File path exists.");
                return true;
            } else {
                System.out.println("File path does not exist.");
                return false;
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            return false;
        }
    }

    private int checkExpireUser(UserDto user) {
        try {
            LocalDateTime now = SystemPageUIController.getNow();
            long daysDifference = ChronoUnit.DAYS.between(now, user.getLastUserCreated());
            if (Math.abs(daysDifference) >= 100) {
                user.setFlagUser(FlagUser.BAN_FOREVER_FOREVER);
                user = userService.updateUser(user);
                return 0; // > 100 day without up paper
            }
            if (user.getFlagUser().equals(FlagUser.BAN_FOREVER_FOREVER)) return 2; // banned forever!
            return 1;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return 3;
        }
    }


}
