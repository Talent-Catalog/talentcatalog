ALTER TABLE agreement
    ADD COLUMN terms_type VARCHAR(255);

UPDATE agreement
SET terms_type = CASE terms_info_id
    WHEN 'GrnCandidatePrivacyPolicyV1' THEN 'GRN_CANDIDATE_PRIVACY_POLICY'
    WHEN 'GrnCandidatePrivacyPolicyV2' THEN 'GRN_CANDIDATE_PRIVACY_POLICY'
    WHEN 'LegacyRedirectToTbbWebsite' THEN 'TBB_CANDIDATE_PRIVACY_POLICY'
    WHEN 'OpcDataProcessingAgreementV1' THEN 'OPC_STANDARD_DATA_PROCESSING_AGREEMENT'
    WHEN 'ReferenceServiceTermsV1' THEN 'REFERENCE_SERVICE_TERMS'
END;

ALTER TABLE agreement
    ALTER COLUMN terms_type SET NOT NULL;

DROP INDEX agreement_active_candidate_counterparty_uq_idx;

CREATE UNIQUE INDEX agreement_active_candidate_counterparty_uq_idx
    ON agreement (candidate_id, counterparty_id, terms_type)
    WHERE end_date IS NULL;
