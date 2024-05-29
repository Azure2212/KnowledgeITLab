package com.example.springboot_github_mvc.service.impl;

import com.example.springboot_github_mvc.dtos.PaperDto;
import com.example.springboot_github_mvc.enumm.PaperStatus;
import com.example.springboot_github_mvc.repository.PaperRepository;
import com.example.springboot_github_mvc.service.IPaperService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class PaperServiceImpl implements IPaperService {
    private final PaperRepository paperRepository;

    @Override
    public List<PaperDto> getAllPaper() {
        try {
            return paperRepository.findAll();
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    @Override
    public PaperDto getPaperById(String id) {
        List<PaperDto> list = paperRepository.findAll();
        list = list.stream().filter(l -> l.getId().equals(id)).collect(Collectors.toList());
        if(list.size() == 0) return null;
        return list.get(0);
    }

    @Override
    public PaperDto addPaper(PaperDto PaperDto) {
        try {
            PaperDto.setId(UUID.randomUUID().toString());
            return paperRepository.insert(PaperDto);
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    @Override
    public PaperDto deletePaper(PaperDto PaperDto) {
        try {
            return paperRepository.delete(PaperDto);
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    @Override
    public PaperDto updatePaper(PaperDto PaperDto) {
        try {
            return paperRepository.update(PaperDto);
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    @Override
    public String getUrlPDFInGithub(String id) {
        List<PaperDto> papers = paperRepository.findAll();
        for(PaperDto paper : papers){
            if(paper.getId().equals(id)) return paper.getUrl();
        }
        return null;
    }

    @Override
    public List<PaperDto> getAllPaperByStatus(PaperStatus status) {
        List<PaperDto> list = paperRepository.findAll();
        Collections.sort(list, Comparator.comparing(PaperDto::getDateCreated).reversed());
        List<PaperDto> rs = new ArrayList<>();
        for(PaperDto paper : list)
            if(paper.getStatus().equals(status))
                rs.add(paper);
        return rs;
    }

    @Override
    public List<PaperDto> getAllPaperByIdAuthor(String authorId) {
        List<PaperDto> list = paperRepository.findAll();
        list = list.stream().filter(l -> l.getAuthor().getId().equals(authorId)).collect(Collectors.toList());
        Collections.sort(list, Comparator.comparing(PaperDto::getDateCreated).reversed());
        return list;
    }


}
