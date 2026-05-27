package org.example.voterreview.service;

import org.example.voterreview.model.AnalysisResult;
import org.example.voterreview.model.DeceasedSearchRequest;
import org.example.voterreview.model.MatchCandidate;
import org.example.voterreview.model.RemovedRecord;
import org.example.voterreview.model.VoterRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AnalysisService {
    private final DocumentIntakeService documentIntakeService;
    private final MatchingService matchingService;
    private final JobStore jobStore;

    public AnalysisService(DocumentIntakeService documentIntakeService, MatchingService matchingService, JobStore jobStore) {
        this.documentIntakeService = documentIntakeService;
        this.matchingService = matchingService;
        this.jobStore = jobStore;
    }

    public AnalysisResult analyze(MultipartFile voterListFile, MultipartFile removedListFile) throws IOException {
        if (voterListFile == null || voterListFile.isEmpty()) {
            throw new IllegalArgumentException("A voter list file is required.");
        }
        Path workingDirectory = Files.createTempDirectory("voter-review-");
        ParsedDocument<VoterRecord> voters = documentIntakeService.parseVoters(voterListFile, workingDirectory);
        ParsedDocument<RemovedRecord> removed = emptyRemovedResult();
        if (removedListFile != null && !removedListFile.isEmpty()) {
            removed = documentIntakeService.parseRemoved(removedListFile, workingDirectory);
        }

        List<String> warnings = new ArrayList<>();
        warnings.addAll(voters.warnings());
        warnings.addAll(removed.warnings());

        String jobId = UUID.randomUUID().toString();
        AnalysisResult result = new AnalysisResult(
                jobId,
                voters.records(),
                removed.records(),
                matchingService.findDuplicateMatches(voters.records()),
                matchingService.findRemovedMatches(voters.records(), removed.records()),
                warnings
        );
        jobStore.save(result);
        return result;
    }

    public AnalysisResult getJob(String jobId) {
        return jobStore.find(jobId)
                .orElseThrow(() -> new IllegalArgumentException("No analysis job found for id " + jobId));
    }

    public List<MatchCandidate> searchDeceased(String jobId, DeceasedSearchRequest request) {
        AnalysisResult result = getJob(jobId);
        List<MatchCandidate> matches = matchingService.searchDeceasedCandidates(request, result.getVoterRecords());
        result.setLastSearchRequest(request);
        result.setLastSearchResults(matches);
        return matches;
    }

    private ParsedDocument<RemovedRecord> emptyRemovedResult() {
        return new ParsedDocument<>(List.of(), List.of(), false);
    }
}
