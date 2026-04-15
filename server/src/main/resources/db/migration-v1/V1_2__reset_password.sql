
alter table users add column reset_token varchar(100);
alter table users add column reset_token_issued_date timestamp;
alter table users add column password_updated_date timestamp;
