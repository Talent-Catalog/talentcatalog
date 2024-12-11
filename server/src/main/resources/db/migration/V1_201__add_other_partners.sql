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

insert into partner (name, default_source_partner, logo, partner_type, status,
                     registration_landing_page, registration_url, website_url)
values ('UNHCR', false, 'assets/images/unhcrLogo.png', 'SourcePartner', 'active',
        null, 'unhcrtalent.org', 'https://www.unhcr.org/');

insert into partner (name, default_source_partner, logo, partner_type, status,
                     registration_landing_page, registration_url, website_url)
values ('HIAS', false, 'assets/images/hiasLogo.png', 'SourcePartner', 'active',
        null, 'hiastalent.org', 'https://www.hias.org/');

insert into partner (name, default_source_partner, logo, partner_type, status,
                     registration_landing_page, registration_url, website_url)
values ('IOM', false, 'assets/images/iomLogo.png', 'SourcePartner', 'active',
        null, 'iomtalent.org', 'https://www.iom.int/');

insert into partner (name, default_source_partner, logo, partner_type, status,
                     registration_landing_page, registration_url, website_url)
values ('Refuge Point', false, 'assets/images/refugePointLogo.png', 'SourcePartner', 'active',
        null, 'refugepointtalent.org', 'https://www.refugepoint.org/');
