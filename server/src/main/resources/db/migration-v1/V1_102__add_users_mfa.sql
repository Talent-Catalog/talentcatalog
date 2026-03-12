
alter table users add column using_mfa boolean default false not null;
alter table users add column mfa_secret text;
