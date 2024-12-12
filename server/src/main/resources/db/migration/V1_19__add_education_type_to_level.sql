
alter table education_level add column education_type text;

update education_level set education_type = 'Associate' where name = 'Associate Degree';
update education_level set education_type = 'Vocational' where name = 'Vocational Degree';
update education_level set education_type = 'Some University' where name = 'Bachelor''s Degree';
update education_level set education_type = 'Bachelor' where name = 'Bachelor''s Degree';
update education_level set education_type = 'Masters' where name = 'Master''s Degree';
update education_level set education_type = 'Doctoral' where name = 'Doctoral Degree';
