CREATE EXTENSION IF NOT EXISTS vector;

/*
 * Copyright (c) 2025 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

-- Add a temporary text search field. We will drop this later - replacing it with a candidate level
-- field which will be computed concatenation of all job description fields
ALTER TABLE candidate_job_experience ADD COLUMN ts tsvector
    GENERATED ALWAYS AS (to_tsvector('english', description)) STORED;

-- Temporary - see above comments
CREATE INDEX ts_idx ON candidate_job_experience USING GIN (ts);
