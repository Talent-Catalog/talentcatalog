
alter table candidate add column videolink text;

update candidate set videolink=
 (select location from candidate_attachment where candidate_id = candidate.id
  and type = 'link' and lower(location) like '%hire.li%' fetch first row only)
where videolink is null;
