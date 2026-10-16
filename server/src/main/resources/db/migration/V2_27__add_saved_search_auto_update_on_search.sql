ALTER TABLE saved_search
    ADD COLUMN auto_update_on_search BOOLEAN DEFAULT TRUE NOT NULL;