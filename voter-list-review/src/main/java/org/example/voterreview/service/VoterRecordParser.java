package org.example.voterreview.service;

import org.example.voterreview.model.RemovedRecord;
import org.example.voterreview.model.VoterRecord;
import org.example.voterreview.util.RecordNormalizer;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class VoterRecordParser {
    private static final Pattern ID_PATTERN = Pattern.compile("(?i)(?:epic|voter(?:\\s+id)?)\\s*[:\\-]?\\s*([A-Z]{1,5}[0-9]{4,}|[A-Z0-9]{6,})");
    private static final Pattern NAME_PATTERN = Pattern.compile("(?i)(?:name|elector(?:'?s)?\\s+name)\\s*[:\\-]?\\s*([A-Za-z .]+)");
    private static final Pattern RELATION_PATTERN = Pattern.compile("(?i)(?:father|spouse|husband|wife|mother)(?:'?s)?\\s+name\\s*[:\\-]?\\s*([A-Za-z .]+)");
    private static final Pattern AGE_PATTERN = Pattern.compile("(?i)(?:age|dob|date\\s+of\\s+birth)\\s*[:\\-]?\\s*([A-Za-z0-9/\\- ]+)");
    private static final Pattern GENDER_PATTERN = Pattern.compile("(?i)(?:gender|sex)\\s*[:\\-]?\\s*(male|female|other)");
    private static final Pattern ADDRESS_PATTERN = Pattern.compile("(?i)(?:house\\s+no|address)\\s*[:\\-]?\\s*([A-Za-z0-9,./\\- ]+)");
    private static final Pattern PART_PATTERN = Pattern.compile("(?i)(?:part\\s+no|part number)\\s*[:\\-]?\\s*([A-Za-z0-9\\-]+)");
    private static final Pattern SERIAL_PATTERN = Pattern.compile("(?i)(?:serial\\s+no|sl\\s*no|serial number)\\s*[:\\-]?\\s*([A-Za-z0-9\\-]+)");

    private final RecordNormalizer normalizer;

    public VoterRecordParser(RecordNormalizer normalizer) {
        this.normalizer = normalizer;
    }

    public List<VoterRecord> parseVoterRecords(String rawText, String sourceDocument) {
        List<VoterRecord> records = new ArrayList<>();
        Set<String> seenSignatures = new LinkedHashSet<>();
        String[] blocks = rawText.split("(?:\\r?\\n){2,}|\\f");
        int sequence = 1;

        for (String block : blocks) {
            String trimmed = block.trim();
            if (trimmed.isBlank()) {
                continue;
            }
            VoterRecord record = parseStructuredBlock(trimmed, sourceDocument, sequence);
            if (record == null) {
                List<VoterRecord> delimitedRows = parseDelimitedRows(trimmed, sourceDocument, sequence);
                for (VoterRecord rowRecord : delimitedRows) {
                    if (registerRecord(records, seenSignatures, rowRecord)) {
                        sequence++;
                    }
                }
                continue;
            }
            if (registerRecord(records, seenSignatures, record)) {
                sequence++;
            }
        }
        return records;
    }

    public List<RemovedRecord> parseRemovedRecords(String rawText, String sourceDocument) {
        return parseVoterRecords(rawText, sourceDocument).stream()
                .map(RemovedRecord::fromVoterRecord)
                .toList();
    }

    private boolean registerRecord(List<VoterRecord> records, Set<String> seenSignatures, VoterRecord record) {
        if (!hasRecordSignal(record)) {
            return false;
        }
        String signature = normalizer.normalizeIdentifier(record.voterId()) + "|" +
                normalizer.normalizeText(record.name()) + "|" +
                normalizer.normalizeText(record.address()) + "|" +
                normalizer.normalizeText(record.serialNumber());
        if (seenSignatures.add(signature)) {
            records.add(record);
            return true;
        }
        return false;
    }

    private boolean hasRecordSignal(VoterRecord record) {
        return !normalizer.normalizeIdentifier(record.voterId()).isBlank()
                || !normalizer.normalizeText(record.name()).isBlank();
    }

    private VoterRecord parseStructuredBlock(String block, String sourceDocument, int sequence) {
        String voterId = capture(ID_PATTERN, block);
        String name = capture(NAME_PATTERN, block);
        String relation = capture(RELATION_PATTERN, block);
        String ageOrDob = capture(AGE_PATTERN, block);
        String gender = capture(GENDER_PATTERN, block);
        String address = capture(ADDRESS_PATTERN, block);
        String partNumber = capture(PART_PATTERN, block);
        String serialNumber = capture(SERIAL_PATTERN, block);

        if (name == null && voterId == null) {
            return null;
        }
        return new VoterRecord(
                sourceDocument,
                sourceDocument + "#" + sequence,
                voterId,
                trim(name),
                trim(relation),
                trim(ageOrDob),
                trim(gender),
                trim(address),
                trim(partNumber),
                trim(serialNumber)
        );
    }

    private List<VoterRecord> parseDelimitedRows(String block, String sourceDocument, int sequenceStart) {
        List<VoterRecord> records = new ArrayList<>();
        String[] lines = block.split("\\R");
        int sequence = sequenceStart;
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isBlank() || !looksTabular(trimmed)) {
                continue;
            }
            String[] parts = trimmed.split("\\s*\\|\\s*|\\s{2,}|\\t|\\s*,\\s*");
            if (parts.length < 2) {
                continue;
            }
            String voterId = findLikelyId(parts);
            String name = findLikelyName(parts);
            String relation = parts.length > 2 ? parts[2] : null;
            String ageOrDob = parts.length > 3 ? parts[3] : null;
            String gender = findLikelyGender(parts);
            String address = parts.length > 5 ? parts[5] : (parts.length > 4 ? parts[4] : null);
            String partNumber = parts.length > 6 ? parts[6] : null;
            String serialNumber = parts.length > 7 ? parts[7] : null;
            records.add(new VoterRecord(
                    sourceDocument,
                    sourceDocument + "#" + sequence,
                    trim(voterId),
                    trim(name),
                    trim(relation),
                    trim(ageOrDob),
                    trim(gender),
                    trim(address),
                    trim(partNumber),
                    trim(serialNumber)
            ));
            sequence++;
        }
        return records;
    }

    private boolean looksTabular(String line) {
        return line.contains("|") || line.contains("\t") || line.matches(".*\\s{2,}.*");
    }

    private String findLikelyId(String[] parts) {
        for (String part : parts) {
            Matcher matcher = ID_PATTERN.matcher(part);
            if (matcher.find()) {
                return matcher.group(1);
            }
            if (part.matches("[A-Z]{1,5}[0-9]{4,}|[A-Z0-9]{6,}")) {
                return part;
            }
        }
        return null;
    }

    private String findLikelyName(String[] parts) {
        for (String part : parts) {
            String normalized = normalizer.normalizeText(part);
            if (!normalized.isBlank() && normalized.chars().filter(Character::isLetter).count() >= 4) {
                return part;
            }
        }
        return null;
    }

    private String findLikelyGender(String[] parts) {
        for (String part : parts) {
            String normalized = normalizer.normalizeText(part);
            if (normalized.equals("male") || normalized.equals("female") || normalized.equals("other")) {
                return part;
            }
        }
        return null;
    }

    private String capture(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
