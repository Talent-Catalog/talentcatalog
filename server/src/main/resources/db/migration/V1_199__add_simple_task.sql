
insert into task (name, display_name, task_type, description, days_to_complete,
                  created_by, created_date)
values ('researchForInterview','Research for interview', 'Task',
        'Research the employer and role prior to your interview.',
        7, (select id from users where username = 'SystemAdmin'), now());
