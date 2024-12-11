
alter table job_chat add column candidate_id bigint references candidate;

-- Indexes
create index job_chat_candidate_id_idx on job_chat(candidate_id);
create index job_chat_job_id_idx on job_chat(job_id);
create index job_chat_source_partner_id_idx on job_chat(source_partner_id);
create index job_chat_type_idx on job_chat(type);

-- Populate new field with candidate id taken from candidate associated with candidate opp
-- associated with existing candidate_opp_id field
update job_chat
set candidate_id = opp.candidate_id, job_id = opp.job_opp_id
from candidate_opportunity opp
where candidate_opp_id is not null and candidate_opp_id = opp.id;

-- Remove all candidate prospect chats they will be regenerated, with one prospect chat being
-- shared across opportunities
delete from job_chat_user where (select type from job_chat where job_chat_id = id) = 'CandidateProspect';
delete from chat_post where (select type from job_chat where job_chat_id = id) = 'CandidateProspect';
delete from job_chat where type = 'CandidateProspect';

