package org.example.voterreview.controller;

import org.example.voterreview.model.AnalysisResult;
import org.example.voterreview.model.DeceasedSearchRequest;
import org.example.voterreview.model.MatchCandidate;
import org.example.voterreview.model.MatchLabel;
import org.example.voterreview.model.RemovedRecord;
import org.example.voterreview.model.VoterRecord;
import org.example.voterreview.service.AnalysisService;
import org.example.voterreview.service.ExportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.servlet.view.AbstractView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class AnalysisControllerWebTest {
    private MockMvc mockMvc;
    private StubAnalysisService analysisService;
    private StubExportService exportService;

    @BeforeEach
    void setUp() {
        analysisService = new StubAnalysisService();
        exportService = new StubExportService();

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(
                        new HomeController(analysisService),
                        new AnalysisController(analysisService, exportService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .setViewResolvers((viewName, locale) -> {
                    if (viewName.startsWith("redirect:")) {
                        return new RedirectView(viewName.substring("redirect:".length()));
                    }
                    return new NoOpView();
                })
                .build();
    }

    @Test
    void uploadRedirectsToSummary() throws Exception {
        mockMvc.perform(multipart("/analyze")
                        .file(new MockMultipartFile("voterListFile", "voters.txt", "text/plain", "Name: Test".getBytes())))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/analysis/job-1"));
    }

    @Test
    void summaryRendersCounts() throws Exception {
        mockMvc.perform(get("/analysis/job-1"))
                .andExpect(status().isOk())
                .andExpect(view().name("summary"))
                .andExpect(model().attributeExists("result"));
    }

    @Test
    void exportReturnsCsv() throws Exception {
        mockMvc.perform(get("/analysis/job-1/export/duplicates"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.valueOf("text/csv")))
                .andExpect(content().string("label,score\n"));
    }

    @Test
    void deceasedSearchValidatesName() throws Exception {
        mockMvc.perform(post("/analysis/job-1/deceased-search")
                        .param("name", "")
                        .param("address", "Road"))
                .andExpect(status().isOk())
                .andExpect(view().name("deceased-search"))
                .andExpect(model().attributeHasFieldErrors("request", "name"));
    }

    private static final class StubAnalysisService extends AnalysisService {
        private final AnalysisResult result = sampleResult();

        private StubAnalysisService() {
            super(null, null, null);
        }

        @Override
        public AnalysisResult analyze(org.springframework.web.multipart.MultipartFile voterListFile,
                                      org.springframework.web.multipart.MultipartFile removedListFile) {
            return result;
        }

        @Override
        public AnalysisResult getJob(String jobId) {
            return result;
        }

        @Override
        public List<MatchCandidate> searchDeceased(String jobId, DeceasedSearchRequest request) {
            result.setLastSearchRequest(request);
            return result.getLastSearchResults();
        }
    }

    private static final class StubExportService extends ExportService {
        @Override
        public String exportDuplicates(AnalysisResult result) {
            return "label,score\n";
        }

        @Override
        public String exportRemoved(AnalysisResult result) {
            return "label,score\n";
        }

        @Override
        public String exportDeceased(AnalysisResult result) {
            return "label,score\n";
        }
    }

    private static AnalysisResult sampleResult() {
        VoterRecord voter = new VoterRecord("doc", "doc#1", "EPIC1", "Sita Devi", "Ram Lal", "45", "Female", "River Road", "7", "1");
        MatchCandidate duplicate = MatchCandidate.duplicate(voter, voter, MatchLabel.EXACT_ID_MATCH, 1.0, "same id");
        AnalysisResult result = new AnalysisResult("job-1", List.of(voter), List.of(RemovedRecord.fromVoterRecord(voter)), List.of(duplicate), List.of(), List.of());
        DeceasedSearchRequest request = new DeceasedSearchRequest();
        request.setName("Sita Devi");
        result.setLastSearchRequest(request);
        result.setLastSearchResults(List.of(duplicate));
        return result;
    }

    private static final class NoOpView extends AbstractView {
        @Override
        protected void renderMergedOutputModel(
                Map<String, Object> model,
                HttpServletRequest request,
                HttpServletResponse response
        ) {
            response.setStatus(HttpServletResponse.SC_OK);
        }
    }
}
