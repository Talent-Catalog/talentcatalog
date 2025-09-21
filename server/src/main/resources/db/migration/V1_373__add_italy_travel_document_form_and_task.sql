insert into candidate_form (name, description)
values ('TravelDocForm', 'Check and enter your travel document information so it matches your official document.');

insert into task (
    name,
    display_name,
    description,
    days_to_complete,
    optional,
    task_type,
    candidate_form_id,
    created_by,
    created_date
)
values (
           'travelDocFormTask',
           'Review and confirm your travel document details.',
           'Check and enter your travel document information so it matches your official document.',
           7,
           false,
           'FormTask',
           (select id from candidate_form where name = 'TravelDocForm'),
           (select id from users where username = 'SystemAdmin'),
           now()
       );