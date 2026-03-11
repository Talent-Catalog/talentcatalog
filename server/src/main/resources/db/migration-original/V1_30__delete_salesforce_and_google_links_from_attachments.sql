
delete from candidate_attachment
where type = 'link' and (lower(name) like '%salesforce%' or lower(name) like '%folder%');
