insert into candidate_form (name, description)
values ('FamilyRsdEvidenceForm',
        'Provide refugee status evidence for each relocating family member.');

insert into task (name, display_name, task_type, description, days_to_complete,
                  created_by, created_date, candidate_form_id)
values ('familyRsdEvidenceFormTask', 'Family RSD Evidence',
        'FormTask', 'Submit refugee status details evidence for each relocating family member.',
        7, (select id from users where username = 'SystemAdmin'),
        now(), (select id from candidate_form where name = 'FamilyRsdEvidenceForm'));

insert into candidate_property_definition (name, label, definition, type)
values ('FAMILY_RSD_EVIDENCE_INFO',
        'Relocating family RSD evidence',
        'Refugee status document details for each relocating family members.',
        'JSON');