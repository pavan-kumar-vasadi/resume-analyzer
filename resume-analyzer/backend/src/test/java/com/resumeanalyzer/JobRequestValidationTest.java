package com.resumeanalyzer;

import static org.junit.jupiter.api.Assertions.*;

import com.resumeanalyzer.dto.JobRequest;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.List;
import org.junit.jupiter.api.Test;

class JobRequestValidationTest {
    Validator v = Validation.buildDefaultValidatorFactory().getValidator();
    JobRequest make(String title, String type, String url, Integer exp) {
        return new JobRequest(title, "Acme", "Desc", "Remote", type, "Entry", exp, null, url, List.of(1L), List.of(), true);
    }

    @Test void validJobPasses() { assertTrue(v.validate(make("Dev", "Full-time", "https://x.com/apply", 0)).isEmpty()); }
    @Test void blankTitleFails() { assertFalse(v.validate(make(" ", "Full-time", null, 0)).isEmpty()); }
    @Test void unknownEmploymentTypeFails() { assertFalse(v.validate(make("Dev", "Casual", null, 0)).isEmpty()); }
    @Test void badUrlFails() { assertFalse(v.validate(make("Dev", "Full-time", "javascript:alert(1)", 0)).isEmpty()); }
    @Test void negativeExperienceFails() { assertFalse(v.validate(make("Dev", "Full-time", null, -1)).isEmpty()); }
}
