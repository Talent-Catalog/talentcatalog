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

insert into partner (name, abbreviation, default_source_partner, logo, partner_type, status,
                     registration_landing_page, registration_domain, website_url)
values ('Dignity for Children', 'Dignity', false, 'assets/images/dignityLogo.png', 'SourcePartner', 'active',
        null, 'dignity.displacedtalent.org', 'https://dignityforchildren.org/');

insert into partner (name, abbreviation, default_source_partner, logo, partner_type, status,
                     registration_landing_page, registration_domain, website_url)
values ('Catholic Relief Services', 'CRS', false, 'assets/images/crsLogo.png', 'SourcePartner', 'active',
        null, 'crs.displacedtalent.org', 'https://www.crs.org/');
