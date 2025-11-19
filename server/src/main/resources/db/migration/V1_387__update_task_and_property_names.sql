UPDATE task
SET display_name = 'Travel Info Document', name = 'travelInfoDocFormTask'
WHERE name = 'travelDocFormTask';

UPDATE task
SET display_name = 'Dependants Travel Info Documents', name = 'dependantsTravelDocFormTask'
WHERE name = 'familyDocFormTask';

UPDATE task
SET display_name = 'Refugee Status Info Document', name = 'refugeeStatusInfoDocFormTask'
WHERE name = 'rsdEvidenceFormTask';

UPDATE task
SET display_name = 'Dependants Refugee Status Info Documents', name = 'dependantsRefugeeStatusInfoDocFormTask'
WHERE name = 'familyRsdEvidenceFormTask';

DELETE FROM candidate_property_definition
WHERE name = 'FAMILY_RSD_EVIDENCE_INFO';

UPDATE candidate_property_definition
SET name = 'DEPENDANTS_INFO', definition = 'Information about dependants of a candidate.'
WHERE name = 'DEPENDANTS_TRAVEL_INFO';

UPDATE candidate_property_definition
SET name = 'REFUGEE_STATUS_EVIDENCE_DOCUMENT_TYPE', definition = 'Type of Refugee Staus evidence document (UNHCR Certificate, Host Country ID, etc.).'
WHERE name = 'EVIDENCE_DOCUMENT_TYPE';

UPDATE candidate_property_definition
SET name = 'REFUGEE_STATUS_EVIDENCE_DOCUMENT_NUMBER', definition = 'Number printed on the Refugee Status evidence document.'
WHERE name = 'EVIDENCE_DOCUMENT_NUMBER';
