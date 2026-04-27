
create table link_preview
(
    id                      bigserial not null primary key,
    chat_post_id            bigint references chat_post,
    url                     text,
    title                   text,
    description             text,
    image_url               text,
    domain                  text,
    favicon_url             text
);
