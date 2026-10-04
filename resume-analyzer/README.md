# AI-Based Resume Analyzer & Job Recommendation System

Final-year B.Tech CSE project: React + Spring Boot + MySQL.
Skill extraction and job matching are **rule-based** (dictionary + overlap scoring). No generative AI or trained ML model is used.

## Status: Phase 1 complete
Project structure, full MySQL schema, Spring Boot with `/api/health`, security/CORS skeleton, React landing page wired to the backend.
All phases (1 to 6) are implemented. Documentation is in the `docs/` folder.

## Requirements
Java 21, Maven 3.9+, Node 18+, MySQL 8.

## 1. Database
```bash
mysql -u root -p < database/schema.sql
mysql -u root -p < database/sample-data.sql
```

## 2. Backend (http://localhost:8080)
```bash
cd backend
export DB_USERNAME=root
export DB_PASSWORD=your_mysql_password      # Windows PowerShell: $env:DB_PASSWORD="..."
mvn spring-boot:run
```
Check: http://localhost:8080/api/health -> `{"status":"UP","database":"UP",...}`
Swagger UI: http://localhost:8080/swagger-ui.html

## 3. Frontend (http://localhost:5173)
```bash
cd frontend
npm install
npm run dev
```
`frontend/.env` holds `VITE_API_BASE_URL` (copy from `.env.example`).

## Manual test checklist (Phase 1)
- [ ] `/api/health` returns UP with database UP
- [ ] Landing page shows green "Backend and database connected"
- [ ] Stop MySQL: status turns amber; stop backend: red
- [ ] Every nav/button navigates (Login/Register/Jobs show a labeled "coming in Phase N" page; `/xyz` shows 404)
- [ ] Resize to mobile width; layout stays usable

## Troubleshooting
- **CORS error**: backend `CORS_ORIGINS` must include `http://localhost:5173`.
- **Access denied for user**: set `DB_USERNAME` / `DB_PASSWORD`.
- **Port 8080 busy**: set `server.port` or free the port.

## Phase 2: Authentication (added)
Register/login with BCrypt + JWT, protected routes, admin-only `/api/admin/**` (enforced server-side).

Backend env vars (PowerShell):
```powershell
$env:DB_USERNAME="root"; $env:DB_PASSWORD="..."
$env:JWT_SECRET="at-least-32-random-characters-here-123456"   # optional in dev (random key is used otherwise)
$env:ADMIN_EMAIL="admin@example.com"; $env:ADMIN_PASSWORD="Admin12345"   # creates the first admin at startup
```
Run tests: `cd backend; mvn test`
Token storage: `sessionStorage` (cleared on tab close). Limitation: readable by injected scripts (XSS); HttpOnly cookies are safer but need CSRF handling.

Manual checklist: register -> dashboard; wrong password -> "Invalid email or password"; duplicate email -> 409 message; `/dashboard` while logged out -> /login; normal user at `/admin` -> redirected, and `GET /api/admin/users` with a user token -> 403; admin login -> /admin shows users.

## Phase 3: Resume upload and analysis (added)
- `POST /api/resumes/upload` (multipart `file`), `GET /api/resumes/my`, `GET /api/resumes/{id}`, `GET /api/resumes/{id}/analysis`, `GET /api/resumes/{id}/skills`, `DELETE /api/resumes/{id}`, `GET /api/skills`
- PDF via PDFBox, DOCX via Apache POI. Validation: extension + file signature + 5 MB limit. Files stored under `app.storage.dir` (default `backend/uploads`, not publicly served) with random UUID names.
- Skill detection is dictionary + alias matching (not ML). The score is a configurable completeness heuristic (`app.scoring.*`), not an ATS score.
- Scanned PDFs: OCR is not configured, so a clear message is shown instead of invented content.
- Other users' resumes return 404.

## Phase 4A: Jobs (added)
- Public: `GET /api/jobs` (alias `/api/jobs/search`; filters q, location, experienceLevel, employmentType, skill; page, size), `GET /api/jobs/{id}`, `GET /api/jobs/{id}/skills`, `GET /api/skills`
- Logged-in: `POST/GET/DELETE /api/saved-jobs`
- Admin only: `GET/POST /api/admin/jobs`, `GET/PUT/DELETE /api/admin/jobs/{id}`, `PATCH /api/admin/jobs/{id}/active?value=`
- Optional extra demo data: `database/more-demo-jobs.sql` (labeled DEMO, fictional)

## Phase 4B: Matching and skill gaps (added)
- `POST /api/recommendations/analyze/{resumeId}`, `GET /api/recommendations/{resumeId}`, `GET /api/recommendations/{resumeId}/skill-gaps?scope=top|saved`
- Required coverage = matched required / total required x 100. Combined score = 0.8 x required + 0.2 x preferred (`app.recommendation.required-weight`).
  No preferred skills -> required only. No required skills -> preferred only. Neither -> 0 (no division by zero).
- The score is a skill-overlap estimate, not a probability of being hired. Learning topics are static, rule-based suggestions.
- Re-analyzing replaces that resume's previous recommendation rows.

## Phase 5A: Student dashboard (added)
- `GET /api/dashboard`, `GET /api/history`, `PUT /api/users/me` (full name), `PUT /api/users/me/password`
- Sidebar layout for student pages; dashboard figures come from the signed-in user's own records (no hardcoded values).
- Email cannot be changed from the profile page; password change verifies the current password (wrong input returns 400, not 401).

## Phase 5B: Admin dashboard (added)
- `GET /api/admin/dashboard` (admin only, enforced by the `/api/admin/**` rule): totals (users, admins, resumes, active/total jobs, resumes matched to jobs), registrations per day (last 14 days), top 8 skills across resumes, resumes by status, jobs by experience level (Entry/Mid/Senior), 5 most recent users, resumes and job analyses.
- Frontend: `/admin` is now the admin dashboard; the user list moved to `/admin/users`. Admin pages use the same sidebar layout as student pages.
- All figures are read from the database. Registrations per day are grouped in Java from the last 14 days of `users.created_at`, which is fine at student-project scale.
- Admins can see resume file names and owner names in the "recent" lists; resume text and contents are never returned.
- Toast notifications on admin job actions (create, update, activate/deactivate, delete) and on errors.
- Tests: `AdminDashboardServiceTest` (day buckets, empty database, totals, jobs by level).

## Phase 6: Tests and documentation (added)
- New test: `SecurityAccessTest` (no token gives 401, normal user gets 403 on all admin routes, admin gets 200, bad/tampered/deleted-user tokens rejected, role read from the database). It uses mocks and needs no MySQL.
- Run everything: `cd backend; mvn test`
- Docs in `docs/`:
  - `ER-DIAGRAM.md`: Mermaid ER diagram and design notes
  - `API.md`: endpoints, roles, error format, scoring formulas
  - `SRS.md`: requirements specification
  - `TEST-CASES.md`: automated test list and 33 manual test cases with a Result column
  - `SECURITY-REVIEW.md`: what is protected, 11 open items with fixes
  - `VIVA-QA.md`: 30 viva questions with short answers

## Known limitations
No OCR for scanned resumes; skill detection is dictionary-based; no login rate limiting; no token revocation; token stored in `sessionStorage`; Swagger is public in development. See `docs/SECURITY-REVIEW.md`.
