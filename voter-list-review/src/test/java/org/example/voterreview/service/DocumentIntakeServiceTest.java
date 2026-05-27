package org.example.voterreview.service;

import org.example.voterreview.model.VoterRecord;
import org.example.voterreview.util.RecordNormalizer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentIntakeServiceTest {

    @Test
    void parsesTextPdfWithoutOcr(@TempDir Path tempDir) throws IOException {
        byte[] pdf = PdfFixtures.textPdf("Name: Sita Devi\nEPIC: EPIC21\nAge: 48\nAddress: River Road");
        DocumentIntakeService service = new DocumentIntakeService(new FakeOcrService(""), new VoterRecordParser(new RecordNormalizer()));

        MockMultipartFile file = new MockMultipartFile("voterListFile", "voters.pdf", "application/pdf", pdf);
        ParsedDocument<VoterRecord> parsed = service.parseVoters(file, tempDir);

        assertThat(parsed.records()).hasSize(1);
        assertThat(parsed.records().getFirst().getVoterId()).isEqualTo("EPIC21");
    }

    @Test
    void fallsBackToOcrWhenPdfHasNoExtractableText(@TempDir Path tempDir) throws IOException {
        byte[] pdf = PdfFixtures.imageOnlyPdf();
        DocumentIntakeService service = new DocumentIntakeService(
                new FakeOcrService("Name: Mohan Lal\nEPIC: OCR9999\nAge: 72\nAddress: Lake View"),
                new VoterRecordParser(new RecordNormalizer())
        );

        MockMultipartFile file = new MockMultipartFile("voterListFile", "scan.pdf", "application/pdf", pdf);
        ParsedDocument<VoterRecord> parsed = service.parseVoters(file, tempDir);

        assertThat(parsed.records()).hasSize(1);
        assertThat(parsed.records().getFirst().getVoterId()).isEqualTo("OCR9999");
        assertThat(parsed.warnings()).anyMatch(warning -> warning.contains("OCR"));
    }

    private static final class FakeOcrService extends OcrService {
        private final String text;

        private FakeOcrService(String text) {
            this.text = text;
        }

        @Override
        public ParsedDocument<String> readTextFromImages(List<BufferedImage> pages) {
            return new ParsedDocument<>(List.of(text), List.of(), true);
        }
    }

    private static final class PdfFixtures {
        private static byte[] textPdf(String text) throws IOException {
            try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                PDPage page = new PDPage(PDRectangle.A4);
                document.addPage(page);
                try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
                    stream.beginText();
                    stream.setLeading(16);
                    stream.newLineAtOffset(60, 760);
                    stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                    for (String line : text.split("\\R")) {
                        stream.showText(line);
                        stream.newLine();
                    }
                    stream.endText();
                }
                document.save(output);
                return output.toByteArray();
            }
        }

        private static byte[] imageOnlyPdf() throws IOException {
            try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                PDPage page = new PDPage(PDRectangle.A4);
                document.addPage(page);
                BufferedImage image = new BufferedImage(200, 80, BufferedImage.TYPE_INT_RGB);
                var pdImage = LosslessFactory.createFromImage(document, image);
                try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
                    stream.drawImage(pdImage, 40, 640, 200, 80);
                }
                document.save(output);
                return output.toByteArray();
            }
        }
    }
}
