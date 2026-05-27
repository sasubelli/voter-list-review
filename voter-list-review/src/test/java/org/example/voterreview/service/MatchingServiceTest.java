package org.example.voterreview.service;

import org.example.voterreview.model.DeceasedSearchRequest;
import org.example.voterreview.model.MatchCandidate;
import org.example.voterreview.model.MatchLabel;
import org.example.voterreview.model.RemovedRecord;
import org.example.voterreview.model.VoterRecord;
import org.example.voterreview.util.RecordNormalizer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MatchingServiceTest {
    private final MatchingService matchingService = new MatchingService(new RecordNormalizer());

    @Test
    void findsExactIdDuplicates() {
        VoterRecord first = voter("EPIC1001", "Lakshmi Devi", "42", "Lane 1");
        VoterRecord second = voter("EPIC1001", "Lakshmi Devi", "42", "Lane 1");

        List<MatchCandidate> matches = matchingService.findDuplicateMatches(List.of(first, second));

        assertThat(matches).hasSize(1);
        assertThat(matches.getFirst().getLabel()).isEqualTo(MatchLabel.EXACT_ID_MATCH);
    }

    @Test
    void findsRemovedMatchesUsingExactPersonData() {
        VoterRecord voter = voter(null, "Gopal Rao", "55", "Main Street");
        RemovedRecord removed = RemovedRecord.fromVoterRecord(voter("OLD11", "Gopal Rao", "55", "Main Street"));

        List<MatchCandidate> matches = matchingService.findRemovedMatches(List.of(voter), List.of(removed));

        assertThat(matches).hasSize(1);
        assertThat(matches.getFirst().getLabel()).isEqualTo(MatchLabel.EXACT_PERSON_MATCH);
    }

    @Test
    void findsPhoneticManualSearchMatches() {
        VoterRecord voter = voter("EPIC88", "Smyth Kumar", "61", "Market Road");
        DeceasedSearchRequest request = new DeceasedSearchRequest();
        request.setName("Smith Kumar");
        request.setAgeOrDob("61");
        request.setAddress("Market Road");

        List<MatchCandidate> matches = matchingService.searchDeceasedCandidates(request, List.of(voter));

        assertThat(matches).hasSize(1);
        assertThat(matches.getFirst().getLabel()).isEqualTo(MatchLabel.PHONETIC_REVIEW);
    }

    private VoterRecord voter(String voterId, String name, String ageOrDob, String address) {
        return new VoterRecord("source", "source#1", voterId, name, "Parent Name", ageOrDob, "Female", address, "12", "7");
    }
}
