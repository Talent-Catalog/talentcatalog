
-- Update any null country object fields by looking up country text field
-- Text country field is deprecated and will be removed in future.
update salesforce_job_opp j
set country_object_id =
        (select country.id from country where name = j.country)
where country_object_id is null and country is not null;
