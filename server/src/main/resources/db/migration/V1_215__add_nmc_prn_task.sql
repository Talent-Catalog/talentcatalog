
insert into task (name, display_name, task_type, description, days_to_complete,
                  created_by, created_date)
values ('nmcPrn','What is your NMC PRN (Nursing and Midwifery Council Personal Reference Number)?',
        'QuestionTask', 'Please enter a 10 digit number',
        7, (select id from users where username = 'SystemAdmin'), now());
