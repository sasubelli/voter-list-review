package org.example.voterreview.model;

public class MatchCandidate {
    private final MatchLabel label;
    private final double score;
    private final String reason;
    private final String searchTerm;
    private final VoterRecord primaryRecord;
    private final VoterRecord secondaryRecord;
    private final RemovedRecord removedRecord;

    private MatchCandidate(
            MatchLabel label,
            double score,
            String reason,
            String searchTerm,
            VoterRecord primaryRecord,
            VoterRecord secondaryRecord,
            RemovedRecord removedRecord
    ) {
        this.label = label;
        this.score = score;
        this.reason = reason;
        this.searchTerm = searchTerm;
        this.primaryRecord = primaryRecord;
        this.secondaryRecord = secondaryRecord;
        this.removedRecord = removedRecord;
    }

    public static MatchCandidate duplicate(VoterRecord left, VoterRecord right, MatchLabel label, double score, String reason) {
        return new MatchCandidate(label, score, reason, null, left, right, null);
    }

    public static MatchCandidate removed(VoterRecord voterRecord, RemovedRecord removedRecord, MatchLabel label, double score, String reason) {
        return new MatchCandidate(label, score, reason, null, voterRecord, null, removedRecord);
    }

    public static MatchCandidate deceasedSearch(String searchTerm, VoterRecord voterRecord, MatchLabel label, double score, String reason) {
        return new MatchCandidate(label, score, reason, searchTerm, voterRecord, null, null);
    }

    public MatchLabel getLabel() {
        return label;
    }

    public double getScore() {
        return score;
    }

    public String getReason() {
        return reason;
    }

    public String getSearchTerm() {
        return searchTerm;
    }

    public VoterRecord getPrimaryRecord() {
        return primaryRecord;
    }

    public VoterRecord getSecondaryRecord() {
        return secondaryRecord;
    }

    public RemovedRecord getRemovedRecord() {
        return removedRecord;
    }
}
