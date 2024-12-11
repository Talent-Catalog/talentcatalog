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

alter table candidate drop column unhcr_status;
alter table candidate drop column unhcr_number;

alter table candidate rename column un_registered to unhcr_status;
alter table candidate rename column un_registration_number to unhcr_number;

alter table candidate rename column unrwa_registered to unrwa_status;
alter table candidate drop column unrwa_was_registered;

alter table candidate alter column unhcr_status type text;

update candidate set unhcr_status = 'RegisteredAsylum' where unhcr_status = 'true';
update candidate set unhcr_status = 'NotRegistered' where unhcr_status = 'false';
