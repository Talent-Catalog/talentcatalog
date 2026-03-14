alter table candidate add column relocated_address text;
alter table candidate add column relocated_city text;
alter table candidate add column relocated_state text;
alter table candidate add column relocated_country_id bigint references country;
