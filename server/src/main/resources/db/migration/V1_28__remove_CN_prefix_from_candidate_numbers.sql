
update candidate set candidate_number = SUBSTRING(candidate_number, 3, 100) where candidate_number like 'CN%';
