package com.example.springboot_github_mvc.controller;


import com.example.springboot_github_mvc.dtos.PaperDto;
import com.example.springboot_github_mvc.dtos.UserDto;
import com.example.springboot_github_mvc.enumm.CategoryPaper;
import com.example.springboot_github_mvc.enumm.PaperStatus;
import com.example.springboot_github_mvc.enumm.RoleUser;
import com.example.springboot_github_mvc.environment;
import com.example.springboot_github_mvc.repository.GitHubRepository;
import com.example.springboot_github_mvc.service.IPaperService;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping(path = "/articlePage")
public class ArticlePageUIController {
    private static final Logger logger = LoggerFactory.getLogger(ArticlePageUIController.class);
    private final IPaperService paperService;
    private final IUserService userService;

    private static final Map<CategoryPaper, String> CategoryPapers = new HashMap<>();

    private static final Map<PaperStatus, String> StatusPapers = new HashMap<>();
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("pdf");

    static {
        CategoryPapers.put(CategoryPaper.SOFT_WARE_DEV, "Lập trình ứng dụng");
        CategoryPapers.put(CategoryPaper.SOFT_WARE_OPS, "Triển khai ứng dụng");
        CategoryPapers.put(CategoryPaper.DBMS, "Quản trị cơ sở dữ liệu");
        CategoryPapers.put(CategoryPaper.SCIENCE_COMPUTER, "Khoa học máy tính");
        CategoryPapers.put(CategoryPaper.ACTIVE_MODEL, "Các mô hình phần mềm");
        CategoryPapers.put(CategoryPaper.TOOL_TUTORIAL, "Hướng dẫn sử dụng công cụ");
        CategoryPapers.put(CategoryPaper.BA_PM_PO, "BA, PM, PO");

        StatusPapers.put(PaperStatus.ACCEPT, "Đã duyệt");
        StatusPapers.put(PaperStatus.WAITING_UPLOAD, "Đang chờ");
        StatusPapers.put(PaperStatus.DELETED, "Đã Hủy");
        StatusPapers.put(PaperStatus.REFUSE, "Đã từ chối");

    }

    @GetMapping("viewUploadPage")
    public String viewUploadPage(Model model, HttpSession session) {
        try {
            if (SystemPageUIController.getUserSession(session) == null)
                return "redirect:/login";
            GitHubRepository.pullDB(environment.folderContainGithub);
            setupData4Page(model, PaperStatus.ACCEPT, session, null);
            model.addAttribute("title", "Danh sách các bài viết đã đăng");
            model.addAttribute("sidebarIndex", "0");
            return "UI/pages/article_module/articleUploadedPage";
        } catch (Exception e) {
            logger.error(e.getMessage());
            return "redirect:/system/error404";
        }
    }

    @GetMapping("viewDeleteArticlePage")
    public String viewDeleteArticlePage(Model model, HttpSession session, @RequestParam(required = false) String paperStatus) {
        try {
            if (SystemPageUIController.getUserSession(session) == null)
                return "redirect:/login";
            GitHubRepository.pullDB(environment.folderContainGithub);
            PaperStatus status = (paperStatus.equals(PaperStatus.DELETED.toString())) ? PaperStatus.DELETED : PaperStatus.REFUSE;
            setupData4Page(model, status, session, null);
            model.addAttribute("title", "Danh sách các bài viết đã xóa" + (status.equals(PaperStatus.DELETED) ? " sau khi đăng" : " sau khi đăng ký"));
            model.addAttribute("sidebarIndex", (status.equals(PaperStatus.DELETED)) ? "2" : "3");
            return "UI/pages/article_module/article_delete_refuse_page";
        } catch (Exception e) {
            logger.error(e.getMessage());
            return "redirect:/system/error404";
        }
    }

    @GetMapping("viewWaitingUploadPage")
    public String viewWaitingUploadPage(Model model, HttpSession session) {
        try {
            if (SystemPageUIController.getUserSession(session) == null)
                return "redirect:/login";
            GitHubRepository.pullDB(environment.folderContainGithub);
            setupData4Page(model, PaperStatus.WAITING_UPLOAD, session, null);
            model.addAttribute("title", "Danh sách các bài viết đang chờ duyệt");
            model.addAttribute("sidebarIndex", "1");
            return "UI/pages/article_module/articleWaitingUploadedPage";
        } catch (Exception e) {
            logger.error(e.getMessage());
            return "redirect:/system/error404";
        }
    }

