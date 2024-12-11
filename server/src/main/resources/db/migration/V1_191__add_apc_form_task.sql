
insert into task (name, display_name, task_type, description, days_to_complete, upload_subfolder_name,
                  upload_type, uploadable_file_types, help_link,
                  created_by, created_date)
values ('apcInterestForm', 'Australian Physiotherapy Council - Expression of Interest','UploadTask', 'Please fill out the Expression of Interest form for the Australian PhysioTherapy Council, the form can be downloaded by clicking on the Task Help button. Once completed, please upload the filled out form below.',
        7, 'Experience', 'apcInterestForm', null, 'https://drive.google.com/file/d/1gIhD8vnJtHN-LmYtRmaZ7n4U_3-x-FPl/view?usp=sharing',
        (select id from users where username = 'SystemAdmin'), now());
