
create table candidate_shortlist_item
(
id                      bigserial not null primary key,
candidate_id            bigint not null references candidate,
saved_search_id         bigint not null references saved_search,
shortlist_status        text not null,
comment                 text,
created_by              bigint references users,
created_date            timestamptz,
updated_by              bigint references users,
updated_date            timestamptz
);


