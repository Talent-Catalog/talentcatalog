
alter table candidate add column migration_status text;
alter table saved_search add column include_draft_and_deleted boolean;

update candidate set country_id = 0 where country_id is null;
update candidate set nationality_id = 0 where nationality_id is null;

update users set email = username where username like '%@%' and email is null;

update candidate_attachment set created_by = null, created_date = null where type = 'link' and migrated = true;
