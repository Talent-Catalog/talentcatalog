
alter table task add column explicit_allowed_answers text;

insert into task (name, display_name, task_type, description, days_to_complete, explicit_allowed_answers,
                  created_by, created_date)
values ('studiedEnglishUniversity','Did you study in English at University?', 'QuestionTask', 'Please select:',
        7, 'Yes,No',
        (select id from users where username = 'SystemAdmin'), now());
