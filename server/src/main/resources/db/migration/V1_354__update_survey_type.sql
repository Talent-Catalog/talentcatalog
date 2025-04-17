update survey_type
set name = 'Friend or colleague referral'
where name = 'From a friend';

-- Merge 'Facebook - through an organisation' into 'Facebook'
update survey_type
set name = 'Facebook'
where name = 'Facebook - through an organisation';

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

-- Update candidate Facebook - through an organisation to Facebook
update candidate
set survey_type_id = 4
where survey_type_id = 5;

-- Remove through an organisation record
delete from survey_type
where id = 5;