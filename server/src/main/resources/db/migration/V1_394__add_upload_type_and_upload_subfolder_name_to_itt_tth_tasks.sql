update task
set upload_type = 'refugeeStatusDoc',
    upload_subfolder_name = 'Immigration'
where name = 'TTH_IT$refugeeStatusDocUpload';

update task
set upload_type = 'dependantRefugeeStatusDoc',
    upload_subfolder_name = 'Immigration'
where name = 'TTH_IT$dependantRefugeeStatusDocUpload';

update task
set upload_type = 'travelDoc',
    upload_subfolder_name = 'Immigration'
where name = 'TTH_IT$travelDocUpload';

update task
set upload_type = 'dependantTravelDoc',
    upload_subfolder_name = 'Immigration'
where name = 'TTH_IT$dependantTravelDocUpload';
