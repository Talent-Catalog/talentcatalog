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

/*
 The purpose of this script is to remove/santise the dump file/database that contains
 information relating to people/other. It will leave several tables with information, however they're reference tables with generic, non-private information.
 This script should be run to create a 'clean/sanitised' dump file for used in the integration testing process.
 */
delete from user_job;
delete from translation;
delete from task_saved_list;
delete from task_assignment;
delete from help_link;
delete from candidate_saved_list;
delete from export_column;
delete from candidate_job_experience;
delete from candidate_occupation;
delete from candidate_skill;
delete from candidate_saved_list;
delete from candidate_review_item;
delete from candidate_note;
delete from candidate_language;
delete from candidate_job_experience;
delete from candidate_exam;
delete from candidate_education;
delete from candidate_destination;
delete from candidate_dependant;
delete from candidate_citizenship;
delete from candidate_certification;
delete from chat_post;
delete from job_chat;
update candidate_opportunity set job_opp_id = null;
update candidate_opportunity set sf_id = null;
delete from candidate_opportunity;
update candidate set shareable_cv_attachment_id = null;
delete from candidate_attachment;
delete from candidate;
delete from partner_job;
delete from job_opp_intake;
update saved_list set job_id = null;
update salesforce_job_opp set exclusion_list_id = null;
update salesforce_job_opp set submission_list_id = null;
delete from saved_list;
delete from salesforce_job_opp;
delete from saved_search;
update partner set employer_id = null;
delete from employer;
update system_language set created_by = null;
delete from users where id != 25000;
delete from partner where id != 1;
update users set password_enc = NULL, email = NULL, mfa_secret = NULL;
