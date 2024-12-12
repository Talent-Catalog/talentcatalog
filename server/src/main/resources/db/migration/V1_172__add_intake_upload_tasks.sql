
insert into task (name, task_type, description, days_to_complete, upload_subfolder_name,
                  upload_type, uploadable_file_types, help_link,
                  created_by, created_date)
values ('Collaboration Agreement','UploadTask', 'Please sign and upload collaboration agreement. You can download the document by clicking on the View Task Help button.',
        7, 'TBB Forms', 'collaborationAgreement', null, 'https://www.talentbeyondboundaries.org/help/upload-collaboration-agreement',
        (select id from users where username = 'SystemAdmin'), now());

insert into task (name, task_type, description, days_to_complete, upload_subfolder_name,
                  upload_type, uploadable_file_types, help_link,
                  created_by, created_date)
values ('Information Release Form','UploadTask', 'Please fill out, sign and upload the Talent Beyond Boundaries information release form. You can download the document by clicking on the View Task Help button.',
        7, 'TBB Forms', 'infoReleaseForm', null, 'https://www.talentbeyondboundaries.org/help/upload-tbb-info-release-form',
        (select id from users where username = 'SystemAdmin'), now());
