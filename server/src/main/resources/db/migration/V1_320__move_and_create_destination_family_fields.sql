
insert into candidate_visa_check (candidate_id, country_id, destination_family, destination_family_location)
select candidate_id, country_id, family, location from candidate_destination cd
where id not in (select cd.id from candidate_destination cd
                                       join candidate c on cd.candidate_id = c.id
                                       join candidate_visa_check cvc on c.id = cvc.candidate_id
                 where cvc.candidate_id = cd.candidate_id
                   and cvc.country_id = cd.country_id)
  and cd.family is not null;
