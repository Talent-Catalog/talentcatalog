
alter table candidate_visa_job_check add column job_opp_id bigint references salesforce_job_opp;

update candidate_visa_job_check set job_opp_id =
    (select id from salesforce_job_opp where candidate_visa_job_check.sf_job_link = CONCAT('https://talentbeyondboundaries.lightning.force.com/lightning/r/Opportunity/', sf_id, '/view'))
where job_opp_id is null;
