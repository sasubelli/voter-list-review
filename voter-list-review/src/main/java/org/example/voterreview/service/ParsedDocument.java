package org.example.voterreview.service;

import java.util.List;

public record ParsedDocument<T>(List<T> records, List<String> warnings, boolean ocrApplied) {
}
