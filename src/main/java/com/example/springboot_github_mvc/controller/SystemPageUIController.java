package com.example.springboot_github_mvc.controller;

import com.example.springboot_github_mvc.dtos.UserDto;
import com.example.springboot_github_mvc.enumm.FlagUser;
import com.example.springboot_github_mvc.enumm.RoleUser;
import com.example.springboot_github_mvc.service.impl.SystemServiceImpl;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping(path = "/system")
public class SystemPageUIController {

    public static final Map<FlagUser, String> flagUser = new HashMap<>();
    public static final Map<RoleUser, String> roleUser = new HashMap<>();
    public static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("png", "jpeg", "jpg");

    static {
        flagUser.put(FlagUser.REFUSE, "Đã từ chối");
        flagUser.put(FlagUser.NONE, "Đã đăng ký");
        flagUser.put(FlagUser.ACCEPT, "Đã chấp nhập");
        flagUser.put(FlagUser.BAN_7, "Cấm 7 ngày");
        flagUser.put(FlagUser.BAN_15, "Cấm 15 ngày");
        flagUser.put(FlagUser.BAN_30, "Cấm 30 ngày");
        flagUser.put(FlagUser.BAN_FOREVER_FOREVER, "Cấm vĩnh viễn");

        roleUser.put(RoleUser.GLOBAL_ADMIN, "Ông chủ");
        roleUser.put(RoleUser.ADMIN, "ADMIN");
        roleUser.put(RoleUser.DIAMOND_USER, "DIAMOND");
        roleUser.put(RoleUser.GOLD_USER, "GOLD");
        roleUser.put(RoleUser.SILVER_USER, "SILVER");
        roleUser.put(RoleUser.BRONZE_USER, "BRONZE");
        roleUser.put(RoleUser.NONE, "TBA");
    }

    @GetMapping("error404")
    public String viewErrorPage(HttpSession session) {
        try {
            if (SystemPageUIController.getUserSession(session) == null)
                return "redirect:/login";
            return "UI/pages/article_module/page404/page404";
        } catch (Exception e) {
            return "UI/pages/article_module/page404/page404";
        }
    }

    @PostMapping("recoverSystem")
    public String recoverSystem(HttpSession session) {
        try {
            session.removeAttribute("user");
            return "redirect:/login";
        } catch (Exception e) {
            return "redirect:/system/error404";
        }
    }

    public static UserDto getUserSession(HttpSession session) {
        if (session.getAttribute("user") != null) {
            SystemServiceImpl systemService = new SystemServiceImpl();
            UserDto user = (UserDto) session.getAttribute("user") ;
            if (user == null) return null;

            user = systemService.getUserByid(user.getId());
            if(!user.getRole().equals(RoleUser.GLOBAL_ADMIN) || user.getFlagUser().equals(FlagUser.BAN_FOREVER_FOREVER)){
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
        if (session.getAttribute("user") != null) {
            session.removeAttribute("user");
        }
    }

//    @GetMapping("/test")
//    public String totest() {
//        return "UI/pages/article_module/informationUser";
//    }
//
//    @GetMapping("takeHeaderCss")
//    public String takeHeaderCss() {
//        System.out.println("UI/util/header/header.empty");
//        return "static/css/main.css";
//    }

//    @GetMapping("/footer")
//    public String totestfooter() {
//        return "UI/pages/util/footer/footer";
//    }

    public static LocalDateTime getNow() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        String formattedDateTime = now.format(formatter);
        LocalDateTime dateTime = LocalDateTime.parse(formattedDateTime, formatter);
        return dateTime;
    }
}
