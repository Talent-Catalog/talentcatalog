
create table reaction_user
(
    reaction_id bigint not null references reaction,
    user_id bigint not null references users,
    primary key (reaction_id, user_id)
);

