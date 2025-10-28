INSERT INTO candidate_property_definition (name, label, definition, type)
VALUES ('travelDocType', 'Travel Document Type',
        'The type of travel document (e.g., Passport, Refugee Travel Doc, National ID).', 'ENUM'),
       ('travelDocNumber', 'Travel Document Number',
        'The unique number printed on the travel document.', 'TEXT'),
       ('travelDocIssuedBy', 'Issued By', 'Authority or country that issued the travel document.',
        'TEXT'),
       ('travelDocIssueDate', 'Issue Date', 'Date when the travel document was issued.', 'DATE'),
       ('travelDocExpiryDate', 'Expiry Date', 'Date when the travel document expires.', 'DATE'),

        -- Add RsdEvidenceForm candidate properties
       ('refugeeStatus', 'Refugee Status',
        'Refugee status according to UNHCR or host country determination.', 'ENUM'),
       ('documentType', 'Evidence Document Type',
        'Type of document proving refugee status (UNHCR certificate, Host Country ID, etc.).',
        'ENUM'),
       ('documentNumber', 'Evidence Document Number',
        'Number printed on the RSD evidence document.', 'TEXT');
