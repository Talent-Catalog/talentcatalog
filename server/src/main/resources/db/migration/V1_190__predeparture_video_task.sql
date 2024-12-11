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
values ('predepartureVideo','Provide link to your pre-departure video/s', 'QuestionTask',
        'The IOM have asked if candidates can do some pre-departure filming (on your mobile phones) that could then be collected and put in a film they are putting together about your arrivals. If you have some videos you would like to put forward, please upload them to a shareable drive (Dropbox, Google Drive etc) and provide the shareable link below.',
        7, (select id from users where username = 'SystemAdmin'), now());
