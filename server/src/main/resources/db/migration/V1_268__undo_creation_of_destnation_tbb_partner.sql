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

-- Remove previously created special extra default_destination_partner
-- Instead we will support the same partner being both default source and destination partner

-- First replace all occurrences of that new partner with default source partner
update salesforce_job_opp set recruiter_partner_id =
        (select id from partner where default_source_partner = true)
    where recruiter_partner_id = (select id from partner where default_destination_partner = true);

update users set partner_id = (select id from partner where default_source_partner = true)
where partner_id = (select id from partner where default_destination_partner = true);

-- Now delete the partner
delete from partner where default_destination_partner = true;

-- Finally make the default source partner also the default destination partner
update partner set default_destination_partner = true where default_source_partner = true;
