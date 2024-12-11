
-- Copy submission list from old job table to corresponding SalesforceJobOpp
update salesforce_job_opp set submission_list_id =
      (select submission_list_id from job j where salesforce_job_opp.id =
            (select sf_job_opp_id from saved_list sl where sl.id = j.submission_list_id) limit 1)
