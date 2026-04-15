
update salesforce_job_opp set created_date =
    (select created_date from saved_list where saved_list.id = salesforce_job_opp.submission_list_id);
update salesforce_job_opp set created_by =
    (select created_by from saved_list where saved_list.id = salesforce_job_opp.submission_list_id);
