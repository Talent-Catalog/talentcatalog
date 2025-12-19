
insert into task (name, task_type, description, days_to_complete, upload_subfolder_name,
                  upload_type, uploadable_file_types, help_link,
                  created_by, created_date)
values ('ID Card','UploadTask', null,
        7, 'Identity', 'idCard', null, null,
        (select id from users where username = 'SystemAdmin'), now());

insert into task (name, task_type, description, days_to_complete, upload_subfolder_name,
                  upload_type, uploadable_file_types, help_link,
                  created_by, created_date)
values ('UNHCR/UNRWA Registration Card','UploadTask', 'Copy of your UNHCR or UNRWA Registration Certificate or Barcode (if applicable). If you are not registered with the UNHCR or UNRWA, please click "abandon task" and add a comment explaining the reason why you are not registered.',
        7, 'Registration', 'unhcrUnrwaRegCard', null, null,
        (select id from users where username = 'SystemAdmin'), now());
