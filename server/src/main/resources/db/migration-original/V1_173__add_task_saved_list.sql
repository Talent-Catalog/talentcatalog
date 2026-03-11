
-- Tasks associated with saved lists
-- Note that this is a unidirectional many to many relationship, so we don't need an index
-- on task_id (don't need to efficiently show all saved lists associated with a task). We just to
-- be able to efficiently retrieve all tasks associated with a saved list - and the primary key
-- can provide that index. See https://stackoverflow.com/questions/3048154/indexes-and-multi-column-primary-keys
-- See also https://thorben-janssen.com/ultimate-guide-association-mappings-jpa-hibernate/#Unidirectional_Many-to-Many_Associations
create table task_saved_list
(
    task_id bigint not null references task,
    saved_list_id bigint not null references saved_list,
    primary key (saved_list_id, task_id)
);
