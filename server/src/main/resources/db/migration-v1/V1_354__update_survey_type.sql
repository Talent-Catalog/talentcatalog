-- Mark old "Friend or colleague referral" as inactive
update survey_type
set status = 'inactive'
where name = 'From a friend';

insert into survey_type (name, status)
select 'Friend or colleague referral', 'active'
where not exists (
    select 1 from survey_type where name = 'Friend or colleague referral' and status = 'active'
);

update candidate
set survey_type_id = (
    select id from survey_type where name = 'Friend or colleague referral' and status = 'active'
)
where survey_type_id = (
    select id from survey_type where name = 'From a friend' and status = 'inactive'
);

-- Mark "Facebook - through an organisation" as inactive (instead of merging name)
update survey_type
set status = 'inactive'
where name = 'Facebook - through an organisation';

insert into survey_type (name, status)
select 'Facebook', 'active'
where not exists (
    select 1 from survey_type where name = 'Facebook'
);


-- Insert new values from HowHeardAboutUs enum that are not in the table
insert into survey_type (name, status) values ('Online Google search', 'active');
insert into survey_type (name, status) values ('Instagram', 'active');
insert into survey_type (name, status) values ('LinkedIn', 'active');
insert into survey_type (name, status) values ('X', 'active');
insert into survey_type (name, status) values ('WhatsApp', 'active');
insert into survey_type (name, status) values ('YouTube', 'active');
insert into survey_type (name, status) values ('University or school referral', 'active');
insert into survey_type (name, status) values ('Employer referral', 'active');
insert into survey_type (name, status) values ('Event or webinar', 'active');

