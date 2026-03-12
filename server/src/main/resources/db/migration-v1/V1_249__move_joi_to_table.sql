
create table job_opp_intake
(
    id                          bigserial not null primary key,
    recruitment_process         text,
    employer_cost_commitment    text,
    location                    text,
    location_details            text,
    salary_range                text,
    benefits                    text,
    language_requirements       text,
    employment_experience       text,
    education_requirements      text,
    skill_requirements          text,
    occupation_code             text,
    min_salary                  text
);
