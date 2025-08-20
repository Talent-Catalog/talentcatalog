INSERT INTO task (
    name,
    task_type,
    description,
    days_to_complete,
    upload_subfolder_name,
    upload_type,
    uploadable_file_types,
    created_by,
    created_date,
    display_name,
    optional,
    required_metadata
)
SELECT
    'candidateTravelDocumentUpload',
    'UploadTask',
    'Upload a valid travel document (passport, national ID, or refugee certificate) and enter the associated data exactly as it appears on the document. If the document is expired or valid for less than 9 months, upload a valid alternative document. Contact us if you have issues or discrepancies.',
    7,
    'Candidate Travel Documents',
    'candidateTravelDocumentUpload',
    'pdf,jpg,jpeg,png',
    (SELECT id FROM users WHERE username = 'SystemAdmin'),
    NOW(),
    'Upload Travel Document and Enter Data',
    FALSE,
    '[
      { "name": "firstName", "type": "text", "label": "TASKS.META_DATA.FIRSTNAME" },
      { "name": "lastName", "type": "text", "label": "TASKS.META_DATA.LASTNAME" },
      { "name": "dob", "type": "date", "label": "TASKS.META_DATA.DOB" },
      { "name": "gender", "type": "select", "label": "TASKS.META_DATA.GENDER", "options": ["GENDER.MALE", "GENDER.FEMALE", "GENDER.OTHER"] },
      { "name": "birthCountry", "type": "select", "label": "TASKS.META_DATA.BIRTH_COUNTRY" },
      { "name": "placeOfBirth", "type": "text", "label": "TASKS.META_DATA.PLACE_OF_BIRTH" },
      { "name": "refugeeStatus", "type": "select", "label": "TASKS.META_DATA.REFUGEE_STATUS", "options": ["UNHCR_RECOGNIZED", "HOST_COUNTRY_RECOGNIZED","PENDING"] },
      { "name": "documentType", "type": "select", "label": "TASKS.META_DATA.DOCUMENT_TYPE", "options": ["Passport", "National ID", "Refugee Certificate"] },
      { "name": "documentNumber", "type": "text", "label": "TASKS.META_DATA.DOCUMENT_NUMBER" },
      { "name": "issuedBy", "type": "text", "label": "TASKS.META_DATA.ISSUED_BY" },
      { "name": "issueDate", "type": "date", "label": "TASKS.META_DATA.ISSUE_DATE" },
      { "name": "expiryDate", "type": "date", "label": "TASKS.META_DATA.EXPIRY_DATE" }
    ]'::jsonb
WHERE NOT EXISTS (
    SELECT 1 FROM task WHERE name = 'candidateTravelDocumentUpload'
);