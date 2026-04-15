UPDATE candidate_form
SET name = 'TravelDocForm'
WHERE name = 'ItalyCandidateTravelDocumentForm';

UPDATE task
SET name = 'travelDocFormTask',
    candidate_form_id = (SELECT id FROM candidate_form WHERE name = 'TravelDocForm')
WHERE name = 'italyCandidateTravelDocumentTask';
