alter table task add column candidate_form_id bigint references candidate_form;

insert into task (name, display_name, task_type, description, days_to_complete,
                  created_by, created_date, candidate_form_id)
values ('myFirstFormTask','Fill out my first form',
        'FormTask', 'This form asks you for some pretty useless information.',
        7, (select id from users where username = 'SystemAdmin'),
        now(), (select id from candidate_form where name = 'MyFirstForm'));

