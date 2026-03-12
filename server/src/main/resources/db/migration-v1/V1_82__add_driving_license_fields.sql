
alter table candidate add column can_drive text;
alter table candidate add column driving_license text;
alter table candidate add column driving_license_exp date;
alter table candidate add column driving_license_country_id bigint references country;
