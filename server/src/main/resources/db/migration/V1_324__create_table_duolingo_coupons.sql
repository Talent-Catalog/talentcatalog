
-- Create the coupon table
CREATE TABLE duolingo_coupon (
                         id BIGSERIAL NOT NULL PRIMARY KEY,
                         coupon_code VARCHAR(255) UNIQUE NOT NULL,
                         candidate_id BIGINT,
                         expiration_date TIMESTAMP NOT NULL,
                         date_sent TIMESTAMP,
                         coupon_status VARCHAR(50) NOT NULL
                         );
