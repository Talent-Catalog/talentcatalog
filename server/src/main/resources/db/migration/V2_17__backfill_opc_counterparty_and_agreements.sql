-- Backfill OPC counterparty and agreement rows for GRN candidates who accepted the privacy policy
-- before the counterparty/agreement tables were introduced (V2_16).
--
-- OPC counterparty row, linked by partner_id.
-- The OPC partner is guaranteed to exist — it is seeded at startup by SystemAdminConfiguration.
INSERT INTO counterparty (type, partner_id, created_date, updated_date)
SELECT 'DATABASE_PROVIDER', p.id, NOW(), NOW()
FROM partner p
WHERE p.abbreviation = 'OPC'
  AND NOT EXISTS (
      SELECT 1 FROM counterparty c
      WHERE c.type = 'DATABASE_PROVIDER' AND c.partner_id = p.id
  );

-- Backfills one agreement row per GRN candidate who has already accepted the privacy policy
-- but does not yet have an active agreement row for the OPC counterparty.
-- Filtered to known GRN terms IDs.
INSERT INTO agreement (candidate_id, counterparty_id, terms_info_id, start_date, end_date, created_date, updated_date)
SELECT
    c.id,
    cp.id,
    c.accepted_privacy_policy_id,
    c.accepted_privacy_policy_date,
    NULL,
    NOW(),
    NOW()
FROM candidate c
JOIN counterparty cp ON cp.type = 'DATABASE_PROVIDER'
JOIN partner p ON p.id = cp.partner_id AND p.abbreviation = 'OPC'
WHERE c.accepted_privacy_policy_id IN ('GrnCandidatePrivacyPolicyV1', 'GrnCandidatePrivacyPolicyV2')
  AND c.accepted_privacy_policy_date IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM agreement a
      WHERE a.candidate_id = c.id
        AND a.counterparty_id = cp.id
        AND a.end_date IS NULL
  );
