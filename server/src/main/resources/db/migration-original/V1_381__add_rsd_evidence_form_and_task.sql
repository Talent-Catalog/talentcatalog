insert into candidate_form (name, description)
values ('RsdEvidenceForm', 'Provide your refugee status evidence and identification details.');


insert into task (name, display_name, description, days_to_complete, optional, task_type,
                  candidate_form_id, created_by, created_date)
values ('rsdEvidenceFormTask', 'RSD Evidence',
        'Provide your refugee status evidence, document type, and identification number.', 7, false,
        'FormTask', (select id from candidate_form where name = 'RsdEvidenceForm'),
        (select id from users where username = 'SystemAdmin'), now());