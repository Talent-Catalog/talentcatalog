update candidate set lang_assessment_score = null;
update candidate_exam set score = null;

alter table candidate add column ielts_score numeric;
