ALTER TABLE candidate
    ADD COLUMN accepted_privacy_policy_partner_id bigint,
    ADD CONSTRAINT fk_candidate_privacy_policy_partner
        FOREIGN KEY (accepted_privacy_policy_partner_id)
            REFERENCES partner(id)
            ON DELETE SET NULL;
