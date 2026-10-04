# ER Diagram

Source of truth: `database/schema.sql`. To get an image for a report, paste the Mermaid block into https://mermaid.live and export PNG/SVG. GitHub and VS Code (Markdown Preview Mermaid Support) also render it directly.

```mermaid
erDiagram
    USERS ||--o{ RESUMES : uploads
    USERS ||--o{ SAVED_JOBS : saves
    JOBS  ||--o{ SAVED_JOBS : "is saved in"
    RESUMES ||--o{ RESUME_SKILLS : contains
    SKILLS  ||--o{ RESUME_SKILLS : "detected in"
    JOBS    ||--o{ JOB_SKILLS : requires
    SKILLS  ||--o{ JOB_SKILLS : "needed by"
    RESUMES ||--o{ RECOMMENDATIONS : "is matched in"
    JOBS    ||--o{ RECOMMENDATIONS : "is scored in"

    USERS {
        bigint id PK
        varchar full_name
        varchar email UK
        varchar password_hash "BCrypt"
        enum role "USER, ADMIN"
        timestamp created_at
        timestamp updated_at
    }
    RESUMES {
        bigint id PK
        bigint user_id FK
        varchar file_name "original name"
        varchar stored_file_name UK "random UUID name on disk"
        mediumtext extracted_text
        enum status "UPLOADED, ANALYZED, NO_TEXT, FAILED"
        timestamp uploaded_at
    }
    SKILLS {
        bigint id PK
        varchar name UK
        varchar category
    }
    RESUME_SKILLS {
        bigint resume_id PK,FK
        bigint skill_id PK,FK
        varchar detection_method
    }
    JOBS {
        bigint id PK
        varchar title
        varchar company_name
        text description
        varchar location
        varchar employment_type
        varchar experience_level "Entry, Mid, Senior"
        int min_experience_years
        varchar salary_range
        varchar application_url
        boolean is_demo
        boolean active
        timestamp created_at
    }
    JOB_SKILLS {
        bigint job_id PK,FK
        bigint skill_id PK,FK
        boolean is_required "false = preferred"
    }
    RECOMMENDATIONS {
        bigint id PK
        bigint resume_id FK
        bigint job_id FK
        decimal match_score "0 to 100"
        text matched_skills
        text missing_skills
        timestamp created_at
    }
    SAVED_JOBS {
        bigint user_id PK,FK
        bigint job_id PK,FK
        timestamp saved_at
    }
```

## Design notes
- `RESUME_SKILLS`, `JOB_SKILLS` and `SAVED_JOBS` are junction tables with composite primary keys, so a pair can exist only once.
- Every foreign key uses `ON DELETE CASCADE`: deleting a user removes their resumes, saved jobs and recommendations; deleting a job removes its skills, recommendations and saved entries.
- `users.email` is unique (login identity). `resumes.stored_file_name` is unique and random, so uploaded files never collide or reveal the original name on disk.
- `RECOMMENDATIONS` is a stored snapshot of the last job analysis of a resume; re-analyzing replaces that resume's rows.
- Indexes: jobs by `active`, `title`, `company_name` and (`location`, `experience_level`, `employment_type`); recommendations by (`resume_id`, `match_score`).
