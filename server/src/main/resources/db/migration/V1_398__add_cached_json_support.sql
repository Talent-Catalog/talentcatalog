--Support for caching of candidate json

-- Candidate version is the authoritative “has anything changed?” signal.
-- We key Redis by (candidate_id, data_version) and validate DB cache rows against it.
alter table candidate
    add column if not exists data_version bigint not null default 0;

-- Stores one cached JSON blob per candidate.
-- Column name is "json" (renamed from json_data for symmetry with SQL alias).
create table if not exists candidate_json_cache (
    candidate_id    bigint      not null primary key,
    data_version    bigint      not null,
    json            jsonb       not null,
    computed_at     timestamptz not null default now()
);

-- Fast lookups by data_version (useful for audits/cleanup)
create index if not exists idx_candidate_json_cache_version
    on candidate_json_cache (data_version);

-- Trigger functions to bump candidate.data_version

-- For row-level triggers:
--   NEW exists for INSERT/UPDATE
--   OLD exists for UPDATE/DELETE
-- coalesce(NEW.id, OLD.id) correctly identifies the candidate row across all operations.
create or replace function bump_candidate_self_version()
    returns trigger language plpgsql as
$$
begin
    update candidate
    set data_version = data_version + 1
    where id = coalesce(new.id, old.id);

    return null; -- return value ignored for AFTER triggers
end;
$$;

drop trigger if exists candidate_bump_version on candidate;

create trigger candidate_bump_version
    after insert or update or delete on candidate
    for each row execute function bump_candidate_self_version();



-- Used for tables with a candidate_id foreign key.
-- coalesce(NEW.candidate_id, OLD.candidate_id) works for INSERT/UPDATE/DELETE.
create or replace function bump_candidate_ref_version()
    returns trigger language plpgsql as
$$
begin
    update candidate
    set data_version = data_version + 1
    where id = coalesce(new.candidate_id, old.candidate_id);

    return null;
end;
$$;


-- Candidate attachments
drop trigger if exists candidate_attachment_bump_version on candidate_attachment;
create trigger candidate_attachment_bump_version
    after insert or update or delete on candidate_attachment
    for each row execute function bump_candidate_ref_version();

-- Candidate languages
drop trigger if exists candidate_language_bump_version on candidate_language;
create trigger candidate_language_bump_version
    after insert or update or delete on candidate_language
    for each row execute function bump_candidate_ref_version();

-- Candidate occupations
drop trigger if exists candidate_occupation_bump_version on candidate_occupation;
create trigger candidate_occupation_bump_version
    after insert or update or delete on candidate_occupation
    for each row execute function bump_candidate_ref_version();

-- Candidate job experiences
drop trigger if exists candidate_job_experience_bump_version on candidate_job_experience;
create trigger candidate_job_experience_bump_version
    after insert or update or delete on candidate_job_experience
    for each row execute function bump_candidate_ref_version();

-- Candidate educations
drop trigger if exists candidate_education_bump_version on candidate_education;
create trigger candidate_education_bump_version
    after insert or update or delete on candidate_education
    for each row execute function bump_candidate_ref_version();

-- Candidate certifications
drop trigger if exists candidate_certification_bump_version on candidate_certification;
create trigger candidate_certification_bump_version
    after insert or update or delete on candidate_certification
    for each row execute function bump_candidate_ref_version();

-- Candidate notes
drop trigger if exists candidate_note_bump_version on candidate_note;
create trigger candidate_note_bump_version
    after insert or update or delete on candidate_note
    for each row execute function bump_candidate_ref_version();

-- Candidate visa checks
drop trigger if exists candidate_visa_check_bump_version on candidate_visa_check;
create trigger candidate_visa_check_bump_version
    after insert or update or delete on candidate_visa_check
    for each row execute function bump_candidate_ref_version();

-- Candidate visa job checks (if it also has candidate_id; if it links via visa_check_id instead, handle differently)
drop trigger if exists candidate_visa_job_check_bump_version on candidate_visa_job_check;
create trigger candidate_visa_job_check_bump_version
    after insert or update or delete on candidate_visa_job_check
    for each row execute function bump_candidate_ref_version();

-- Candidate opportunities
drop trigger if exists candidate_opportunity_bump_version on candidate_opportunity;
create trigger candidate_opportunity_bump_version
    after insert or update or delete on candidate_opportunity
    for each row execute function bump_candidate_ref_version();

-- Candidate destinations
drop trigger if exists candidate_destination_bump_version on candidate_destination;
create trigger candidate_destination_bump_version
    after insert or update or delete on candidate_destination
    for each row execute function bump_candidate_ref_version();

-- Candidate citizenships
drop trigger if exists candidate_citizenship_bump_version on candidate_citizenship;
create trigger candidate_citizenship_bump_version
    after insert or update or delete on candidate_citizenship
    for each row execute function bump_candidate_ref_version();

-- Candidate dependants
drop trigger if exists candidate_dependant_bump_version on candidate_dependant;
create trigger candidate_dependant_bump_version
    after insert or update or delete on candidate_dependant
    for each row execute function bump_candidate_ref_version();

-- Candidate skills
drop trigger if exists candidate_skill_bump_version on candidate_skill;
create trigger candidate_skill_bump_version
    after insert or update or delete on candidate_skill
    for each row execute function bump_candidate_ref_version();


-- Special case for Users table
-- When a user changes, bump all candidates that reference that user via candidate.user_id.
create or replace function bump_candidate_on_user_change()
    returns trigger language plpgsql as
$$
begin
    update candidate
    set data_version = data_version + 1
    where user_id = coalesce(new.id, old.id);

    return null;
end;
$$;

drop trigger if exists user_bump_candidate_version on users;

create trigger user_bump_candidate_version
    after insert or update or delete on users
    for each row execute function bump_candidate_on_user_change();
