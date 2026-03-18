
update candidate_visa_job_check cvjc set languages_required = '342' where english_threshold = 'Yes';
update candidate_visa_job_check cvjc set languages_threshold_met = 'Yes' where english_threshold = 'Yes';
