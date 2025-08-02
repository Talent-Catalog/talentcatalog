ALTER TABLE candidate
    ADD COLUMN accepted_privacy_policy_partner_id bigint
        REFERENCES partner(id)
            ON DELETE SET NULL;
