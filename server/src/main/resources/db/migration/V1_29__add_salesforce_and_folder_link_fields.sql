
alter table candidate add column sflink text;
alter table candidate add column folderlink text;

update candidate set sflink=
 (select location from candidate_attachment where candidate_id = candidate.id
  and type = 'link' and lower(name) like '%salesforce%' fetch first row only)
where sflink is null;

update candidate set folderlink=
 (select location from candidate_attachment where candidate_id = candidate.id
  and type = 'link' and lower(name) like '%folder%' fetch first row only)
where folderlink is null;
