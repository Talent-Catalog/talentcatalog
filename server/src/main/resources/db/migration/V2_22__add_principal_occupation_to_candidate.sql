ALTER TABLE candidate
    ADD COLUMN principal_occupation_id bigint REFERENCES candidate_occupation (id) ON DELETE SET NULL;
