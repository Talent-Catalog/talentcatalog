alter table users
    add column email_verified boolean default false,
    add column email_verification_token varchar(255),
    add column email_verification_token_issued_time timestamp;
