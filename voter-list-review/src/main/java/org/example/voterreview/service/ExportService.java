package org.example.voterreview.service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.example.voterreview.model.AnalysisResult;
import org.example.voterreview.model.MatchCandidate;
import org.example.voterreview.model.RemovedRecord;
import org.example.voterreview.model.VoterRecord;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringWriter;

@Service
public class ExportService {

    public String exportDuplicates(AnalysisResult result) throws IOException {
        StringWriter writer = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(
                "label", "score", "reason", "leftVoterId", "leftName", "rightVoterId", "rightName"))) {
            for (MatchCandidate match : result.getDuplicateMatches()) {
                VoterRecord left = match.getPrimaryRecord();
                VoterRecord right = match.getSecondaryRecord();
                printer.printRecord(match.getLabel(), match.getScore(), match.getReason(),
                        left.getVoterId(), left.getName(), right.getVoterId(), right.getName());
            }
        }
        return writer.toString();
    }

    public String exportRemoved(AnalysisResult result) throws IOException {
        StringWriter writer = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(
                "label", "score", "reason", "voterId", "voterName", "removedVoterId", "removedName"))) {
            for (MatchCandidate match : result.getRemovedMatches()) {
                VoterRecord voter = match.getPrimaryRecord();
                RemovedRecord removed = match.getRemovedRecord();
                printer.printRecord(match.getLabel(), match.getScore(), match.getReason(),
                        voter.getVoterId(), voter.getName(), removed.getVoterId(), removed.getName());
            }
        }
        return writer.toString();
    }

    public String exportDeceased(AnalysisResult result) throws IOException {
        StringWriter writer = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(
                "searchTerm", "label", "score", "reason", "voterId", "name", "address"))) {
            for (MatchCandidate match : result.getLastSearchResults()) {
                VoterRecord voter = match.getPrimaryRecord();
                printer.printRecord(match.getSearchTerm(), match.getLabel(), match.getScore(), match.getReason(),
                        voter.getVoterId(), voter.getName(), voter.getAddress());
            }
        }
        return writer.toString();
    }
}
