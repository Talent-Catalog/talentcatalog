
create table partner_job
(
    partner_id bigint references partner,
    tc_job_id bigint references salesforce_job_opp,
    contact_id bigint references users,
    primary key (partner_id, tc_job_id)
);

