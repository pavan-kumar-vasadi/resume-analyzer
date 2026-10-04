package com.resumeanalyzer.util;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;

public final class AuthUtil {
    private AuthUtil() {}
    /** Email of the logged-in user, or null for anonymous visitors. */
    public static String emailOrNull(Authentication a) {
        return (a != null && a.isAuthenticated() && !(a instanceof AnonymousAuthenticationToken)) ? a.getName() : null;
    }
}
