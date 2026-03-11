
alter table candidate_visa_job_check add column languages_required text;
alter table candidate_visa_job_check add column languages_threshold_met text;

alter table candidate_visa_job_check rename column english_threshold_notes to languages_threshold_notes;
