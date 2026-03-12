
-- update FULL INTAKE COMPLETED BY
update candidate as c
set full_intake_completed_by = n.created_by
from candidate_note n
where (lower(n.title) LIKE '%full intake interview completed%'
    OR lower(title) LIKE '%full intake took place%' )
  and c.id = n.candidate_id;

-- update MINI INTAKE COMPLETED BY
update candidate as c
set mini_intake_completed_by = n.created_by
from candidate_note n
where (lower(n.title) LIKE '%mini intake interview completed%'
    OR lower(title) LIKE '%mini intake took place%' )
  and c.id = n.candidate_id;

-- update FULL INTAKE COMPLETED DATE
update candidate as c
set full_intake_completed_date = n.created_date
from candidate_note n
where (lower(n.title) LIKE '%full intake interview completed%'
    OR lower(title) LIKE '%full intake took place%' )
  and c.id = n.candidate_id;

-- update MINI INTAKE COMPLETED DATE
update candidate as c
set mini_intake_completed_date = n.created_date
from candidate_note n
where (lower(n.title) LIKE '%mini intake interview completed%'
    OR lower(title) LIKE '%mini intake took place%' )
  and c.id = n.candidate_id;
