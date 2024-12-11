
alter table candidate_attachment add column upload_type text;

update candidate_attachment set upload_type = 'cv' where cv = true;
update candidate_attachment set upload_type = 'other' where cv = false;


