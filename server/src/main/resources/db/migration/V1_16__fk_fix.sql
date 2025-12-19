
alter table candidate_education drop constraint candidate_education_major_id_fkey;

alter table candidate_education add constraint candidate_education_major_id_fkey
   foreign key (major_id) references education_major;
