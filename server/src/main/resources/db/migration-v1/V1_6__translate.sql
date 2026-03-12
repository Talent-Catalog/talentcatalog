
create table system_language
(
	id                      bigserial not null primary key,
	language                text not null,
	label                   text not null,
    status                  text not null default 'active',
	created_by              bigint references users,
	created_date            timestamptz,
	updated_by              bigint references users,
	updated_date            timestamptz
);

create table translation
(
	id                      bigserial not null primary key,
	object_id               bigint not null,
	object_type             text not null,
	language                text not null,
	value                   text not null,
	created_by              bigint references users,
	created_date            timestamptz,
	updated_by              bigint references users,
	updated_date            timestamptz
);

insert into system_language (language, label, created_date, created_by, status) values ('en', 'English', now(), 1, 'active');
insert into system_language (language, label, created_date, created_by, status) values ('ar','عربى', now(), 1, 'active');
