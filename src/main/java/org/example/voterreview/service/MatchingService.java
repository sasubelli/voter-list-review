package org.example.voterreview.service;

import org.example.voterreview.model.DeceasedSearchRequest;
import org.example.voterreview.model.MatchCandidate;
import org.example.voterreview.model.MatchLabel;
import org.example.voterreview.model.RemovedRecord;
import org.example.voterreview.model.VoterRecord;
import org.example.voterreview.util.RecordNormalizer;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class MatchingService {
    private final RecordNormalizer normalizer;

    public MatchingService(RecordNormalizer normalizer) {
        this.normalizer = normalizer;
    }

    public List<MatchCandidate> findDuplicateMatches(List<VoterRecord> voterRecords) {
        List<MatchCandidate> matches = new ArrayList<>();
        for (int i = 0; i < voterRecords.size(); i++) {
            for (int j = i + 1; j < voterRecords.size(); j++) {
                VoterRecord left = voterRecords.get(i);
                VoterRecord right = voterRecords.get(j);
                determineMatch(left, right)
                        .map(candidate -> MatchCandidate.duplicate(
                                left,
                                right,
                                candidate.label(),
                                candidate.score(),
                                candidate.reason()))
                        .ifPresent(matches::add);
            }
        }
        matches.sort(Comparator.comparing(MatchCandidate::getLabel).thenComparing(MatchCandidate::getScore).reversed());
        return matches;
    }

    public List<MatchCandidate> findRemovedMatches(List<VoterRecord> voterRecords, List<RemovedRecord> removedRecords) {
        List<MatchCandidate> matches = new ArrayList<>();
        for (VoterRecord voterRecord : voterRecords) {
            for (RemovedRecord removedRecord : removedRecords) {
                determineMatch(voterRecord, removedRecord.asVoterRecord())
                        .map(candidate -> MatchCandidate.removed(
                                voterRecord,
                                removedRecord,
                                candidate.label(),
                                candidate.score(),
                                candidate.reason()))
                        .ifPresent(matches::add);
            }
        }
        matches.sort(Comparator.comparing(MatchCandidate::getLabel).thenComparing(MatchCandidate::getScore).reversed());
        return matches;
    }

    public List<MatchCandidate> searchDeceasedCandidates(DeceasedSearchRequest request, List<VoterRecord> voterRecords) {
        List<MatchCandidate> matches = new ArrayList<>();
        VoterRecord queryRecord = new VoterRecord(
                "manual-search",
                "manual-search",
                request.getVoterId(),
                request.getName(),
                null,
                request.getAgeOrDob(),
                null,
                request.getAddress(),
                null,
                null
        );
        for (VoterRecord voterRecord : voterRecords) {
            determineMatch(queryRecord, voterRecord)
                    .map(candidate -> MatchCandidate.deceasedSearch(
                            request.getName(),
                            voterRecord,
                            candidate.label(),
                            candidate.score(),
                            candidate.reason()))
                    .ifPresent(matches::add);
        }
        matches.sort(Comparator.comparing(MatchCandidate::getScore).reversed());
        return matches;
    }

    private Optional<MatchDecision> determineMatch(VoterRecord left, VoterRecord right) {
        if (normalizer.exactIdentifierMatch(left.voterId(), right.voterId())) {
            return Optional.of(new MatchDecision(MatchLabel.EXACT_ID_MATCH, 1.0, "Matching voter ID / EPIC"));
        }

        boolean exactName = normalizer.exactMatch(left.name(), right.name());
        boolean exactAge = normalizer.exactMatch(normalizer.normalizeAgeOrDob(left.ageOrDob()), normalizer.normalizeAgeOrDob(right.ageOrDob()));
        boolean exactAddress = normalizer.exactMatch(left.address(), right.address());
        boolean exactRelation = normalizer.exactMatch(left.fatherOrSpouseName(), right.fatherOrSpouseName());

        if (exactName && (exactAge || exactAddress || exactRelation)) {
            return Optional.of(new MatchDecision(MatchLabel.EXACT_PERSON_MATCH, 0.95, "Exact name with supporting field match"));
        }

        boolean phoneticName = normalizer.phoneticMatch(left.name(), right.name());
        if (!phoneticName) {
            return Optional.empty();
        }

        double score = 0.55;
        if (exactAge) {
            score += 0.15;
        }
        if (exactAddress) {
            score += 0.15;
        } else {
            score += Math.min(0.10, normalizer.similarity(left.address(), right.address()) * 0.10);
        }
        if (exactRelation || normalizer.phoneticMatch(left.fatherOrSpouseName(), right.fatherOrSpouseName())) {
            score += 0.10;
        }
        if (!normalizer.normalizeIdentifier(left.voterId()).isBlank() && !normalizer.normalizeIdentifier(right.voterId()).isBlank()) {
            score += Math.min(0.05, normalizer.similarity(left.voterId(), right.voterId()) * 0.05);
        }

        if (score >= 0.60) {
            return Optional.of(new MatchDecision(MatchLabel.PHONETIC_REVIEW, Math.min(score, 0.89), "Phonetic name match with supporting similarities"));
        }
        return Optional.empty();
    }

    private record MatchDecision(MatchLabel label, double score, String reason) {
    }
}
