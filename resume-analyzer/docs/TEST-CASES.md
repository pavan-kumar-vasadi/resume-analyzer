# Test Cases

Automated tests run with `cd backend; mvn test`. Manual cases are run in the browser with one admin account and one normal account. Fill the "Result" column while testing.

## A. Automated tests (JUnit)
| Test class | What it covers |
|---|---|
| `AuthServiceTest` | Register stores BCrypt hash and USER role; duplicate email; wrong password |
| `JwtServiceTest` | Token round-trip, tampered and expired tokens rejected |
| `UserServiceTest` | Wrong current password gives 400, mismatch, same-as-current, success stores new hash |
| `FileValidatorTest` | Size, extension, and file-signature checks |
| `SkillExtractorTest` | Dictionary and alias matching |
| `ResumeAnalysisServiceTest` | Section detection, scoring, no-text resumes |
| `MatchCalculatorTest` | Required/preferred weighting, no division by zero |
| `JobRequestValidationTest` | Job field validation rules |
| `AdminDashboardServiceTest` | 14-day registration buckets, empty database, totals, jobs by level |
| `SecurityAccessTest` | No token gives 401 on protected endpoints; normal user gets 403 on every admin endpoint; admin gets 200; invalid, malformed or deleted-user tokens give 401; role comes from the database, not the token; public job listing needs no login |

## B. Manual test cases
| ID | Steps | Expected | Result |
|---|---|---|---|
| TC-01 | Open landing page with backend and MySQL running | Green "connected" status | |
| TC-02 | Stop MySQL, reload landing | Amber status | |
| TC-03 | Stop backend, reload landing | Red status | |
| TC-04 | Register with valid data | Redirected to dashboard, sidebar visible | |
| TC-05 | Register with an existing email | Message "An account with this email already exists" | |
| TC-06 | Register with password `abcdefgh` (no digit) | Validation error shown | |
| TC-07 | Log in with wrong password | "Invalid email or password"; no hint which part was wrong | |
| TC-08 | Open `/dashboard` while logged out | Redirected to `/login` | |
| TC-09 | Normal user opens `/admin` | Redirected to `/dashboard` | |
| TC-10 | Call `GET /api/admin/users` with a normal user's token (Swagger or curl) | 403 | |
| TC-11 | Upload a valid text-based PDF | Resume listed, skills detected, score shown | |
| TC-12 | Upload a `.txt` file renamed to `.pdf` | Rejected: content does not match extension | |
| TC-13 | Upload a file over 5 MB | Rejected with size message | |
| TC-14 | Upload a scanned (image-only) PDF | Message that no readable text was found; no invented skills | |
| TC-15 | Log in as user B and open user A's resume URL | "Resume not found" (404) | |
| TC-16 | Delete a resume | Removed from list; toast shown; also gone from history | |
| TC-17 | Click "Find matching jobs" | Jobs ranked by match %, matched skills green, missing red | |
| TC-18 | Hand-check one job: matched required / total required x 0.8 + preferred share x 0.2 | Equals displayed % | |
| TC-19 | Save a job, open Saved Jobs, unsave | Appears then disappears | |
| TC-20 | Skill gaps page, scope top and saved | Missing skills and learning topics for each scope | |
| TC-21 | New user with no resumes opens dashboard | "No data yet" state, zeros, no errors | |
| TC-22 | Profile: change name | Toast; sidebar name updates; email field not editable | |
| TC-23 | Settings: wrong current password | "Current password is incorrect"; user stays logged in | |
| TC-24 | Settings: correct change, log out, log in with new password | Works; old password no longer works | |
| TC-25 | Jobs page filters and pagination | Results match filters; page buttons work | |
| TC-26 | Admin: create job with skills | Toast "Job created"; visible in public Jobs | |
| TC-27 | Admin: deactivate a job | Toast; job disappears from public list and its URL gives not found | |
| TC-28 | Admin: delete a job | Confirmation dialog, then toast; gone from list | |
| TC-29 | Admin dashboard numbers vs Users page and Manage jobs | Totals match | |
| TC-30 | Register a new user, reload admin dashboard | Today's registrations +1; user at top of recent registrations | |
| TC-31 | Admin dashboard jobs-by-level chart vs Manage jobs levels | Counts match | |
| TC-32 | Shrink browser to phone width | Hamburger menu opens and closes sidebar | |
| TC-33 | Let token expire (or clear it) and click a page | Redirected to login with expired notice | |
