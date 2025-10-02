insert into candidate_form (name, description)
values ('FamilyDocForm',
        'Enter details of your eligible relocating family members.');


insert into task (name, display_name, task_type, description, days_to_complete,
                  created_by, created_date, candidate_form_id)
values ('familyDocFormTask','Enter details of your eligible relocating family members',
        'FormTask', 'Please enter details of your eligible relocating family members',
        7, (select id from users where username = 'SystemAdmin'),
        now(), (select id from candidate_form where name = 'FamilyDocForm'));

