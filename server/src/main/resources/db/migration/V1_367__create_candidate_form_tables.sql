create table candidate_form
(
    id                  bigserial   not null primary key,
    name                text,
    description         text,
    html_component_name text
);


create table candidate_form_instance
(
    candidate_id bigint      not null references candidate,
    form_id bigint      not null references candidate_form,
    created_date timestamptz not null,
    updated_date timestamptz,

--   Form and candidate uniquely define an instance. ie a Candidate only has one instance of a given form
    primary key (candidate_id, form_id)
);
