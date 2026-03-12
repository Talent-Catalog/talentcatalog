update candidate_education
set major_id = candidate.migration_education_major_id
from candidate
where candidate_education.candidate_id = candidate.id;

--
alter table candidate drop column migration_education_major_id;
