# Software Requirements Specification: Resume Analyzer and Job Recommender

## 1. Introduction
**Purpose.** A web application where students upload a resume, see which skills it contains and how complete it is, and get job suggestions with the skills they are missing. Admins manage job postings and watch platform statistics.

**Scope.** Student-project scale, single server, MySQL database. Not a hiring system: scores describe document completeness and skill overlap only.

**Definitions.** *Completeness score*: heuristic 0 to 100 rating of which resume sections are present. *Match score*: weighted share of a job's required and preferred skills found in a resume. *DEMO job*: sample job labeled `[DEMO]`, fictional.

**Technology.** Java 21, Spring Boot 3.3, Spring Security with JWT, Spring Data JPA, MySQL 8, React 18, Vite, Tailwind CSS, Recharts, Apache PDFBox and POI.

## 2. Overall description
**Users.**
| Role | Description |
|---|---|
| Visitor | Not logged in. Can view landing page, register, log in, browse active jobs |
| Student (USER) | Registered user. Owns resumes, recommendations, saved jobs |
| Admin (ADMIN) | Manages jobs, views users and platform statistics. First admin is created from environment variables |

**Operating environment.** Modern browser; backend on port 8080, frontend on 5173; MySQL reachable by the backend.

**Constraints and assumptions.** Text-based PDF or DOCX only (no OCR). Skill detection uses a fixed dictionary with aliases (no machine learning). Job data is entered by the admin; no external job APIs.

## 3. Functional requirements
| ID | Requirement |
|---|---|
| FR-1 | A visitor can register with name, email and password (min 8 chars, letter and digit). Duplicate email is rejected |
| FR-2 | A user can log in and receives a time-limited JWT; wrong credentials give a generic error |
| FR-3 | Public registration always creates a USER; admins are never created through the API |
| FR-4 | A user can view and rename their profile and change their password after entering the current one |
| FR-5 | A user can upload a PDF or DOCX up to 5 MB; type is checked by extension and file signature |
| FR-6 | The system extracts text and detects skills from the dictionary |
| FR-7 | The system shows a completeness score with per-section breakdown, missing sections and improvement tips |
| FR-8 | If a file has no readable text, the system says so and does not invent content |
| FR-9 | A user can list, open and delete only their own resumes |
| FR-10 | Anyone can browse and filter active jobs (keyword, location, level, type, skill) with pagination |
| FR-11 | A user can run job matching for a resume and see jobs ranked by match score with matched and missing skills |
| FR-12 | A user can see skill gaps across top matching jobs or saved jobs, with learning topics |
| FR-13 | A user can save and unsave jobs and list saved jobs |
| FR-14 | A user dashboard shows resume count, latest score, skill count, recommended jobs, charts and recent activity from the user's own data |
| FR-15 | A user can view analysis history per resume |
| FR-16 | An admin can create, edit, activate, deactivate and delete jobs and assign required and preferred skills |
| FR-17 | An admin can list all users (without password data) |
| FR-18 | An admin dashboard shows totals, registrations per day, common skills, resumes by status, jobs by level, and recent users, resumes and analyses |
| FR-19 | Actions show success or error toast notifications |

## 4. Non-functional requirements
| ID | Requirement |
|---|---|
| NFR-1 Security | Passwords hashed with BCrypt; JWT-signed sessions; role checked on the server for every `/api/admin/**` call; role read from the database on each request |
| NFR-2 Privacy | Users can never read another user's resumes; secrets come from environment variables; uploaded files are stored outside public folders under random names |
| NFR-3 Validation | All request bodies validated server-side; errors have one JSON shape |
| NFR-4 Usability | Responsive layout (sidebar collapses to a menu on mobile); loading, empty and error states on every data page |
| NFR-5 Reliability | Stopping the database or backend is reflected in the landing page status indicator |
| NFR-6 Maintainability | Layered backend (controller, service, repository, entity, dto); weights and thresholds configurable in `application.properties` |
| NFR-7 Honesty | Scores are labeled as completeness or skill-overlap estimates, not hiring probability or ATS score |

## 5. External interfaces
REST/JSON over HTTP (see `API.md`), multipart upload for resumes, MySQL via JDBC. No third-party services.

## 6. Out of scope (known limitations)
OCR for scanned resumes; email verification and password reset; refresh tokens and logout-everywhere; rate limiting; real job feeds; ML-based skill extraction; audit logging.
