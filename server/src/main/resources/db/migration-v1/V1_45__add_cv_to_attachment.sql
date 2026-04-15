
alter table candidate_attachment add column cv boolean default false not null;

update candidate_attachment set cv = true where candidate_attachment.text_extract is not null;
