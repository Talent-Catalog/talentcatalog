-- Spring Batch 6 uses batch_job_instance_seq for job instance ids instead of batch_job_seq.
-- Renaming keeps the current sequence value, so new job instance ids continue from the existing
-- ones. This is the only schema change between Spring Batch 5 and 6 - see Spring Batch's
-- org/springframework/batch/core/migration/6.0/migration-postgresql.sql (TC-1189).
ALTER SEQUENCE batch_job_seq RENAME TO batch_job_instance_seq;
