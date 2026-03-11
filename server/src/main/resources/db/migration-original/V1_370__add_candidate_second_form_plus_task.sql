insert into candidate_form (name, description)
values ('MySecondForm',
        'Enter city where you live and the colour of your hair');


insert into task (name, display_name, task_type, description, days_to_complete,
                  created_by, created_date, candidate_form_id)
values ('mySecondFormTask','Fill out my second form',
        'FormTask', 'This form asks you for more pretty useless information.',
        7, (select id from users where username = 'SystemAdmin'),
        now(), (select id from candidate_form where name = 'MySecondForm'));

