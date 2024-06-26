package com.example.springboot_github_mvc.service;

import com.example.springboot_github_mvc.dtos.PaperDto;
import com.example.springboot_github_mvc.enumm.PaperStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface IPaperService {

    public List<PaperDto> getAllPaper();

    public PaperDto getPaperById(String id);
    public PaperDto addPaper(PaperDto paperDto);

    public PaperDto deletePaper(PaperDto paperDto);

    public PaperDto updatePaper(PaperDto paperDto);

    public String getUrlPDFInGithub(String id);

    public List<PaperDto> getAllPaperByStatus(PaperStatus status);

    public List<PaperDto> getAllPaperByIdAuthor(String authorId);

    class enviroment {

        //the place store github
        public static String folderContainGithub = "src/main/java/com/example/springboot_github_mvc/DB/Github_DB";

        // all folder in github
        public static String databaseUrl = "src/main/java/com/example/springboot_github_mvc/DB/Github_DB/databases";
        public static String allPapers = "src/main/java/com/example/springboot_github_mvc/DB/Github_DB/allAcceptedPaper";

        public static String avatarUrl = "src/main/java/com/example/springboot_github_mvc/DB/Github_DB/avatar_user";

        //DB path
        public static String userTableUrl = "src/main/java/com/example/springboot_github_mvc/DB/Github_DB/databases/userTable.json";
        public static String paperTableUrl = "src/main/java/com/example/springboot_github_mvc/DB/Github_DB/databases/paperTable.json";

        public static String UserDirectory = "src/main/java/com/example/springboot_github_mvc/DB/userPlace";

        public static String gitHub_allPaper_Url = "https://github.com/azurebob2212/knowledgeTreeDocumentDataLab/blob/main/allAcceptedPaper";

        public static String gitHub_userAvatar_Url = "https://github.com/azurebob2212/knowledgeTreeDocumentDataLab/blob/main/avatar_user";
    }
}
