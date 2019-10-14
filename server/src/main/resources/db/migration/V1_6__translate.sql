create table system_language
(
id                      bigserial not null primary key,
language                text not null,
label                   text not null,
created_by              bigint references users,
created_date            timestamp,
updated_by              bigint references users,
updated_date            timestamp
);

create table translation
(
id                      bigserial not null primary key,
object_id               bigint not null,
object_type             text not null,
language                text not null,
created_by              bigint references users,
created_date            timestamp,
updated_by              bigint references users,
updated_date            timestamp
);
