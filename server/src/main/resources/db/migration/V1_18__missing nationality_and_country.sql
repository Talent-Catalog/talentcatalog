
update candidate set nationality_id = (select id from nationality where name = 'Unknown') where nationality_id is null;
update candidate set country_id = (select id from country where name = 'Unknown') where country_id is null;
