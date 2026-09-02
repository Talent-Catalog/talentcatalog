ALTER TABLE candidate
    ADD COLUMN verify_plus_consented boolean NOT NULL DEFAULT false,
    ADD COLUMN verify_plus_consented_at timestamp with time zone;
