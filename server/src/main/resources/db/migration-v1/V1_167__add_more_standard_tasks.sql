insert into task (name, task_type, description, days_to_complete, upload_subfolder_name,
                  upload_type, uploadable_file_types,
                  created_by, created_date)
values ('Residency Card or Drivers License','UploadTask', null,
        14, 'Identity', 'otherId', null,
        (select id from users where username = 'SystemAdmin'), now());

insert into task (name, task_type, description, days_to_complete, upload_subfolder_name,
                  upload_type, uploadable_file_types,
                  created_by, created_date)
values ('Residency Card or Drivers License Translation','UploadTask', 'Translation of original document',
        14, 'Identity', 'otherIdTrans', null,
        (select id from users where username = 'SystemAdmin'), now());

insert into task (name, task_type, description, days_to_complete, upload_subfolder_name,
                  upload_type, uploadable_file_types,
                  created_by, created_date)
values ('Proof of address','UploadTask',
        'You can use an attestation from your local Mayor if you have no other proof of address',
        14, 'Address', 'proofAddress', null,
        (select id from users where username = 'SystemAdmin'), now());

insert into task (name, task_type, description, days_to_complete, upload_subfolder_name,
                  upload_type, uploadable_file_types,
                  created_by, created_date)
values ('Proof of address translation','UploadTask', 'Translation of original document',
        14, 'Address', 'proofAddressTrans', null,
        (select id from users where username = 'SystemAdmin'), now());

insert into task (name, task_type, description, days_to_complete, upload_subfolder_name,
                  upload_type, uploadable_file_types,
                  created_by, created_date)
values ('Residence Attestation','UploadTask',
        'You can use an attestation from your local Mayor if you have no other proof of address',
        14, 'Address', 'residenceAttest', null,
        (select id from users where username = 'SystemAdmin'), now());

insert into task (name, task_type, description, days_to_complete, upload_subfolder_name,
                  upload_type, uploadable_file_types,
                  created_by, created_date)
values ('Residence Attestation','UploadTask', 'Translation of original document',
        14, 'Address', 'residenceAttestTrans', null,
        (select id from users where username = 'SystemAdmin'), now());

insert into task (name, task_type, description, days_to_complete, upload_subfolder_name,
                  upload_type, uploadable_file_types,
                  created_by, created_date)
values ('Police Check','UploadTask',
        'From every country you lived in for over 6 months in the last 5 years.',
        14, 'Character', 'policeCheck', null,
        (select id from users where username = 'SystemAdmin'), now());

insert into task (name, task_type, description, days_to_complete, upload_subfolder_name,
                  upload_type, uploadable_file_types,
                  created_by, created_date)
values ('Police Check Translation','UploadTask', 'Translation of original document',
        14, 'Character', 'policeCheckTrans', null,
        (select id from users where username = 'SystemAdmin'), now());

insert into task (name, task_type, description, days_to_complete, upload_subfolder_name,
                  upload_type, uploadable_file_types,
                  created_by, created_date, optional)
values ('Licensing Registration','UploadTask',
        'Optional - not needed if you are a recent graduate or do not have registration',
        14, 'Registration', 'licencing', null,
        (select id from users where username = 'SystemAdmin'), now(), true);

insert into task (name, task_type, description, days_to_complete, upload_subfolder_name,
                  upload_type, uploadable_file_types,
                  created_by, created_date, optional)
values ('Licensing Registration Translation','UploadTask', 'Translation of original document',
        14, 'Registration', 'licencingTrans', null,
        (select id from users where username = 'SystemAdmin'), now(), true);

insert into task (name, task_type, description, days_to_complete, upload_subfolder_name,
                  upload_type, uploadable_file_types,
                  created_by, created_date)
values ('Certificate of Good Conduct from Ministry of Public Health','UploadTask',
        'eg from Ministry of Public Health in Lebanon',
        14, 'Registration', 'conductMinistry', null,
        (select id from users where username = 'SystemAdmin'), now());

insert into task (name, task_type, description, days_to_complete, upload_subfolder_name,
                  upload_type, uploadable_file_types,
                  created_by, created_date)
values ('Certificate of Good Conduct from Ministry of Public Health Translation','UploadTask',
        'Translation of original document',
        14, 'Registration', 'conductMinistryTrans', null,
        (select id from users where username = 'SystemAdmin'), now());

insert into task (name, task_type, description, days_to_complete, upload_subfolder_name,
                  upload_type, uploadable_file_types,
                  created_by, created_date)
values ('Certificate of Good Conduct from Employer/Education','UploadTask',
        'From most recent healthcare employer or education if recently graduated',
        14, 'Registration', 'conductEmployer', null,
        (select id from users where username = 'SystemAdmin'), now());

insert into task (name, task_type, description, days_to_complete, upload_subfolder_name,
                  upload_type, uploadable_file_types,
                  created_by, created_date)
values ('Certificate of Good Conduct from Employer/Education Translation','UploadTask',
        'Translation of original document',
        14, 'Registration', 'conductEmployerTrans', null,
        (select id from users where username = 'SystemAdmin'), now());

insert into task (name, task_type, description, days_to_complete, upload_subfolder_name,
                  upload_type, uploadable_file_types,
                  created_by, created_date)
values ('Vaccination records','UploadTask',
        'Covid 19 vaccination certificate',
        14, 'Medicals', 'vaccination', null,
        (select id from users where username = 'SystemAdmin'), now());

insert into task (name, task_type, description, days_to_complete, upload_subfolder_name,
                  upload_type, uploadable_file_types,
                  created_by, created_date)
values ('Vaccination records translation','UploadTask',
        'Translation of original document',
        14, 'Medicals', 'vaccinationTrans', null,
        (select id from users where username = 'SystemAdmin'), now());

insert into task (name, task_type, description, days_to_complete, upload_subfolder_name,
                  upload_type, uploadable_file_types,
                  created_by, created_date)
values ('Letter of offer','UploadTask',
        'From employer',
        14, 'Employer', 'offer', null,
        (select id from users where username = 'SystemAdmin'), now());

insert into task (name, task_type, description, days_to_complete, upload_subfolder_name,
                  upload_type, uploadable_file_types,
                  created_by, created_date, optional)
values ('English exam results - OET/IELTS','UploadTask',
        'Optional - upload result if you have taken an exam',
        14, 'English', 'englishExam', null,
        (select id from users where username = 'SystemAdmin'), now(), true);

insert into task (name, task_type, description, days_to_complete, upload_subfolder_name,
                  upload_type, uploadable_file_types,
                  created_by, created_date)
values ('Certificate of sponsorship (COS)','UploadTask',
        'Candidates should also send directly to trust',
        14, 'Immigration', 'cos', null,
        (select id from users where username = 'SystemAdmin'), now());

insert into task (name, task_type, description, days_to_complete, upload_subfolder_name,
                  upload_type, uploadable_file_types,
                  created_by, created_date)
values ('References','UploadTask',
        'Candidates just need to provide email address or phone number for referees, not document. Need to cover a full three years from a professional institution i.e work or education. If there is a gap of over 3 months, you need to provide character referees who are working within a professional environment.',
        14, 'Immigration', 'references', null,
        (select id from users where username = 'SystemAdmin'), now());
