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

/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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

alter sequence candidate_file_id_seq rename to candidate_attachment_id_seq;

select setval('users_id_seq', 25000, false);
select setval('candidate_id_seq', 20000, false);
select setval('candidate_certification_id_seq', 20000, false);
select setval('candidate_language_id_seq', 25000, false);
select setval('candidate_education_id_seq', 10000, false);
select setval('candidate_occupation_id_seq', 20000, false);
select setval('candidate_job_experience_id_seq', 25000, false);
select setval('candidate_note_id_seq', 10000, false);
select setval('candidate_attachment_id_seq', 10000, false);
select setval('candidate_skill_id_seq', 25000, false);
select setval('country_id_seq', 10000, false);
select setval('education_level_id_seq', 10000, false);
select setval('education_major_id_seq', 10000, false);
select setval('industry_id_seq', 10000, false);
select setval('language_id_seq', 10000, false);
select setval('language_level_id_seq', 500, false);
select setval('nationality_id_seq', 10000, false);
select setval('occupation_id_seq', 10000, false);

















