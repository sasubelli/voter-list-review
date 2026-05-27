package org.example.voterreview.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.example.voterreview.model.RemovedRecord;
import org.example.voterreview.model.VoterRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class DocumentIntakeService {
    private final OcrService ocrService;
    private final VoterRecordParser parser;

    public DocumentIntakeService(OcrService ocrService, VoterRecordParser parser) {
        this.ocrService = ocrService;
        this.parser = parser;
    }

    public ParsedDocument<VoterRecord> parseVoters(MultipartFile file, Path workingDirectory) throws IOException {
        String sourceName = file.getOriginalFilename() == null ? "voter-list" : file.getOriginalFilename();
        ExtractionResult extractionResult = extractDocument(file, workingDirectory, sourceName);
        List<String> warnings = new ArrayList<>(extractionResult.warnings());
        List<VoterRecord> records = parser.parseVoterRecords(extractionResult.text(), sourceName);
        if (records.isEmpty()) {
            warnings.add("No voter records were parsed from " + sourceName + ". Review the file layout or OCR quality.");
        }
        return new ParsedDocument<>(records, warnings, extractionResult.ocrApplied());
    }

    public ParsedDocument<RemovedRecord> parseRemoved(MultipartFile file, Path workingDirectory) throws IOException {
        String sourceName = file.getOriginalFilename() == null ? "removed-list" : file.getOriginalFilename();
        ExtractionResult extractionResult = extractDocument(file, workingDirectory, sourceName);
        List<String> warnings = new ArrayList<>(extractionResult.warnings());
        List<RemovedRecord> records = parser.parseRemovedRecords(extractionResult.text(), sourceName);
        if (records.isEmpty()) {
            warnings.add("No removed-list records were parsed from " + sourceName + ".");
        }
        return new ParsedDocument<>(records, warnings, extractionResult.ocrApplied());
    }

    private ExtractionResult extractDocument(MultipartFile file, Path workingDirectory, String sourceName) throws IOException {
        Path tempFile = workingDirectory.resolve(sourceName.replaceAll("[^A-Za-z0-9._-]", "_"));
        file.transferTo(tempFile);
        if (!"pdf".equals(extensionOf(sourceName))) {
            return new ExtractionResult(Files.readString(tempFile, StandardCharsets.UTF_8), List.of(), false);
        }

        try (PDDocument document = Loader.loadPDF(tempFile.toFile())) {
            String text = new PDFTextStripper().getText(document);
            if (text != null && text.replaceAll("\\s+", "").length() >= 40) {
                return new ExtractionResult(text, List.of(), false);
            }

            PDFRenderer renderer = new PDFRenderer(document);
            List<BufferedImage> pages = new ArrayList<>();
            for (int i = 0; i < document.getNumberOfPages(); i++) {
                pages.add(renderer.renderImageWithDPI(i, 200));
            }
            ParsedDocument<String> ocrText = ocrService.readTextFromImages(pages);
            String extractedText = ocrText.records().isEmpty() ? "" : ocrText.records().getFirst();
            List<String> warnings = new ArrayList<>(ocrText.warnings());
            if (ocrText.ocrApplied()) {
                warnings.add("OCR was applied to scanned pages in " + sourceName + ".");
            } else {
                warnings.add("PDF text extraction was weak for " + sourceName + ". Results may be incomplete.");
            }
            return new ExtractionResult(extractedText, warnings, ocrText.ocrApplied());
        }
    }

    private String extensionOf(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0) {
            return "";
        }
        return filename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }

    private record ExtractionResult(String text, List<String> warnings, boolean ocrApplied) {
    }
}
