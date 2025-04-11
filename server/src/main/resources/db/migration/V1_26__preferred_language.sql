
alter table candidate add column preferred_language text;

update candidate set preferred_language = 'en';
