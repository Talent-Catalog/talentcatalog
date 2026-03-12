
alter table candidate_opportunity add column relocating_dependant_ids text;

update candidate_opportunity co set relocating_dependant_ids =
    (select relocating_dependant_ids from candidate_visa_job_check cvjc
        join salesforce_job_opp jo on cvjc.job_opp_id = jo.id
        join candidate_visa_check cvc on cvjc.candidate_visa_check_id = cvc.id
        join candidate c on cvc.candidate_id = c.id
                                     where co.candidate_id = c.id and co.job_opp_id = cvjc.job_opp_id);
