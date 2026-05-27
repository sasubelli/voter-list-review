package org.example.voterreview.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class AnalysisResult {
    private final String jobId;
    private final Instant createdAt;
    private final List<VoterRecord> voterRecords;
    private final List<RemovedRecord> removedRecords;
    private final List<MatchCandidate> duplicateMatches;
    private final List<MatchCandidate> removedMatches;
    private final List<String> warnings;
    private DeceasedSearchRequest lastSearchRequest;
    private List<MatchCandidate> lastSearchResults;

    public AnalysisResult(
            String jobId,
            List<VoterRecord> voterRecords,
            List<RemovedRecord> removedRecords,
            List<MatchCandidate> duplicateMatches,
            List<MatchCandidate> removedMatches,
            List<String> warnings
    ) {
        this.jobId = jobId;
        this.createdAt = Instant.now();
        this.voterRecords = List.copyOf(voterRecords);
        this.removedRecords = List.copyOf(removedRecords);
        this.duplicateMatches = List.copyOf(duplicateMatches);
        this.removedMatches = List.copyOf(removedMatches);
        this.warnings = List.copyOf(warnings);
        this.lastSearchResults = new ArrayList<>();
    }

    public String getJobId() {
        return jobId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<VoterRecord> getVoterRecords() {
        return voterRecords;
    }

    public List<RemovedRecord> getRemovedRecords() {
        return removedRecords;
    }

    public List<MatchCandidate> getDuplicateMatches() {
        return duplicateMatches;
    }

    public List<MatchCandidate> getRemovedMatches() {
        return removedMatches;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public int getRecordCount() {
        return voterRecords.size();
    }

    public int getRemovedRecordCount() {
        return removedRecords.size();
    }

    public int getDuplicateCount() {
        return duplicateMatches.size();
    }

    public int getRemovedMatchCount() {
        return removedMatches.size();
    }

    public DeceasedSearchRequest getLastSearchRequest() {
        return lastSearchRequest;
    }

    public void setLastSearchRequest(DeceasedSearchRequest lastSearchRequest) {
        this.lastSearchRequest = lastSearchRequest;
    }

    public List<MatchCandidate> getLastSearchResults() {
        return lastSearchResults;
    }

    public void setLastSearchResults(List<MatchCandidate> lastSearchResults) {
        this.lastSearchResults = List.copyOf(lastSearchResults);
    }

    public int getLastSearchResultCount() {
        return lastSearchResults.size();
    }
}
