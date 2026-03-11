
alter table candidate rename column nationality_id to nationalityold_id;
alter table candidate rename constraint candidate_nationality_id_fkey to candidate_nationalityold_id_fkey;
alter table candidate add column nationality_id bigint references country;
update candidate as c set nationality_id = j.country_id from country_nationality_join as j where j.nationality_id = nationalityold_id;

alter table candidate_citizenship drop constraint candidate_citizenship_nationality_id_fkey;
update candidate_citizenship as cc set nationality_id = j.country_id from country_nationality_join as j where j.nationality_id = cc.nationality_id;
alter table candidate_citizenship add constraint candidate_citizenship_nationality_id_fkey foreign key (nationality_id) REFERENCES country;
