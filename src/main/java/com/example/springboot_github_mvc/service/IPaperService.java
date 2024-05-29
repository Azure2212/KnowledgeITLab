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
}
