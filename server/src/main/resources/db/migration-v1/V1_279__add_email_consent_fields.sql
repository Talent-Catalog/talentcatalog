
alter table candidate add column contact_consent_registration boolean default false not null;
alter table candidate add column contact_consent_partners boolean default false not null;
