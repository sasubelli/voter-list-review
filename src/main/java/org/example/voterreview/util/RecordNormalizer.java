package org.example.voterreview.util;

import org.apache.commons.codec.language.DoubleMetaphone;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

@Component
public class RecordNormalizer {
    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-z0-9 ]");
    private static final Pattern MULTI_SPACE = Pattern.compile("\\s+");

    private final DoubleMetaphone doubleMetaphone = new DoubleMetaphone();

    public String normalizeText(String input) {
        if (input == null) {
            return "";
        }
        String lowered = input.toLowerCase(Locale.ROOT).trim();
        lowered = NON_ALPHANUMERIC.matcher(lowered).replaceAll(" ");
        return MULTI_SPACE.matcher(lowered).replaceAll(" ").trim();
    }

    public String normalizeIdentifier(String input) {
        return normalizeText(input).replace(" ", "");
    }

    public String normalizeAgeOrDob(String input) {
        return normalizeText(input).replace("years", "").replace("year", "").trim();
    }

    public boolean exactMatch(String left, String right) {
        return !normalizeText(left).isBlank() && Objects.equals(normalizeText(left), normalizeText(right));
    }

    public boolean exactIdentifierMatch(String left, String right) {
        return !normalizeIdentifier(left).isBlank() && Objects.equals(normalizeIdentifier(left), normalizeIdentifier(right));
    }

    public String phoneticCode(String input) {
        String normalized = normalizeText(input);
        return normalized.isBlank() ? "" : doubleMetaphone.doubleMetaphone(normalized);
    }

    public boolean phoneticMatch(String left, String right) {
        String leftCode = phoneticCode(left);
        String rightCode = phoneticCode(right);
        return !leftCode.isBlank() && leftCode.equals(rightCode);
    }

    public double similarity(String left, String right) {
        String normalizedLeft = normalizeText(left);
        String normalizedRight = normalizeText(right);
        if (normalizedLeft.isBlank() || normalizedRight.isBlank()) {
            return 0.0;
        }
        if (normalizedLeft.equals(normalizedRight)) {
            return 1.0;
        }
        if (phoneticMatch(left, right)) {
            return 0.8;
        }
        int commonPrefix = 0;
        int max = Math.min(normalizedLeft.length(), normalizedRight.length());
        while (commonPrefix < max && normalizedLeft.charAt(commonPrefix) == normalizedRight.charAt(commonPrefix)) {
            commonPrefix++;
        }
        return (double) commonPrefix / Math.max(normalizedLeft.length(), normalizedRight.length());
    }
}
