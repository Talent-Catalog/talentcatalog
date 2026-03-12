UPDATE task
SET description = 'Please enter details of your eligible relocating dependants.'
WHERE name = 'dependantsTravelDocFormTask';

UPDATE task
SET description = 'Submit refugee status evidence for each relocating dependant.'
WHERE name = 'dependantsRefugeeStatusInfoDocFormTask';

UPDATE candidate_property_definition
SET label = 'Travel Document Issued By'
WHERE name = 'TRAVEL_DOC_ISSUED_BY';

UPDATE candidate_property_definition
SET label = 'Travel Document Issue Date'
WHERE name = 'TRAVEL_DOC_ISSUE_DATE';

UPDATE candidate_property_definition
SET label = 'Travel Document Expiry Date'
WHERE name = 'TRAVEL_DOC_EXPIRY_DATE';
