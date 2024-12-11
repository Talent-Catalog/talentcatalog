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

insert into candidate_visa_check (candidate_id, country_id, destination_family, destination_family_location)
select candidate_id, country_id, family, location from candidate_destination cd
where id not in (select cd.id from candidate_destination cd
                                       join candidate c on cd.candidate_id = c.id
                                       join candidate_visa_check cvc on c.id = cvc.candidate_id
                 where cvc.candidate_id = cd.candidate_id
                   and cvc.country_id = cd.country_id)
  and cd.family is not null;
