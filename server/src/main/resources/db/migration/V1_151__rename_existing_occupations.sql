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

update occupation set name = 'Auctioneer' where id = 8486;
update occupation set name = 'Medical and Dental Prosthetic Technicians' where id = 8618;
update occupation set name = 'Bricklayer' where id = 8495;
update occupation set name = 'Butcher' where id = 8500;
update occupation set name = 'Surveyor, land' where id = 8603;
update occupation set name = 'Biologist' where id = 8593;
update occupation set name = 'Pharmacologist' where id = 8678;
update occupation set name = 'Sociologist' where id = 8699;
update occupation set name = 'Optometrist' where id = 8649;
update occupation set name = 'Other Craft and Related Workers' where id = 8672;
update occupation set name = 'Geologist' where id = 8642;

update occupation set name = 'Inspector, building' where id = 8497;
insert into occupation (name, isco08_code) values ('Inspector, fire','3112');
