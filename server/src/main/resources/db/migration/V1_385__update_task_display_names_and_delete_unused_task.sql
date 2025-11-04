UPDATE task
SET display_name = 'Your Travel Document'
WHERE name = 'travelDocFormTask';

UPDATE task
SET display_name = 'Family Travel Documents'
WHERE name = 'familyDocFormTask';

UPDATE task
SET display_name = 'Your RSD Document'
WHERE name = 'rsdEvidenceFormTask';

UPDATE task
SET display_name = 'Family RSD Documents'
WHERE name = 'familyRsdEvidenceFormTask';

DELETE FROM task
WHERE name = 'mySecondFormTask';