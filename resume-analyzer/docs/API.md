# API Documentation

Base URL: `http://localhost:8080/api`. Interactive docs: `http://localhost:8080/swagger-ui.html`.
Authentication: `Authorization: Bearer <JWT>` (token comes from register/login, valid 120 minutes by default).

Errors use one shape: `{"success": false, "message": "...", "errors": {"field": "message"}}` (`errors` only for validation failures).

| Status | Meaning |
|---|---|
| 400 | Validation failed, malformed body, wrong current password, unreadable file |
| 401 | Missing, invalid or expired token; wrong login credentials |
| 403 | Logged in but not allowed (non-admin on `/api/admin/**`) |
| 404 | Not found, or belongs to another user |
| 409 | Email already registered |
| 413 | Upload larger than 5 MB |

## Public (no token)
| Method | Path | Description |
|---|---|---|
| GET | `/health` | App status and database status |
| POST | `/auth/register` | Body: `fullName, email, password, confirmPassword`. Returns token and user (role is always USER) |
| POST | `/auth/login` | Body: `email, password`. Returns token and user |
| GET | `/jobs` (alias `/jobs/search`) | Active jobs. Query: `q, location, experienceLevel, employmentType, skill, page, size` |
| GET | `/jobs/{id}` | One active job (inactive jobs return 404) |
| GET | `/jobs/{id}/skills` | Required and preferred skills of a job |
| GET | `/skills` | Skill dictionary |

Password rule: 8 to 72 characters, at least one letter and one digit.

## Logged-in user
| Method | Path | Description |
|---|---|---|
| GET | `/auth/me` | Current user |
| PUT | `/users/me` | Update full name (email cannot be changed) |
| PUT | `/users/me/password` | Body: `currentPassword, newPassword, confirmPassword`. 204 on success |
| POST | `/resumes/upload` | Multipart field `file` (PDF or DOCX, max 5 MB). 201 |
| GET | `/resumes/my` | Your resumes |
| GET | `/resumes/{id}` | One of your resumes |
| GET | `/resumes/{id}/analysis` | Completeness score, section scores, detected details, tips |
| GET | `/resumes/{id}/skills` | Skills detected in the resume |
| DELETE | `/resumes/{id}` | Delete resume and its file. 204 |
| POST | `/recommendations/analyze/{resumeId}` | Match the resume against all active jobs and store results |
| GET | `/recommendations/{resumeId}` | Stored matches, best first |
| GET | `/recommendations/{resumeId}/skill-gaps?scope=top\|saved` | Missing skills across top matches or saved jobs, with learning topics |
| POST | `/saved-jobs/{jobId}` | Save a job. 201 |
| GET | `/saved-jobs` | Your saved jobs |
| DELETE | `/saved-jobs/{jobId}` | Unsave. 204 |
| GET | `/dashboard` | Your totals, charts data, recent activity |
| GET | `/history` | Per-resume analysis history |

Another user's resume id always returns 404 (ownership is part of the database query).

## Admin only (`ROLE_ADMIN`, otherwise 403)
| Method | Path | Description |
|---|---|---|
| GET | `/admin/dashboard` | Totals, registrations per day (14 days), top skills, resumes by status, jobs by level, recent users/resumes/analyses |
| GET | `/admin/users` | All users (no password hashes) |
| GET | `/admin/jobs` | All jobs including inactive. Same filters as `/jobs` |
| GET | `/admin/jobs/{id}` | One job |
| POST | `/admin/jobs` | Create. Required: `title, companyName, description, employmentType (Full-time/Part-time/Internship/Contract), experienceLevel (Entry/Mid/Senior)`. Optional: location, minExperienceYears, salaryRange, applicationUrl, requiredSkillIds, preferredSkillIds, active. 201 |
| PUT | `/admin/jobs/{id}` | Update (same body) |
| PATCH | `/admin/jobs/{id}/active?value=true\|false` | Activate or deactivate |
| DELETE | `/admin/jobs/{id}` | Delete. 204 |

## Scoring formulas
- Completeness score: sum of 8 weighted checks (contact 15, summary 10, education 15, skills 20, projects 15, experience 10, certifications 5, readability 10). Weights are configurable via `app.scoring.*`.
- Job match: `0.8 x required-skill coverage + 0.2 x preferred-skill coverage` (configurable). It is a skill-overlap estimate, not a probability of being hired.
