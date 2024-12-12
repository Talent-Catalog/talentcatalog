-- Syrian
update candidate set nationality_id = 9396 where nationality_id = 6863;
delete from nationality where id = 6863;
delete from translation where object_type = 'nationality' and object_id = 6863;

-- Iraqi
update candidate set nationality_id = 9317 where nationality_id = 265;
delete from nationality where id = 265;
delete from translation where object_type = 'nationality' and object_id = 265;

-- Palestinian
update candidate set nationality_id = 9362 where nationality_id = 7342;
delete from nationality where id = 7342;
delete from translation where object_type = 'nationality' and object_id = 7342;

-- Somali
update candidate set nationality_id = 9386 where nationality_id = 267;
delete from nationality where id = 267;
delete from translation where object_type = 'nationality' and object_id = 267;


-- Sudanese
update candidate set nationality_id = 9391 where nationality_id = 353;
delete from nationality where id = 353;
delete from translation where object_type = 'nationality' and object_id = 353;
