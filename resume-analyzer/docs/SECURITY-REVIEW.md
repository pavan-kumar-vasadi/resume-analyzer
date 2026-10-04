# Security Review

Scope: code review of this project (Phase 6). It is a checklist-style review by the developer, not a penetration test. Items marked **Open** are known limitations you should mention in a viva or report.

## What is in place
| Area | Status | Where |
|---|---|---|
| Password storage | BCrypt hash, never returned by any DTO | `SecurityConfig`, `UserDto` |
| Authentication | Signed JWT (HS256), 120 min expiry, invalid/expired token means unauthenticated | `JwtService`, `JwtAuthFilter` |
| Role enforcement | `/api/admin/**` requires ROLE_ADMIN on the server; role is loaded from the database on each request, so a token cannot claim a role | `SecurityConfig`, `JwtAuthFilter` |
| Default deny | Everything not explicitly public requires login (`anyRequest().authenticated()`) | `SecurityConfig` |
| Admin creation | Public registration forces role USER; first admin only via env vars | `AuthService`, `AdminSeeder` |
| Data isolation | Resume queries include the user id (`findByIdAndUserId`); other users' resumes return 404, so existence is not revealed | `ResumeRepository`, `ResumeService` |
| Input validation | Bean Validation on all request DTOs, one error format, no stack traces in responses | DTOs, `GlobalExceptionHandler` |
| SQL injection | JPA/JPQL with bound parameters and Specifications; no string-built SQL | repositories |
| File upload | 5 MB limit, extension plus file-signature check, random UUID storage name, stored outside static folders | `FileValidator`, `ResumeService`, `application.properties` |
| Login errors | Same message for unknown email and wrong password | `AuthService` |
| Secrets | DB password, JWT secret, admin credentials from environment variables; nothing committed | `application.properties`, `.gitignore` |
| CORS | Only the configured origin(s), only needed methods and headers | `SecurityConfig` |
| CSRF | Disabled on purpose: stateless bearer-token API, no cookies | `SecurityConfig` |
| Password change | Requires current password; wrong value returns 400 (not 401) so the user is not logged out | `UserService` |
| XSS in UI | React escapes rendered text; no `dangerouslySetInnerHTML` used for user content | frontend |

## Open items and recommendations
| # | Risk | Severity | Recommendation |
|---|---|---|---|
| 1 | **No rate limiting or lockout on `/auth/login`**: passwords can be guessed by brute force | Medium | Add Bucket4j or a simple failed-attempt counter per email/IP |
| 2 | **JWT kept in `sessionStorage`**: readable by any injected script (XSS) | Medium | Accept for a student project, or move to an HttpOnly cookie plus CSRF protection |
| 3 | **Swagger UI and `/v3/api-docs` are public** | Low in development | Disable or protect them in production (`springdoc.swagger-ui.enabled=false`) |
| 4 | **No JWT revocation**: logout only clears the browser token; a stolen token works until expiry. Password change does not invalidate old tokens | Medium | Short expiry plus refresh tokens, or a `token_version` column checked by the filter |
| 5 | **Temporary random JWT key when `JWT_SECRET` is unset**: safe but all sessions end on restart | Low | Always set `JWT_SECRET` (32+ chars) outside development |
| 6 | **Plain HTTP** in development | High if deployed as is | Serve over HTTPS (reverse proxy) in any real deployment |
| 7 | **Database user is `root`** in local examples | Medium | Create a dedicated MySQL user with rights only on `resume_analyzer` |
| 8 | **Uploaded files are not virus-scanned** and are parsed by PDFBox/POI | Low-Medium | Keep libraries updated; size limit already bounds the cost |
| 9 | **No audit log** of admin actions | Low | Log job create/update/delete with admin email |
| 10 | **No email verification or password reset** | Low | Out of scope; mention as future work |
| 11 | **Admin lists show resume file names and owner names** (never resume text) | Informational | Mention in a privacy note if the app is used with real data |

## Automated coverage
`SecurityAccessTest` checks: 401 without a token on protected routes, 403 for a normal user on every admin route, 200 for an admin, rejection of tampered, malformed and deleted-user tokens, and that the role is read from the database. It runs without MySQL.

## Not verified
These tests and checks were written but not executed by the author of this document in the build environment (no Maven or MySQL there). Run `mvn test` and the manual cases in `TEST-CASES.md` on your machine and record the results.
