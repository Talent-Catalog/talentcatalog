alter table candidate alter column returned_home type text;
update candidate set returned_home = null where returned_home is not null;
