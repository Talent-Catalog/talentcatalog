CREATE EXTENSION IF NOT EXISTS vector;

-- Add a temporary text search field. We will drop this later - replacing it with a candidate level
-- field which will be computed concatenation of all job description fields
ALTER TABLE candidate_job_experience ADD COLUMN ts tsvector
    GENERATED ALWAYS AS (to_tsvector('english', description)) STORED;

-- Temporary - see above comments
CREATE INDEX ts_idx ON candidate_job_experience USING GIN (ts);
