
create table user_source_country
(
    user_id bigint not null references users,
    country_id bigint references country,
    primary key (user_id, country_id)
);
