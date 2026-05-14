-- create the duolingo_extra_fields table
create table duolingo_extra_fields
(
    id bigserial not null primary key,
    certificate_url varchar(255),
    interview_url varchar(255),
    verification_date varchar(255),
    percent_score int not null,
    scale int not null,
    literacy_subscore int not null,
    conversation_subscore int not null,
    comprehension_subscore int not null,
    production_subscore int not null,
    candidate_exam_id bigint not null references candidate_exam(id)
);

