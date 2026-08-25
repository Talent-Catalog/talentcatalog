ALTER TABLE job_experience_embedding_minilm_l6_spacy_v3
    RENAME TO experience_embedding_minilm_l6_v3;

ALTER INDEX idx_job_experience_embedding_minilm_l6_spacy_v3
    RENAME TO idx_experience_embedding_minilm_l6_v3;
