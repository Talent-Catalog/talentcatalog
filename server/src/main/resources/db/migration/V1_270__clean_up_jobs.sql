
-- For old jobs that did not have a publishedDate, set the published date to the created date
-- and publishedBy to createdBy
-- The publishedDate field was introduced 2022-10-31
update salesforce_job_opp set published_date = created_date
    where published_date is null and created_date <= '2022-10-31'::date;

update salesforce_job_opp set published_by = created_by
    where published_by is null and created_by is not null and created_date <= '2022-10-31'::date;
