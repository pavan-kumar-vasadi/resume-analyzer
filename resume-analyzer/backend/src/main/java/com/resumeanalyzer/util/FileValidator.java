package com.resumeanalyzer.util;

import com.resumeanalyzer.exception.ApiException;
import org.springframework.http.HttpStatus;

public final class FileValidator {
    public static final long MAX_BYTES = 5L * 1024 * 1024;
    private FileValidator() {}

    /** Validates size, extension and file signature. Returns "pdf" or "docx". */
    public static String validate(String originalName, byte[] data) {
        if (data == null || data.length == 0) throw bad("The file is empty");
        if (data.length > MAX_BYTES) throw new ApiException(HttpStatus.PAYLOAD_TOO_LARGE, "File is larger than 5 MB");
        String n = originalName == null ? "" : originalName.toLowerCase();
        boolean pdfExt = n.endsWith(".pdf"), docxExt = n.endsWith(".docx");
        if (!pdfExt && !docxExt) throw bad("Only PDF and DOCX files are supported");
        boolean pdfMagic = data.length > 4 && data[0] == '%' && data[1] == 'P' && data[2] == 'D' && data[3] == 'F';
        boolean zipMagic = data.length > 4 && data[0] == 'P' && data[1] == 'K'; // DOCX is a ZIP container
        if ((pdfExt && !pdfMagic) || (docxExt && !zipMagic)) throw bad("File content does not match its extension");
        return pdfExt ? "pdf" : "docx";
    }

    private static ApiException bad(String m) { return new ApiException(HttpStatus.BAD_REQUEST, m); }
}
