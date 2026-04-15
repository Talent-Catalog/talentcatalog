insert into task (name, task_type, description, days_to_complete, upload_subfolder_name,
                  upload_type, uploadable_file_types,
                  created_by, created_date)
values ('Degree certificate','UploadTask', 'Bachelors degree certificate',
        7, 'Qualification', 'degree', null,
        (select id from users where username = 'SystemAdmin'), now());

insert into task (name, task_type, description, days_to_complete, upload_subfolder_name,
                  upload_type, uploadable_file_types,
                  created_by, created_date)
values ('Degree transcript','UploadTask', 'Transcript with grades for all years',
        7, 'Qualification', 'degreeTranscript', null,
        (select id from users where username = 'SystemAdmin'), now());

insert into task (name, task_type, description, days_to_complete, upload_subfolder_name,
                  upload_type, uploadable_file_types,
                  created_by, created_date)
values ('Degree transcript translation','UploadTask', 'Translation of degree transcript',
        7, 'Qualification', 'degreeTranscriptTrans', null,
        (select id from users where username = 'SystemAdmin'), now());

insert into task (name, task_type, description, days_to_complete, upload_subfolder_name,
                  upload_type, uploadable_file_types,
                  created_by, created_date)
values ('Letter studied in English','UploadTask',
        'Letter from your university stating, in English, that you studied in English',
        7, 'Qualification', 'studiedInEnglish', null,
        (select id from users where username = 'SystemAdmin'), now());

insert into task (name, task_type, description, days_to_complete, upload_subfolder_name,
                  upload_type, uploadable_file_types,
                  created_by, created_date)
values ('Passport','UploadTask',
        'Must have 6 months validity after arrival to the UK. Birth date must be present and correct',
        14, 'Identity', 'passport', null,
        (select id from users where username = 'SystemAdmin'), now());
