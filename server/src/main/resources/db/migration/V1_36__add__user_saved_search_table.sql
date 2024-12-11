create table user_saved_search
(
   user_id bigint not null references users,
   saved_search_id bigint not null references saved_search,
   primary key (user_id, saved_search_id)
);


-- Note that the primary key can serve as the index for user_id.
-- See https://stackoverflow.com/questions/3048154/indexes-and-multi-column-primary-keys
create index saved_search_id_idx on user_saved_search(saved_search_id);
