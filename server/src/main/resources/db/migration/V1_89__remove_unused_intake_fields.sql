
alter table candidate rename column work_abroad_loc to work_abroad_loc_id;

alter table candidate drop column returned_home_notes;
alter table candidate drop column family_health_concern;
alter table candidate drop column family_health_concern_notes;
alter table candidate drop column children;
alter table candidate drop column children_age;
