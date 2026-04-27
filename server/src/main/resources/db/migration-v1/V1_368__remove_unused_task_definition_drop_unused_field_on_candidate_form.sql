-- Remove old task definition
delete from task where name = 'candidateTravelDocumentUpload';

-- Remove old candidate demo form and related table then change definition
delete from candidate_form_instance where 'MyFirstForm' =
        (select name from candidate_form where candidate_form.id = form_id);
delete from candidate_form where name = 'MyFirstForm';

-- Change candidate_form definition
alter table candidate_form drop column html_component_name;
alter table candidate_form add constraint unique_name unique(name);
create index candidate_form_name_idx on candidate_form(name);

-- Now add the demo form back in
insert into candidate_form (name, description)
values ('MyFirstForm',
        'Enter city where you live and the colour of your hair');
