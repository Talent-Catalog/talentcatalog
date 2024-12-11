alter sequence candidate_file_id_seq rename to candidate_attachment_id_seq;

select setval('users_id_seq', 25000, false);
select setval('candidate_id_seq', 20000, false);
select setval('candidate_certification_id_seq', 20000, false);
select setval('candidate_language_id_seq', 25000, false);
select setval('candidate_education_id_seq', 10000, false);
select setval('candidate_occupation_id_seq', 20000, false);
select setval('candidate_job_experience_id_seq', 25000, false);
select setval('candidate_note_id_seq', 10000, false);
select setval('candidate_attachment_id_seq', 10000, false);
select setval('candidate_skill_id_seq', 25000, false);
select setval('country_id_seq', 10000, false);
select setval('education_level_id_seq', 10000, false);
select setval('education_major_id_seq', 10000, false);
select setval('industry_id_seq', 10000, false);
select setval('language_id_seq', 10000, false);
select setval('language_level_id_seq', 500, false);
select setval('nationality_id_seq', 10000, false);
select setval('occupation_id_seq', 10000, false);

















