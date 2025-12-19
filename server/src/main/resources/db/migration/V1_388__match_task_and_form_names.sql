UPDATE candidate_form
SET name = 'TravelInfoForm'
WHERE name = 'TravelDocForm';

UPDATE candidate_form
SET name = 'RefugeeStatusInfoForm'
WHERE name = 'RsdEvidenceForm';

UPDATE candidate_form
SET name = 'DependantsTravelInfoForm',
    description = 'Enter details of dependants relocating with you.'
WHERE name = 'FamilyDocForm';

UPDATE candidate_form
SET name = 'DependantsRefugeeStatusInfoForm',
    description = 'Provide refugee status evidence for each dependant relocating with you.'
WHERE name = 'FamilyRsdEvidenceForm';
