insert into task (admin, created_by, created_date, days_to_complete, description, name, optional, task_type,
                  display_name)
values (false, (select id from users where username = 'SystemAdmin'), now(), 14,
        'We are offering you a free Duolingo Test.', 'duolingoTest', false, 'Task',
        'Take the Duolingo English Test for free!');