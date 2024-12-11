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

alter table partner add column notification_email text;
alter table partner add column default_partner_ref boolean default false;

alter table candidate add column partner_ref text;

-- Set all default partners (there is only one! - TBB) to also default the partner reference
update partner set default_partner_ref = true where default_source_partner = true;

--Set the partner_ref to candidate_number of all candidates with a partner using default partner ref's
update candidate set partner_ref = candidate_number where true =
    (select default_partner_ref from candidate c
        join users u on c.user_id = u.id
        join partner p on u.partner_id = p.id
    where c.candidate_number = candidate.candidate_number);
