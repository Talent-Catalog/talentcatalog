ALTER TABLE candidate ADD COLUMN text text;

ALTER TABLE candidate ADD COLUMN ts_text tsvector
    GENERATED ALWAYS AS (to_tsvector('english', text)) STORED;

CREATE INDEX ts_text_idx ON candidate USING GIN (ts_text);
