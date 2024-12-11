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


create table partner
(
    default_source_partner    boolean not null default false,
    id                        bigserial not null primary key,
    logo                      text,
    name                      text not null,
    partner_type              text,
    status                    text not null,
    registration_landing_page text,
    registration_url          text,
    website_url               text
);

create table partner_source_country
(
    partner_id bigint not null references partner,
    country_id bigint references country,
    primary key (partner_id, country_id)
);

insert into partner (name, default_source_partner, logo, partner_type, status,
                     registration_landing_page, registration_url, website_url)
values ('Talent Beyond Boundaries', true, 'assets/images/tbbLogo.png', 'SourcePartner', 'active',
        'https://www.talentbeyondboundaries.org/talentcatalog/', 'tbbtalent.org',
        'https://talentbeyondboundaries.org');

alter table users add column partner_id bigint references partner;

update users set partner_id = (select id from partner where default_source_partner = true);

alter table users alter column partner_id set not null;
