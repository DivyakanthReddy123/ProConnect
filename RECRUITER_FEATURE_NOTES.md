# Recruiter Feature V1

This zip adds a clean Recruiter V1 module to the LinkedIn clone.

## Added flow

Recruiter signup/login → Recruiter Dashboard → Post Job → My Jobs → Applicants → Candidate Profile / Messaging.

## Main backend changes

- Added `RECRUITER` role in `RoleType.java`.
- Updated signup so users can register as either `PROFESSIONAL` or `RECRUITER`.
- Added expanded job fields in `Job.java`.
- Added recruiter-specific job APIs in `JobsController.java`:
  - `GET /in/{id}/recruiter/dashboard`
  - `GET /in/{id}/recruiter/jobs`
  - `POST /in/{id}/new-job`
  - `PUT /in/{id}/jobs/{jobId}`
  - `DELETE /in/{id}/jobs/{jobId}`
  - `GET /in/{id}/jobs/{jobId}/applicants`
- Restricted job posting to recruiters/admins.
- Updated seed data to include the `RECRUITER` role.

## Main frontend changes

- Signup now supports:
  - Job Seeker / Professional
  - Recruiter
- Login redirects recruiters to `/recruiter/dashboard`.
- Added recruiter pages:
  - `/recruiter/dashboard`
  - `/recruiter/post-job`
  - `/recruiter/jobs`
  - `/recruiter/jobs/:jobId/applicants`
  - `/recruiter/profile`
- Existing Jobs page remains available for professionals to browse and apply.

## Notes

- V1 reuses the existing `User` model for recruiter/company profile fields:
  - `currentCompany` = company name
  - `currentJob` = recruiter designation
  - `city` = company location
  - `website` = company website
- Applicant status like shortlisted/rejected/contacted is not added yet because that needs a separate `JobApplication` entity/table. This is the recommended V2.