    @GetMapping("acceptUploadPaper")
    public String viewHistoryPage(Model model, HttpSession session, @RequestParam String idPaper){
        try{
            UserDto user = SystemPageUIController.getUserSession(session);
            if (user == null)
                return "redirect:/login";
            GitHubRepository.pullDB(environment.folderContainGithub);
            PaperDto paper = paperService.getPaperById(idPaper);
            if(paper == null) return "redirect:/login";
            paper.setStatus(PaperStatus.ACCEPT);
            paper = paperService.updatePaper(paper);
            GitHubRepository.push2Git("accept upload paper!", user.getFullName());
            return "redirect:/articlePage/viewUploadPage";
        }catch (Exception e) {
            logger.error(e.getMessage());
            return "redirect:/system/error404";
        }
    }

    @GetMapping("viewHistoryPage")
    public String viewHistoryPage(Model model, HttpSession session, @RequestParam(required = false) String idPaper, @RequestParam(required = false) String idUser) {
        try {
            UserDto user = SystemPageUIController.getUserSession(session);
            if (user == null)
                return "redirect:/login";
            GitHubRepository.pullDB(environment.folderContainGithub);
            UserDto userIn4 = user;
            if (idPaper != null) {
                PaperDto paperDto = paperService.getPaperById(idPaper);
                userIn4 = userService.getUserByid(paperDto.getAuthor().getId());
            } else if (idUser != null) {
                userIn4 = userService.getUserByid(idUser);
            }
            setupData4Page(model, null, session, userIn4.getId());

            model.addAttribute("title", "Danh sách các bài viết đã đóng góp");
            model.addAttribute("infoUser", userIn4);
            model.addAttribute("role", SystemPageUIController.roleUser);
            model.addAttribute("flag", SystemPageUIController.flagUser);
            model.addAttribute("isEditable", user.getId().equals(userIn4.getId()));
            //return "UI/pages/article_module/history_articles_page/history_articles_page";
            return "UI/pages/article_module/informationUser";
        } catch (Exception e) {
            logger.error(e.getMessage());
            return "redirect:/system/error404";
        }
    }

    @GetMapping("detailPaperPage")
    public String viewDetailPaperPage(Model model, @RequestParam("gitUrl") String url, HttpSession session) {
        try {
            if (SystemPageUIController.getUserSession(session) == null)
                return "redirect:/login";
            String link = paperService.getUrlPDFInGithub(url);
            if (link == null) return "redirect:/system/error404";
            link = link.replace("/blob/", "/raw/");
            model.addAttribute("pdfUrl", link);
            return "UI/pages/article_module/paperDetailPage";
        }  catch (Exception e) {logger.error(e.getMessage());
            return "redirect:/system/error404";
        }
    }

    @GetMapping("recoverPaper")
    public String recoverPaper(@RequestParam(required = false) String idPaper, HttpSession session) {
        try {
            if (SystemPageUIController.getUserSession(session) == null)
                return "redirect:/login";
            GitHubRepository.pullDB(environment.folderContainGithub);
            PaperDto paperDto = paperService.getPaperById(idPaper);
            if (paperDto == null) return "redirect:/system/error404";
            if (paperDto.getStatus().equals(PaperStatus.DELETED)) paperDto.setStatus(PaperStatus.ACCEPT);
            if (paperDto.getStatus().equals(PaperStatus.REFUSE)) paperDto.setStatus(PaperStatus.WAITING_UPLOAD);
            paperDto = paperService.updatePaper(paperDto);
            GitHubRepository.push2Git("update paper", "Ông chủ");
            return "redirect:/articlePage/viewDeleteArticlePage?paperStatus=" +
                    (paperDto.getStatus().equals(PaperStatus.ACCEPT) ? PaperStatus.DELETED.toString() : PaperStatus.REFUSE.toString());

        } catch (Exception e) {
            logger.error(e.getMessage());
            return "redirect:/system/error404";
        }
    }

