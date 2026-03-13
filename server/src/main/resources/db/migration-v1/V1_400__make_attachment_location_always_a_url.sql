update candidate_attachment
set location = concat('https://s3.us-east-1.amazonaws.com/files.tbbtalent.org/candidate/',
      CASE WHEN migrated = true THEN
               'migrated' ELSE
               (select candidate_number from candidate where id = candidate_attachment.candidate_id)
          END,
      '/',
      location
   )
where type = 'file' and location not like 'https:%';
