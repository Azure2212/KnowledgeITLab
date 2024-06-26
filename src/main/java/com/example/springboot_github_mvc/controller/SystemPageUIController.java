package com.example.springboot_github_mvc.controller;

import com.example.springboot_github_mvc.dtos.PaperDto;
import com.example.springboot_github_mvc.dtos.UserDto;
import com.example.springboot_github_mvc.enumm.FlagUser;
import com.example.springboot_github_mvc.enumm.PaperStatus;
import com.example.springboot_github_mvc.enumm.RoleUser;
import com.example.springboot_github_mvc.environment;
import com.example.springboot_github_mvc.repository.GitHubRepository;
import com.example.springboot_github_mvc.repository.PaperRepository;
import com.example.springboot_github_mvc.service.IPaperService;
import com.example.springboot_github_mvc.service.IUserService;
import com.example.springboot_github_mvc.service.impl.SystemServiceImpl;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Stream;

@Controller
@RequiredArgsConstructor
@RequestMapping(path = "/system")
public class SystemPageUIController {
    private static final Logger logger = LoggerFactory.getLogger(SystemPageUIController.class);
    public static final Map<FlagUser, String> flagUser = new HashMap<>();
    public static final Map<RoleUser, String> roleUser = new HashMap<>();
    public static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("png", "jpeg", "jpg");

    static {
        flagUser.put(FlagUser.REFUSE, "Đã từ chối");
        flagUser.put(FlagUser.NONE, "Đã đăng ký");
        flagUser.put(FlagUser.ACCEPT, "Đã chấp nhập");
        flagUser.put(FlagUser.BAN_FOREVER_FOREVER, "Cấm vĩnh viễn");

        roleUser.put(RoleUser.GLOBAL_ADMIN, "Ông chủ");
        roleUser.put(RoleUser.ADMIN, "ADMIN");
        roleUser.put(RoleUser.DIAMOND_USER, "DIAMOND");
        roleUser.put(RoleUser.GOLD_USER, "GOLD");
        roleUser.put(RoleUser.SILVER_USER, "SILVER");
        roleUser.put(RoleUser.BRONZE_USER, "BRONZE");
        roleUser.put(RoleUser.NONE, "TBA");
    }

    private final IPaperService paperService;
    private final IUserService userService;
    private final PaperRepository paperRepository;

    @GetMapping("error404")
    public String viewErrorPage(HttpSession session) {
        try {
            if (SystemPageUIController.getUserSession(session) == null)
                return "redirect:/login";
            return "UI/pages/article_module/page404";
        } catch (Exception e) {
            logger.error(e.getMessage());
            return "UI/pages/article_module/page404";
        }
    }

    @GetMapping("turnOnSecretRestore")
    public String viewSecretRestore(HttpSession session) {

        return "redirect:/login?error=7";
    }

    @PostMapping("restoreDatabase")
    public String restoreDatabase(HttpSession session, @RequestParam String secretPass) {
        try {
            if (!secretPass.equals("Tuandeptrai2212")) return "redirect:/login?error=8";
            GitHubRepository.removeDB().thenAccept(result -> {
                System.out.println("the Secret tool to restore database is actived !");
            });
            SystemPageUIController.removeUser(session);
            //boolean check = GitHubRepository.setup();
            return "redirect:/login?error=9";
        } catch (Exception e) {
            logger.error(e.getMessage());
            return "redirect:/login?error=8";
        }
    }

    @PostMapping("recoverSystem")
    public String recoverSystem(HttpSession session) {
        try {
            session.removeAttribute("user");
            return "redirect:/login";
        } catch (Exception e) {
            logger.error(e.getMessage());
            return "redirect:/system/error404";
        }
    }

    public static UserDto getUserSession(HttpSession session) {
        if (session.getAttribute("sessionTime") == null) return null;
        if (session.getAttribute("user") != null) {
            SystemServiceImpl systemService = new SystemServiceImpl();
            UserDto user = (UserDto) session.getAttribute("user");
            if (user == null) return null;

            user = systemService.getUserByid(user.getId());
            if (!user.getRole().equals(RoleUser.GLOBAL_ADMIN) && user.getFlagUser().equals(FlagUser.BAN_FOREVER_FOREVER)) {
                session.removeAttribute("user");
                return null;
            }
            session.setAttribute("user", user);
            LocalDateTime previousClick = (LocalDateTime) session.getAttribute("sessionTime");
            LocalDateTime now = getNow();
            long minutes = Math.abs(ChronoUnit.MINUTES.between(previousClick, now));
            if (minutes > 60) {
                session.removeAttribute("user");
                return null;
            }
            session.setAttribute("sessionTime", now);
            return (UserDto) session.getAttribute("user");
        }
        return null;
    }

    public static void removeUser(HttpSession session) {
//        if (session.getAttribute("user") != null) {
//            session.removeAttribute("user");
//        }
        Enumeration<String> attributeNames = session.getAttributeNames();

        // Remove each attribute from the session
        while (attributeNames.hasMoreElements()) {
            String attributeName = attributeNames.nextElement();
            session.removeAttribute(attributeName);
        }

        System.out.println("Session cleared!");
    }


    @GetMapping("removeAllRubbishData")
    public String removeAllRubbishData() {
        try {
            GitHubRepository.pullDB(environment.folderContainGithub);
            List<PaperDto> papers = paperService.getAllPaperByStatus(PaperStatus.DELETED);
            papers.addAll(paperService.getAllPaperByStatus(PaperStatus.REFUSE));

            List<String> fileNames = new ArrayList<>();
            String folderPath = "src/main/java/com/example/springboot_github_mvc/DB/Github_DB/allAcceptedPaper";

            try (Stream<Path> paths = Files.walk(Paths.get(folderPath))) {
                paths.filter(Files::isRegularFile)
                        .map(Path::getFileName)
                        .map(Path::toString)
                        .forEach(fileNames::add);
            } catch (IOException e) {
                e.printStackTrace();
            }
            List<String> url2removed = new ArrayList<>();
            for (PaperDto p : papers) {
                for (String f : fileNames) {
                    String url = p.getUrl().replace("https://github.com/azurebob2212/knowledgeTreeDocumentDataLab/blob/main/allAcceptedPaper",
                            environment.allPapers);
                    if (url.equals(environment.allPapers + "/" + f)) {
                        url2removed.add((url + File.separator + f));
                    }
                }
            }
            for(String i: url2removed){
                File file = new File(i);

                // Check if the file exists
                if (file.exists()) {
                    // Attempt to delete the file
                    boolean deleted = file.delete();

                    // Check if the file was successfully deleted
                    if (deleted) {
                        System.out.println("File deleted successfully.");
                    } else {
                        System.out.println("Failed to delete the file.");
                    }
                } else {
                    System.out.println("File does not exist.");
                }
            }

            for(PaperDto i: papers){
                PaperDto rs = paperService.deletePaper(i);
            }
            List<UserDto> userAfterUpdated = userService.getAllUsers();
            for(UserDto user: userAfterUpdated)
                userService.updateUser(user);
            GitHubRepository.push2Git("reset","azure");
            return "redirect:/articlePage/viewUploadPage";
        } catch (Exception e) {
            logger.error(e.getMessage());
            return "redirect:/login?error=8";
        }
    }

    public static LocalDateTime getNow() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        String formattedDateTime = now.format(formatter);
        LocalDateTime dateTime = LocalDateTime.parse(formattedDateTime, formatter);
        return dateTime;
    }
}
