ALTER TABLE users
    ADD COLUMN IF NOT EXISTS idp_issuer TEXT,
    ADD COLUMN IF NOT EXISTS idp_subject TEXT;

DROP INDEX IF EXISTS users_idp_issuer_idp_subject_uq_idx;

CREATE UNIQUE INDEX IF NOT EXISTS users_idp_issuer_idp_subject_uq_idx
    ON users (idp_issuer, idp_subject)
    WHERE idp_issuer IS NOT NULL
        AND idp_subject IS NOT NULL;
