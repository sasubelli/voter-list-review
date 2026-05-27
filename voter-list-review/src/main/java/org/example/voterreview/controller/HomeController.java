package org.example.voterreview.controller;

import org.example.voterreview.model.AnalysisResult;
import org.example.voterreview.service.AnalysisService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HomeController {
    private final AnalysisService analysisService;

    public HomeController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/analyze")
    public String analyze(
            @RequestParam("voterListFile") MultipartFile voterListFile,
            @RequestParam(value = "removedListFile", required = false) MultipartFile removedListFile,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        try {
            AnalysisResult result = analysisService.analyze(voterListFile, removedListFile);
            return "redirect:/analysis/" + result.getJobId();
        } catch (Exception ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "index";
        }
    }
}
