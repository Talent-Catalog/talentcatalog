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

insert into task (name, display_name, task_type, description, days_to_complete,
                  created_by, created_date)
values ('visaReturned','Have you received your passport back with your visa in it?', 'Task',
        'Please check the box below if you have received your visa back, if you have not yet received it do not worry it will come. When it does arrive, please return to task and check the box.',
        7,
        (select id from users where username = 'SystemAdmin'), now());

insert into task (name, display_name, task_type, description, days_to_complete, help_link,
                  created_by, created_date)
values ('collaborationAgreementSimple','Collaboration Agreement', 'Task', 'Please read the collaboration agreement and check the box to agree.',
        7, 'https://static1.squarespace.com/static/5dc0262432cd095744bf1bf2/t/6213172072e2bb66b18cd8b5/1645418273053/Candidate_TBB+Collaboration+Agreement+-+UPDATED+2021.pdf',
        (select id from users where username = 'SystemAdmin'), now());

insert into task (name, display_name, task_type, description, days_to_complete, candidate_answer_field,
                  created_by, created_date)
values ('unhcrStatus','What is your status with UNHCR?', 'QuestionTask', 'Please select which option best describes your status.',
        7, 'unhcrStatus',
        (select id from users where username = 'SystemAdmin'), now());

insert into task (name, display_name, task_type, description, days_to_complete,
                  created_by, created_date)
values ('livingRestrictions','Do you have any restrictions when it comes to living with other people?', 'QuestionTask', 'If you have any restrictions please describe in detail so we can do our best to try and accommodate.',
        7, (select id from users where username = 'SystemAdmin'), now());
