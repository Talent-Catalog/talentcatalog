
alter table candidate_visa_check add column destination_family text;
alter table candidate_visa_check add column destination_family_location text;

update candidate_visa_check cvc set destination_family =
                    (select family from candidate_destination cd
                        join candidate c on cd.candidate_id = c.id
                                   where c.id = cvc.candidate_id
                                     and cd.country_id = cvc.country_id
                                     and family is not null)
                                where cvc.destination_family is null;

update candidate_visa_check cvc set destination_family_location =
                    (select location from candidate_destination cd
                        join candidate c on cd.candidate_id = c.id
                                     where c.id = cvc.candidate_id
                                       and cd.country_id = cvc.country_id
                                       and location is not null)
                                where cvc.destination_family_location is null;
