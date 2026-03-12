
-- Tc_job_id should be unique
alter table salesforce_job_opp add constraint uq_tc_job_id unique (tc_job_id);

-- Make additional index based on tc_job_id
create index salesforce_job_opp_tc_job_id_idx on salesforce_job_opp(tc_job_id);

-- New fields
alter table salesforce_job_opp add column contact_email text;
alter table salesforce_job_opp add column contact_user_id bigint references users;
alter table salesforce_job_opp add column job_summary text;
alter table salesforce_job_opp add column recruiter_partner_id bigint references partner;
alter table salesforce_job_opp add column suggested_list_id bigint references saved_list;

-- Support multiple salesforce_job_opp suggestedSearches
create table job_suggested_saved_search
(
    tc_job_id bigint references salesforce_job_opp(tc_job_id),
    saved_search_id bigint references saved_search,
    primary key (tc_job_id, saved_search_id)
);


-- ******  Make numeric job id the primary key of salesforce_job_opp *****

-- Start by removing existing primary key = ie the id which has the sf job id
-- To remove that primary key we have to drop things that use it - SavedList and SavedSearch
-- use it to reference the Salesforce Job opp table
alter table saved_list
    drop constraint saved_list_sf_job_opp_id_fkey;
alter table saved_search
    drop constraint saved_search_sf_job_opp_id_fkey;


-- Now remove the old primary key id
alter table salesforce_job_opp
    drop constraint salesforce_job_opp_pkey;

-- But make that field unique so that we can still use it as a unique index look up
-- because we need to be able look up job opp from the SF id.
alter table salesforce_job_opp
    add unique (id);

-- Rename the fields. The numeric job id field becomes id, and the existing id becomes sf_job_opp_id
alter table salesforce_job_opp rename column id to sf_job_opp_id;
alter table salesforce_job_opp rename column tc_job_id to id;

-- Finally make the new numeric id, the primary key
alter table salesforce_job_opp add constraint salesforce_job_opp_pkey primary key (id);

-- We no longer need the id index
alter table salesforce_job_opp
    drop constraint salesforce_job_opp_id_key;


-- Now we need to create and populate a new job id field on SavedList and SavedSearch which
-- references the Job opp

alter table saved_list add column job_id bigint references salesforce_job_opp;
update saved_list set job_id =
                          (select id from salesforce_job_opp where salesforce_job_opp.sf_job_opp_id = saved_list.sf_job_opp_id)
where saved_list.sf_job_opp_id is not null;

alter table saved_search add column job_id bigint references salesforce_job_opp;
update saved_search set job_id =
                            (select id from salesforce_job_opp where salesforce_job_opp.sf_job_opp_id = saved_search.sf_job_opp_id)
where saved_search.sf_job_opp_id is not null;
