alter table language_level add column cefr_level VARCHAR(20);

-- Update based on level values
update language_level set cefr_level = 'A2' where level = 10;
update language_level set cefr_level = 'B1' where level = 20;
update language_level set cefr_level = 'C1' where level = 30;
update language_level set cefr_level = 'C2' where level = 40;

-- Insert new rows
insert into language_level (name, status, level, cefr_level)
values
    ('Beginner Proficiency', 'active', 5, 'A1'),
    ('Advanced Proficiency', 'active', 25, 'B2');
