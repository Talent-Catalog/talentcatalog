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
    optional
)
SELECT
    'travelDocumentUpload',
    'UploadTask',
    'Upload a valid travel document (passport, national ID, or refugee certificate) and enter the associated data exactly as it appears on the document. If the document is expired or valid for less than 9 months, upload a valid alternative document. Contact us if you have issues or discrepancies.',
    7,
    'travel_documents',
    'travelDocument',
    'pdf,jpg,jpeg,png',
    (SELECT id FROM users WHERE username = 'SystemAdmin'),
    now(),
    'Upload Travel Document and Enter Data',
    false
WHERE NOT EXISTS (
    SELECT 1 FROM task WHERE name = 'travelDocumentUpload'
);