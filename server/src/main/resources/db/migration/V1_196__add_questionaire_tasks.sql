
insert into task (name, display_name, task_type, description, days_to_complete, explicit_allowed_answers,
                  created_by, created_date)
values ('spouseJobUk','Is your spouse also applying for a job in the UK?', 'QuestionTask',
        'Please select: (if you do not have a spouse, please abandon the task and provide comment that you do not have a spouse)',
        7, 'Yes,No',
        (select id from users where username = 'SystemAdmin'), now());

insert into task (name, display_name, task_type, description, days_to_complete, explicit_allowed_answers,
                  created_by, created_date)
values ('validPassport','Do you have a valid passport (in date with no omissions of dates e.g. birth date, at least 6 months validity after arrival in UK)?', 'QuestionTask',
        'Please select: (if no, please apply for a passport as soon as possible. If you need support with paying for funds)',
        7, 'Yes,No',
        (select id from users where username = 'SystemAdmin'), now());

insert into task (name, display_name, task_type, description, days_to_complete, explicit_allowed_answers,
                  created_by, created_date)
values ('minBachelorsDegree','Did you get at least a Bachelors degree?', 'QuestionTask',
        'Please select: (if did not study in english or if have technical baccalaureate, need to book an English test)',
        7, 'Yes,No',
        (select id from users where username = 'SystemAdmin'), now());

insert into task (name, display_name, task_type, description, days_to_complete, explicit_allowed_answers,
                  created_by, created_date)
values ('colleaguesKnowRefugeeStatus','Are you happy for colleagues to know you are a refugee/displaced talent at work?', 'QuestionTask',
        'Please select: (if maybe, please provide comment)',
        7, 'Yes,No,Maybe',
        (select id from users where username = 'SystemAdmin'), now());

insert into task (name, display_name, task_type, description, days_to_complete, explicit_allowed_answers,
                  created_by, created_date)
values ('ownLaptopUK','Do you have a laptop you can bring to the UK?', 'QuestionTask',
        'Please select: ',
        7, 'Yes,No',
        (select id from users where username = 'SystemAdmin'), now());

insert into task (name, display_name, task_type, description, days_to_complete, explicit_allowed_answers,
                  created_by, created_date)
values ('shareDetailsTbbPartners','Do you agree to TBB sharing your contact details (phone/ whatsapp number and email address) with our partners, including IOM and Reset? (We will only share your contact details if absolutely necessary for your relocation to the UK e.g. to book flights)', 'QuestionTask',
        'Please select: (if maybe, please provide comment)',
        7, 'Yes,No,Maybe',
        (select id from users where username = 'SystemAdmin'), now());
