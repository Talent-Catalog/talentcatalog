/*
 * Copyright (c) 2024 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

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
