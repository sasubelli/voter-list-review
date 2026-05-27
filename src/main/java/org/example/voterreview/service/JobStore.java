package org.example.voterreview.service;

import org.example.voterreview.model.AnalysisResult;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JobStore {
    private final Map<String, AnalysisResult> jobs = new ConcurrentHashMap<>();

    public void save(AnalysisResult result) {
        jobs.put(result.getJobId(), result);
    }

    public Optional<AnalysisResult> find(String jobId) {
        return Optional.ofNullable(jobs.get(jobId));
    }
}
