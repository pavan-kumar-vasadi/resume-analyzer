package com.resumeanalyzer;

import static org.junit.jupiter.api.Assertions.*;

import com.resumeanalyzer.exception.ApiException;
import com.resumeanalyzer.util.FileValidator;
import org.junit.jupiter.api.Test;

class FileValidatorTest {
    byte[] pdf = "%PDF-1.7 data".getBytes();
    byte[] zip = {'P', 'K', 3, 4, 0, 0};

    @Test void acceptsPdfAndDocx() {
        assertEquals("pdf", FileValidator.validate("cv.PDF", pdf));
        assertEquals("docx", FileValidator.validate("cv.docx", zip));
    }
    @Test void rejectsWrongExtension() { assertThrows(ApiException.class, () -> FileValidator.validate("virus.exe", pdf)); }
    @Test void rejectsContentMismatch() { assertThrows(ApiException.class, () -> FileValidator.validate("fake.pdf", "hello world".getBytes())); }
    @Test void rejectsEmpty() { assertThrows(ApiException.class, () -> FileValidator.validate("cv.pdf", new byte[0])); }
    @Test void rejectsOver5MB() { assertThrows(ApiException.class, () -> FileValidator.validate("cv.pdf", new byte[5 * 1024 * 1024 + 1])); }
}
