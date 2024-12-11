alter table candidate_property drop column related_task_id;
alter table candidate_property add column related_task_assignment_id bigint references task_assignment;
