-- Add countries
insert into country (name) values ('Afghanistan');
insert into country (name) values ('Albania');
insert into country (name) values ('Algeria');
insert into country (name) values ('Andorra');
insert into country (name) values ('Angola');
insert into country (name) values ('Antigua and Barbuda');

-- Add nationalities
insert into nationality (name) values ('Afghan');
insert into nationality (name) values ('Albanian');
insert into nationality (name) values ('Algerian');
insert into nationality (name) values ('Andorran');
insert into nationality (name) values ('Angolan');
insert into nationality (name) values ('Antiguan or Barbudan');

-- Add languages
insert into language (name) values ('English');
insert into language (name) values ('Afar');
insert into language (name) values ('Afrikaans');
insert into language (name) values ('Akan');
insert into language (name) values ('Albanian');
insert into language (name) values ('Amharic');

-- Add language levels
insert into language_level (level, sort_order) values ('Elementary Proficiency', 0);
insert into language_level (level, sort_order) values ('Intermediate Proficiency', 1);
insert into language_level (level, sort_order) values ('Full Occupational Proficiency', 2);
insert into language_level (level, sort_order) values ('Native or Bilingual Proficiency', 3);

-- Add industries
insert into industry (name) values ('Accounting');
insert into industry (name) values ('Engineering');
insert into industry (name) values ('Information Technology');
insert into industry (name) values ('Legal');
insert into industry (name) values ('Medicine');
insert into industry (name) values ('Nursing');

-- Add occupation
insert into occupation (name) values ('Administrative Assistant');
insert into occupation (name) values ('Agricultural, fishery or related labourer');
insert into occupation (name) values ('Appraiser or auctioneer');
insert into occupation (name) values ('Bookkeeper');
insert into occupation (name) values ('Carpenter or Joiner');
insert into occupation (name) values ('Cartoonist');

-- Add candidateEducation level
insert into education_level (name, level) values ('No Formal Education', 0);
insert into education_level (name, level) values ('Primary School', 1);
insert into education_level (name, level) values ('Some Secondary School', 2);
insert into education_level (name, level) values ('Secondary School Degree or Equivalent', 3);
insert into education_level (name, level) values ('Some Vocational Training', 4);
insert into education_level (name, level) values ('Vocational Degree', 5);
insert into education_level (name, level) values ('Associate  Degree', 6);
insert into education_level (name, level) values ('Vocational Degree', 7);
insert into education_level (name, level) values ('Some University', 8);
insert into education_level (name, level) values ('Bachelors Degree', 9);
insert into education_level (name, level) values ('Doctoral Degree', 10);

insert into education_major (name) values ('Accounting');
insert into education_major (name) values ('Engineering');


-- sample translations
insert into translation (object_id, object_type, language, value) values (1, 'country', 'ar', 'Afghan-TODO');
insert into translation (object_id, object_type, language, value) values (2, 'country', 'ar', 'ألبانيا');
insert into translation (object_id, object_type, language, value) values (3, 'country', 'ar', 'Algeria-TODO');