    @GetMapping("deletePaper")
    public String delete_upload_waiting_Paper(@RequestParam(required = false) String idPaper, HttpSession session) {
        try {
            if (SystemPageUIController.getUserSession(session) == null)
                return "redirect:/login";
            GitHubRepository.pullDB(environment.folderContainGithub);
            PaperDto paperDto = paperService.getPaperById(idPaper);
            if (paperDto == null) return "redirect:/system/error404";
            if (paperDto.getStatus().equals(PaperStatus.ACCEPT)) paperDto.setStatus(PaperStatus.DELETED);
            if (paperDto.getStatus().equals(PaperStatus.WAITING_UPLOAD)) paperDto.setStatus(PaperStatus.REFUSE);
            paperDto = paperService.updatePaper(paperDto);
            GitHubRepository.push2Git("update paper", "Ông chủ");
            if (paperDto.getStatus().equals(PaperStatus.DELETED))
                return "redirect:/articlePage/viewUploadPage";
            else
                return "redirect:/articlePage/viewWaitingUploadPage";

        } catch (Exception e) {
            return "redirect:/system/error404";
        }
    }

    // Add article Page
    @GetMapping("addPaperForm")
    public String addArticle(Model model, HttpSession session) {
        try {
            UserDto userDto = SystemPageUIController.getUserSession(session);
            if (userDto == null)
                return "redirect:/login";
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss", new Locale("vi", "VN"));
            String formattedDateTime = now.format(formatter);
            LocalDateTime dateTime = LocalDateTime.parse(formattedDateTime, formatter);
            System.out.println(dateTime);

            PaperDto paperDto = new PaperDto();
            paperDto.setAuthor(userDto);
            paperDto.setDateCreated(dateTime);
            model.addAttribute("paper", paperDto);
            model.addAttribute("editRight", userDto.getRole().equals(RoleUser.ADMIN) || userDto.getRole().equals(RoleUser.GLOBAL_ADMIN));
            model.addAttribute("CategoryPapers", CategoryPapers);
            return "UI/pages/article_module/addPaperPage";
        } catch (Exception e) {
            logger.error(e.getMessage());
            return "redirect:/system/error404";
        }
    }

