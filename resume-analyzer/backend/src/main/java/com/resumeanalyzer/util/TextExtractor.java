package com.resumeanalyzer.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

public final class TextExtractor {
    private TextExtractor() {}

    public static String extract(String ext, byte[] data) throws IOException {
        String text;
        if ("pdf".equals(ext)) {
            try (PDDocument d = Loader.loadPDF(data)) { text = new PDFTextStripper().getText(d); }
        } else {
            try (XWPFDocument d = new XWPFDocument(new ByteArrayInputStream(data));
                 XWPFWordExtractor x = new XWPFWordExtractor(d)) { text = x.getText(); }
        }
        return text == null ? "" : text.replace("\u0000", "").trim();
    }
}
