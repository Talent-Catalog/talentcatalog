update task set task_type = 'Form' where task_type = 'FormTask';
update task set task_type = 'Question' where task_type = 'QuestionTask';
update task set task_type = 'Simple' where task_type = 'Task';
update task set task_type = 'Upload' where task_type = 'UploadTask';

update task_assignment set task_type = 'Form' where task_type = 'FormTask';
update task_assignment set task_type = 'Question' where task_type = 'QuestionTask';
update task_assignment set task_type = 'Simple' where task_type = 'Task';
update task_assignment set task_type = 'Upload' where task_type = 'UploadTask';
