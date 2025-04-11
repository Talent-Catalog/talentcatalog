
alter table candidate add column partner_registered text;
alter table candidate add column partner_candidate_id bigint references candidate;
alter table candidate add column partner_edu_level_id bigint references education_level;
alter table candidate add column partner_occupation_id bigint references occupation;
alter table candidate add column partner_english text;
alter table candidate add column partner_english_level_id bigint references language_level;
alter table candidate add column partner_ielts text;
alter table candidate add column partner_ielts_score text;
alter table candidate add column partner_citizenship_id bigint references nationality;
