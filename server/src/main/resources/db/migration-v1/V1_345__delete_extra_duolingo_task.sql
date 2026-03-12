-- First, remove task assignments that reference task ID 60
delete from task_assignment WHERE task_id = 60;
delete from task where name = 'takeDuolingoTest';