    @PostMapping("upload")
    public String handleFormSubmission(@ModelAttribute("paper") PaperDto paperDto, @RequestParam MultipartFile img, Model model, HttpSession session) {
        // Process the submitted data
        try {
            if (SystemPageUIController.getUserSession(session) == null)
                return "redirect:/login";
            String fileName = img.getOriginalFilename();
            String fileExtension = getFileExtension(fileName);
            if (fileExtension != null && ALLOWED_EXTENSIONS.contains(fileExtension.toLowerCase())) {
                System.out.println("File is an image.");
            } else {
                return "redirect:/articlePage/addPaperForm";
            }
            //sync
            GitHubRepository.pullDB(environment.folderContainGithub);
            paperDto.setAuthor(SystemPageUIController.getUserSession(session));
            fileName = StringUtils.cleanPath(img.getOriginalFilename());
            Path uploadDir = Paths.get(environment.UserDirectory);
            Path filePath = uploadDir.resolve(fileName);
            Files.copy(img.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String directoryName = filePath.getParent().toString();
            String oldFile = directoryName + File.separator + fileName;
            String newFile = directoryName + File.separator + paperDto.getNamePaper() + "_" + paperDto.getAuthor().getUsername() + ".pdf";
            Resource oldResource = new FileSystemResource(oldFile);
            Resource newResource = new FileSystemResource(newFile);
            if (oldResource.getFile().renameTo(newResource.getFile())) {
                System.out.println("File renamed successfully");
            }
            Path destinationPath = Paths.get(environment.allPapers);
            ;

            if (paperDto.getAuthor().getRole().equals(RoleUser.GLOBAL_ADMIN) || paperDto.getAuthor().getRole().equals(RoleUser.ADMIN)) {
                paperDto.setStatus(PaperStatus.ACCEPT);

            } else {
                paperDto.setStatus(PaperStatus.WAITING_UPLOAD);
            }
            LocalDate today = LocalDate.now();
            String urlPaper = paperDto.getNamePaper() + "_" + paperDto.getAuthor().getUsername() + "_" + today.toString().replace("-", "_") + ".pdf";
            if (!checkIfPageNameExist(urlPaper, destinationPath)) {
                paperDto.setUrl(environment.gitHub_allPaper_Url + File.separator + urlPaper.replaceAll(" ","%20"));
                PaperDto rs = paperService.addPaper(paperDto);
                if (rs == null) return "redirect:/system/error404";
                System.out.println("ten bai bao:"+rs.getNamePaper());
                UserDto user = rs.getAuthor();
                user.setLastUserCreated(SystemPageUIController.getNow());
                rs.setAuthor(user);
                userService.updateUser(rs.getAuthor());
                Path sourcePath = Paths.get(newFile);
                String destinationFilePath = destinationPath + File.separator + urlPaper;
                System.out.println(destinationFilePath);
                //paperDto.setNamePaper(paperDto.getNamePaper() + "_" + paperDto.getAuthor().getUsername() + "_" + today.toString().replace("-","_"));
                destinationPath = Paths.get(destinationFilePath);
                System.out.println("des="+destinationFilePath);
                System.out.println("source="+sourcePath);
                Files.copy(sourcePath, destinationPath);
                GitHubRepository.push2Git("add paper", paperDto.getAuthor().getUsername());
            } else {
                return "redirect:/system/error404";
            }
            model.addAttribute("CategoryPapers", CategoryPapers);
            // Return a response indicating success
            setupData4Page(model, PaperStatus.ACCEPT, session, null);
            return "redirect:/articlePage/viewUploadPage";

        } catch (Exception e) {
            logger.error(e.getMessage());
            return "redirect:/system/error404";
        }
    }

    private boolean checkIfPageNameExist(String nameFile, Path destinationPath) {
        try {
            String filePath = destinationPath.toFile().getPath().toString() + "\\" + nameFile;
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

    public String getCategoryName(CategoryPaper cate) {

        return CategoryPapers.get(cate);
    }

    public boolean setupData4Page(Model model, PaperStatus paperStatus, HttpSession session, String idUser/* 4 detail page*/) {
        try {
            List<String> columnName = List.of();
            List<List<String>> data = new ArrayList<>();
            List<String> idPapers = new ArrayList<>();
            UserDto user = SystemPageUIController.getUserSession(session);
            if (paperStatus != null) {
                List<PaperDto> papers = paperService.getAllPaperByStatus(paperStatus);
                columnName = List.of("Tên bài viết", "Thể loại", "Tác giả", "Ngày tạo", "Link");
                for (int i = 0; i < papers.size(); i++) {
                    PaperDto paper = papers.get(i);
                    data.add(List.of(paper.getNamePaper(), getCategoryName(paper.getCategoryPaper()), paper.getAuthor().getUsername(), paper.getDateCreated().toString(), paper.getUrl()));
                    idPapers.add(paper.getId());
                }
            } else {
                if (user != null) {
                    columnName = List.of("Tên bài viết", "Thể loại", "Trạng thái", "Ngày tạo", "Link");
                    String idUser4Find = idUser == null ? user.getId() : idUser;
                    List<PaperDto> papers = paperService.getAllPaperByIdAuthor(idUser4Find);

                    if (idUser != null && !(user.getRole().equals(RoleUser.ADMIN) || user.getRole().equals(RoleUser.GLOBAL_ADMIN)))
                        papers = papers.stream().filter(p -> p.getStatus().equals(PaperStatus.ACCEPT)).collect(Collectors.toList());


                    List<PaperStatus> statusOrder = Arrays.asList(PaperStatus.ACCEPT, PaperStatus.WAITING_UPLOAD, PaperStatus.REFUSE, PaperStatus.DELETED);

                    papers = papers.stream()
                            .filter(p -> statusOrder.contains(p.getStatus()))
                            .sorted(Comparator.comparingInt(p -> statusOrder.indexOf(p.getStatus())))
                            .collect(Collectors.toList());
                    for (int i = 0; i < papers.size(); i++) {
                        PaperDto paper = papers.get(i);
                        data.add(List.of(paper.getNamePaper(), getCategoryName(paper.getCategoryPaper()), StatusPapers.get(paper.getStatus()), paper.getDateCreated().toString(), paper.getUrl()));
                        idPapers.add(paper.getId());
                    }
                }
            }
            model.addAttribute("columnName", columnName);
            model.addAttribute("data", data);
            model.addAttribute("idPapers", idPapers);
            if (user.getRole().equals(RoleUser.ADMIN) || user.getRole().equals(RoleUser.GLOBAL_ADMIN))
                model.addAttribute("editRight", true);
            else
                model.addAttribute("editRight", false);
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return false;
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName != null && fileName.lastIndexOf(".") != -1) {
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        }
        return null;
    }


}
