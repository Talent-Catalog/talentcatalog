/*
 * Copyright (c) 2024 Talent Beyond Boundaries.
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

alter table candidate_visa_check add column destination_family text;
alter table candidate_visa_check add column destination_family_location text;

update candidate_visa_check cvc set destination_family =
                    (select family from candidate_destination cd 
                        join candidate c on cd.candidate_id = c.id 
                                   where c.id = cvc.candidate_id 
                                     and cd.country_id = cvc.country_id 
                                     and family is not null) 
                                where cvc.destination_family is null;

update candidate_visa_check cvc set destination_family_location =
                    (select location from candidate_destination cd 
                        join candidate c on cd.candidate_id = c.id 
                                     where c.id = cvc.candidate_id 
                                       and cd.country_id = cvc.country_id 
                                       and location is not null) 
                                where cvc.destination_family_location is null;