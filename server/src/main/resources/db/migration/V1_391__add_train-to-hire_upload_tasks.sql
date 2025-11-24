/*
 * Copyright (c) 2025 Talent Catalog.
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

-- TRAVEL DOCUMENT UPLOAD TASKS
-- Relocating candidate travel document
insert into task (name, display_name, task_type, description, days_to_complete, created_by,
                  created_date)
values ('italyTrainToHireTravelDocUpload', 'Travel Document Upload', 'UploadTask', 'placeholder',
        7, (select id from users where username = 'SystemAdmin'), now());

-- Relocating candidate dependant travel doc upload
insert into task (name, display_name, task_type, description, days_to_complete, created_by,
                  created_date)
values ('italyTrainToHireDependantTravelDocUpload', 'Dependant Travel Document Upload', 'UploadTask',
        'placeholder', 7, (select id from users where username = 'SystemAdmin'), now());

-- REFUGEE STATUS DOCUMENT UPLOAD TASKS
-- Relocating candidate refugee status document
insert into task (name, display_name, task_type, description, days_to_complete, created_by,
                  created_date)
values ('italyTrainToHireRefugeeStatusDocUpload', 'Refugee Status Document Upload', 'UploadTask',
        'placeholder', 7, (select id from users where username = 'SystemAdmin'), now());

-- Relocating candidate dependant refugee status document
insert into task (name, display_name, task_type, description, days_to_complete, created_by,
                  created_date)
values ('italyTrainToHireDependantRefugeeStatusDocUpload', 'Dependant Refugee Status Document Upload',
        'UploadTask', 'placeholder', 7, (select id from users where username = 'SystemAdmin'), now());