
-- Remove previously created special extra default_destination_partner
-- Instead we will support the same partner being both default source and destination partner

-- First replace all occurrences of that new partner with default source partner
update salesforce_job_opp set recruiter_partner_id =
        (select id from partner where default_source_partner = true)
    where recruiter_partner_id = (select id from partner where default_destination_partner = true);

update users set partner_id = (select id from partner where default_source_partner = true)
where partner_id = (select id from partner where default_destination_partner = true);

-- Now delete the partner
delete from partner where default_destination_partner = true;

-- Finally make the default source partner also the default destination partner
update partner set default_destination_partner = true where default_source_partner = true;
