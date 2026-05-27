package org.example.voterreview.service;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

@Service
public class OcrService {

    public ParsedDocument<String> readTextFromImages(List<BufferedImage> pages) {
        List<String> warnings = new ArrayList<>();
        StringBuilder text = new StringBuilder();
        boolean applied = false;
        try {
            ITesseract tesseract = new Tesseract();
            for (BufferedImage page : pages) {
                text.append(tesseract.doOCR(page)).append(System.lineSeparator());
            }
            applied = true;
        } catch (UnsatisfiedLinkError | NoClassDefFoundError ex) {
            warnings.add("OCR libraries are not available in this environment, so image-only PDF pages were skipped.");
        } catch (TesseractException ex) {
            warnings.add("OCR could not read one or more scanned pages: " + ex.getMessage());
        }
        return new ParsedDocument<>(List.of(text.toString()), warnings, applied);
    }
}
