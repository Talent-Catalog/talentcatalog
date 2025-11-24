INSERT INTO candidate_property_definition (name, label, definition, type)
VALUES ('TRAVEL_DOC_TYPE', 'Travel Document Type',
        'Type of travel document (e.g., Passport, Refugee Travel Doc, National ID).', 'ENUM'),
       ('TRAVEL_DOC_NUMBER', 'Travel Document Number',
        'Unique number printed on the travel document.', 'TEXT'),
       ('TRAVEL_DOC_ISSUED_BY', 'Issued By',
        'Authority or country that issued the travel document.', 'TEXT'),
       ('TRAVEL_DOC_ISSUE_DATE', 'Issue Date', 'Date when the travel document was issued.', 'DATE'),
       ('TRAVEL_DOC_EXPIRY_DATE', 'Expiry Date', 'Date when the travel document expires.', 'DATE');

-- RsdEvidenceForm properties
INSERT INTO candidate_property_definition (name, label, definition, type)
VALUES ('REFUGEE_STATUS', 'Refugee Status',
        'Refugee status according to UNHCR or host country determination.', 'ENUM'),
       ('EVIDENCE_DOCUMENT_TYPE', 'Evidence Document Type',
        'Type of RSD evidence document (UNHCR Certificate, Host Country ID, etc.).', 'ENUM'),
       ('EVIDENCE_DOCUMENT_NUMBER', 'Evidence Document Number',
        'Number printed on the RSD evidence document.', 'TEXT');
