
update salesforce_job_opp set recruiter_partner_id =
                                  (select id from partner where default_destination_partner = true)
where recruiter_partner_id is null;
