
-- To speed up look ups of candidate dependents by candidate id
create index candidate_dependant_candidate_id_idx on candidate_dependant(candidate_id);
