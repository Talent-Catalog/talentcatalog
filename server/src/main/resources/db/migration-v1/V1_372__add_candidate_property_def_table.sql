create table candidate_property_definition
(
    id                        bigserial not null primary key,
    name                      text not null unique,
    label                     text,
    definition                text,
    type                      text
);
