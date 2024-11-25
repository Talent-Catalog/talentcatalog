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

-- Create the coupon table
CREATE TABLE duolingo_coupon (
                         id BIGINT DEFAULT nextval('coupon_id_seq') PRIMARY KEY,
                         coupon_code VARCHAR(255) UNIQUE NOT NULL,
                         candidate_id BIGINT,
                         expiration_date TIMESTAMP NOT NULL,
                         date_sent TIMESTAMP,
                         coupon_status VARCHAR(50) NOT NULL,
                         test_status VARCHAR(50)
                    );


SELECT setval('coupon_id_seq', COALESCE((SELECT MAX(id) FROM duolingo_coupon), 1), false);
