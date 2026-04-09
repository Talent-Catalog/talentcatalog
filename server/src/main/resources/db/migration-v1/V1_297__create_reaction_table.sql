
create table reaction
(
    id                      bigserial not null primary key,
    chat_post_id            bigint references chat_post,
    emoji                   text
);
