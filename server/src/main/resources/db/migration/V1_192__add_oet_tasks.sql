
insert into task (name, display_name, task_type, description, days_to_complete, upload_subfolder_name,
                  upload_type, uploadable_file_types, help_link,
                  created_by, created_date)
values ('OETPulseResults', 'OET Pulse Test result (Please upload a screenshot of your result)','UploadTask', 'Please upload a screenshot of the results screen once completed your test. It should show your Reading, Listening, Language Knowledge and Overall scores.',
        7, 'Language', 'OETPulseResults', null, null,
        (select id from users where username = 'SystemAdmin'), now());

insert into task (name, display_name, task_type, description, days_to_complete, candidate_answer_field,
                  created_by, created_date)
values ('oetOverallScore','What was your overall OET score?', 'QuestionTask', 'Please write your score below (numeric value)',
        7, 'candidateExams.OET',
        (select id from users where username = 'SystemAdmin'), now());

insert into task (name, display_name, task_type, description, days_to_complete, candidate_answer_field,
                  created_by, created_date)
values ('oetReadingScore','What was your OET Reading score?', 'QuestionTask', 'Please write your score below (numeric value)',
        7, 'candidateExams.OETRead',
        (select id from users where username = 'SystemAdmin'), now());

insert into task (name, display_name, task_type, description, days_to_complete, candidate_answer_field,
                  created_by, created_date)
values ('oetListeningScore','What was your OET Listening score?', 'QuestionTask', 'Please write your score below (numeric value)',
        7, 'candidateExams.OETList',
        (select id from users where username = 'SystemAdmin'), now());

insert into task (name, display_name, task_type, description, days_to_complete, candidate_answer_field,
                  created_by, created_date)
values ('oetLanguageScore','What was your OET Language Knowledge score?', 'QuestionTask', 'Please write your score below (numeric value)',
        7, 'candidateExams.OETLang',
        (select id from users where username = 'SystemAdmin'), now());

