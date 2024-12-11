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

alter table candidate_opportunity add column relocating_dependant_ids text;

update candidate_opportunity co set relocating_dependant_ids =
    (select relocating_dependant_ids from candidate_visa_job_check cvjc
        join salesforce_job_opp jo on cvjc.job_opp_id = jo.id
        join candidate_visa_check cvc on cvjc.candidate_visa_check_id = cvc.id
        join candidate c on cvc.candidate_id = c.id
                                     where co.candidate_id = c.id and co.job_opp_id = cvjc.job_opp_id);
