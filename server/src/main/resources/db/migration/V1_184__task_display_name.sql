alter table task add column display_name text;

update task set display_name = name;

create unique index task_name_uindex on task (name);
