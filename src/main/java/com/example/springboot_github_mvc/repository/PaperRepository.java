package com.example.springboot_github_mvc.repository;

import com.example.springboot_github_mvc.dtos.PaperDto;
import com.example.springboot_github_mvc.enviroment;
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
public class PaperRepository {

    private String paperTableUrl = enviroment.paperTableUrl;

    public List<PaperDto> findAll() {
        try {
            // Convert the list to JSON string
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            System.out.println(paperTableUrl);
            // Write JSON string to file
            List<PaperDto> paperDtos = objectMapper.readValue(new File(paperTableUrl),
                    new TypeReference<List<PaperDto>>() {
                    });

            System.out.println("JSON data has been written to papers .json file.");
            return paperDtos;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public PaperDto insert(PaperDto paperDto) {
        try {
//            GitHubRepository.pullDB(enviroment.folderContentGithub);
            List<PaperDto> paperDtos = findAll();

            int index = paperDtos.indexOf(paperDtos.stream()
                    .filter(paper -> paper.getNamePaper() != null)
                    .filter(paper -> paper.getNamePaper().equals(paperDto.getNamePaper()))
                    .findFirst()
                    .orElse(null));

            if (index != -1) {
                System.out.println("exist element with name found.");
                return null;
            }
            paperDtos.add(paperDto);
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());

            String jsonString = objectMapper.writeValueAsString(paperDtos);

            // Write JSON string to file
            objectMapper.writeValue(new File(paperTableUrl), paperDtos);
            return paperDto;

        } catch (Exception e) {
            System.out.println("error :" + e);
            return null;
        }
    }

    public PaperDto delete(PaperDto PaperDto) {
        try {
//            GitHubRepository.pullDB(enviroment.folderContentGithub);
            List<PaperDto> PaperDtos = findAll();
            if (PaperDtos.size() == 0) return new PaperDto();

            Iterator<PaperDto> iterator = PaperDtos.iterator();
            while (iterator.hasNext()) {
                PaperDto paper = iterator.next();
                if (paper.getId().equals(PaperDto.getId())) {
                    iterator.remove();
                    System.out.println("paper with id: " + paper.getId() + " is removed successfully.");
                }
            }

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            String jsonString = objectMapper.writeValueAsString(PaperDtos);

            // Write JSON string to file
            objectMapper.writeValue(new File(paperTableUrl), PaperDtos);
            return PaperDto;
        } catch (Exception e) {
            System.out.println("error :" + e);
            return null;
        }
    }

    public PaperDto update(PaperDto paperDto) {
        try {
//            GitHubRepository.pullDB(enviroment.folderContentGithub);
            List<PaperDto> paperDtos = findAll();
            if (paperDtos.size() == 0) return new PaperDto();

            int index = paperDtos.indexOf(paperDtos.stream()
                    .filter(paper -> paper.getId().equals(paperDto.getId()))
                    .findFirst()
                    .orElse(null));

            if (index == -1) {
                System.out.println("Not found!");
                return null;
            }

            paperDtos.set(index, paperDto);
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());

            String jsonString = objectMapper.writeValueAsString(paperDtos);

            // Write JSON string to file
            objectMapper.writeValue(new File(paperTableUrl), paperDtos);
            return paperDto;
        } catch (Exception e) {
            System.out.println("error :" + e);
            return null;
        }
    }
}
