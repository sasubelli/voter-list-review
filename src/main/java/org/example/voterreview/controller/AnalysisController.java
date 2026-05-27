package org.example.voterreview.controller;

import jakarta.validation.Valid;
import org.example.voterreview.model.AnalysisResult;
import org.example.voterreview.model.DeceasedSearchRequest;
import org.example.voterreview.service.AnalysisService;
import org.example.voterreview.service.ExportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Controller
public class AnalysisController {
    private final AnalysisService analysisService;
    private final ExportService exportService;

    public AnalysisController(AnalysisService analysisService, ExportService exportService) {
        this.analysisService = analysisService;
        this.exportService = exportService;
    }

    @GetMapping("/analysis/{jobId}")
    public String summary(@PathVariable String jobId, Model model) {
        AnalysisResult result = analysisService.getJob(jobId);
        model.addAttribute("result", result);
        return "summary";
    }

    @GetMapping("/analysis/{jobId}/duplicates")
    public String duplicates(@PathVariable String jobId, Model model) {
        model.addAttribute("result", analysisService.getJob(jobId));
        return "duplicates";
    }

    @GetMapping("/analysis/{jobId}/removed")
    public String removed(@PathVariable String jobId, Model model) {
        model.addAttribute("result", analysisService.getJob(jobId));
        return "removed";
    }

    @GetMapping("/analysis/{jobId}/deceased-search")
    public String deceasedSearch(@PathVariable String jobId, Model model) {
        AnalysisResult result = analysisService.getJob(jobId);
        model.addAttribute("result", result);
        model.addAttribute("request", result.getLastSearchRequest() == null ? new DeceasedSearchRequest() : result.getLastSearchRequest());
        return "deceased-search";
    }

    @PostMapping("/analysis/{jobId}/deceased-search")
    public String runDeceasedSearch(
            @PathVariable String jobId,
            @Valid @ModelAttribute("request") DeceasedSearchRequest request,
            BindingResult bindingResult,
            Model model
    ) {
        AnalysisResult result = analysisService.getJob(jobId);
        model.addAttribute("result", result);
        if (bindingResult.hasErrors()) {
            return "deceased-search";
        }
        model.addAttribute("matches", analysisService.searchDeceased(jobId, request));
        model.addAttribute("result", analysisService.getJob(jobId));
        return "deceased-search";
    }

    @GetMapping("/analysis/{jobId}/export/{reportType}")
    public ResponseEntity<byte[]> export(@PathVariable String jobId, @PathVariable String reportType) throws IOException {
        AnalysisResult result = analysisService.getJob(jobId);
        String csv;
        if ("duplicates".equals(reportType)) {
            csv = exportService.exportDuplicates(result);
        } else if ("removed".equals(reportType)) {
            csv = exportService.exportRemoved(result);
        } else if ("deceased".equals(reportType)) {
            csv = exportService.exportDeceased(result);
        } else {
            throw new IllegalArgumentException("Unsupported report type: " + reportType);
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + reportType + "-" + jobId + ".csv\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(csv.getBytes(StandardCharsets.UTF_8));
    }
}
