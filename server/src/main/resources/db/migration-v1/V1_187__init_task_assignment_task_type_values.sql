update task_assignment set task_type = (select task_type from task where id = task_assignment.task_id);
