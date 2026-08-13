ALTER TABLE saved_search
    ADD COLUMN requirements TEXT,
    ADD COLUMN lexical_weight DOUBLE PRECISION DEFAULT 0.5 NOT NULL,
    ADD COLUMN model_key TEXT;
