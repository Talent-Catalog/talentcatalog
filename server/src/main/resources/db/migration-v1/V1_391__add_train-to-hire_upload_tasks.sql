-- TRAVEL DOCUMENT UPLOAD TASKS
-- Relocating candidate travel document
insert into task (name, display_name, task_type, description, days_to_complete, created_by,
                  created_date)
values ('italyTrainToHireTravelDocUpload', 'Travel Document Upload', 'UploadTask', 'placeholder',
        7, (select id from users where username = 'SystemAdmin'), now());

-- Relocating candidate dependant travel doc upload
insert into task (name, display_name, task_type, description, days_to_complete, created_by,
                  created_date)
values ('italyTrainToHireDependantTravelDocUpload', 'Dependant Travel Document Upload', 'UploadTask',
        'placeholder', 7, (select id from users where username = 'SystemAdmin'), now());

-- REFUGEE STATUS DOCUMENT UPLOAD TASKS
-- Relocating candidate refugee status document
insert into task (name, display_name, task_type, description, days_to_complete, created_by,
                  created_date)
values ('italyTrainToHireRefugeeStatusDocUpload', 'Refugee Status Document Upload', 'UploadTask',
        'placeholder', 7, (select id from users where username = 'SystemAdmin'), now());

-- Relocating candidate dependant refugee status document
insert into task (name, display_name, task_type, description, days_to_complete, created_by,
                  created_date)
values ('italyTrainToHireDependantRefugeeStatusDocUpload', 'Dependant Refugee Status Document Upload',
        'UploadTask', 'placeholder', 7, (select id from users where username = 'SystemAdmin'), now());