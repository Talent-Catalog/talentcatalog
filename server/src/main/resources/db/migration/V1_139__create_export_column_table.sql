
create table export_column
(
    id              bigserial not null primary key,
    saved_list_id   bigint references saved_list,
    saved_search_id bigint references saved_search,
    index           integer   not null,
    key             text,
    properties      text
);

alter table saved_list drop column export_columns;
alter table saved_search drop column export_columns;

