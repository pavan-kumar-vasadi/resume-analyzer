# Viva Questions and Answers

Answers are short on purpose: say them in your own words. Where the project has a limitation, say so; examiners respect honesty more than overclaiming.

## Project basics
**1. What does the project do?**
Students upload a resume (PDF or DOCX). The system extracts the text, detects skills, gives a completeness score with tips, matches the resume against jobs added by an admin, and shows which skills are missing for those jobs.

**2. Which technologies did you use and why?**
Spring Boot for a structured REST backend with built-in security and validation, MySQL because the data is relational (users, resumes, jobs, skills), React for a component-based UI, Tailwind for quick responsive styling, Recharts for charts, PDFBox and Apache POI to read PDF and Word files.

**3. Explain the architecture.**
Three tiers: React frontend, Spring Boot REST backend, MySQL database. The backend is layered: controller (HTTP), service (business rules), repository (database), entity (tables), dto (what is sent to the client).

**4. Why DTOs instead of returning entities?**
So that sensitive fields (password hash) are never exposed, lazy-loaded relations do not cause errors during JSON conversion, and the API stays stable if tables change.

## Authentication and security
**5. How does login work?**
The server checks the password against the BCrypt hash. If correct it creates a JWT (subject = email, 120 minutes expiry, signed with a secret). The frontend sends it as `Authorization: Bearer ...` on every request.

**6. What is BCrypt and why not plain hashing like SHA-256?**
BCrypt is deliberately slow and adds a random salt per password, so brute-forcing and rainbow tables are much harder. SHA-256 is fast, which helps attackers.

**7. What is a JWT and how is it verified?**
A signed token with three parts: header, payload (claims), signature. The server recomputes the signature with its secret; if the token was changed or expired, verification fails and the request is treated as not logged in.

**8. How do you stop a normal user from using admin APIs?**
`SecurityConfig` requires `ROLE_ADMIN` for `/api/admin/**`. The JWT filter loads the role from the database on every request, so even a modified token cannot grant admin. The React redirect for non-admins is only for convenience. The automated `SecurityAccessTest` checks 401 and 403 cases.

**9. 401 versus 403?**
401: not authenticated (no or bad token). 403: authenticated but not allowed.

**10. Why is CSRF disabled?**
CSRF attacks rely on browsers automatically sending cookies. This API uses a bearer token in a header, with no cookies, so that attack does not apply. If I moved the token to a cookie I would have to enable CSRF protection.

**11. Where is the token stored and what is the risk?**
In `sessionStorage`; it clears when the tab closes. Risk: injected JavaScript (XSS) could read it. HttpOnly cookies protect against that but need CSRF handling. React escaping reduces XSS risk.

**12. How do you prevent SQL injection?**
JPA repositories and Specifications use bound parameters; I never build SQL strings from user input.

**13. How do you keep one user from reading another's resume?**
Queries include the user id (`findByIdAndUserId`). Another user's id returns 404, which also hides that the resume exists.

**14. How is file upload made safe?**
Size limit 5 MB, extension check, file-signature check (PDF starts with `%PDF`, DOCX is a ZIP), random UUID file name, stored outside public folders.

**15. What security weaknesses remain?**
No login rate limiting, no token revocation, Swagger is public in development, HTTP not HTTPS locally, root DB user in examples. They are listed in `SECURITY-REVIEW.md` with fixes.

## Resume analysis and matching
**16. How are skills detected?**
A dictionary of 41 skills with aliases (for example "js" for JavaScript) is matched against the text using word boundaries. It is not machine learning, so skills outside the dictionary are not found.

**17. How is the completeness score calculated?**
Eight checks with configurable weights: contact 15, summary 10, education 15, skills 20, projects 15, experience 10, certifications 5, readability 10. Each is scored from section headings and patterns in the text. It measures completeness of the document, not employability or any ATS score.

**18. How is the job match score calculated?**
`0.8 x (matched required skills / required skills) + 0.2 x (matched preferred / preferred)`. If a job has no preferred skills only required counts, and the reverse; if it has neither the score is 0, so there is no division by zero. It estimates skill overlap, not the chance of being hired.

**19. What happens with a scanned PDF?**
No text can be extracted and OCR is not configured, so the system says so instead of inventing results.

**20. How are skill gaps and learning topics produced?**
Missing required and preferred skills are counted across the top matching jobs or saved jobs; topics are a static, rule-based mapping, not AI generated.

## Database
**21. Explain the main tables and relations.**
Users own resumes and saved jobs. Resumes and skills are many-to-many through `resume_skills`; jobs and skills through `job_skills` (with an `is_required` flag). `recommendations` stores each resume-job score. See `ER-DIAGRAM.md`.

**22. Why composite primary keys on junction tables?**
They guarantee a pair (resume, skill) cannot be stored twice, without an extra id column.

**23. What does `ON DELETE CASCADE` do here?**
Deleting a user removes their resumes, saved jobs and recommendations; deleting a job removes its links and recommendations. No orphan rows remain.

**24. What is the LazyInitializationException you hit?**
Job skills are loaded lazily. A `clearAutomatically` option on the delete query detached already-loaded jobs, so reading their skills later failed with no session. Removing that option fixed it.

## Frontend
**25. How does the frontend protect pages?**
`ProtectedRoute` redirects logged-out users to login and non-admins away from admin pages. This is a convenience; real enforcement is on the server.

**26. How are charts fed with data?**
The dashboard endpoints return aggregated numbers from the database; Recharts renders them. With no data the page shows an empty state instead of fake numbers.

**27. What happens when the token expires?**
The axios interceptor sees a 401 on a logged-in session, clears the token and redirects to login.

## Testing and wrap-up
**28. What did you test?**
Unit tests for services (auth, JWT, validation, scoring, matching, admin statistics) and a security test that checks 401 and 403 rules without a database. Manual test cases are in `TEST-CASES.md`.

**29. Limitations and future work?**
OCR, ML-based skill extraction, real job feeds, email verification and password reset, refresh tokens, rate limiting, audit log, HTTPS deployment, Docker.

**30. What would you do differently?**
Add integration tests with a test database, role-based admin actions on users (block, delete), and store a token version to allow revoking sessions.
