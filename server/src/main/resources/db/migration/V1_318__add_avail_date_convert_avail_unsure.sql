
alter table candidate add column avail_date date;

update candidate set avail_immediate = 'No',
                     avail_immediate_notes =
                         concat(avail_immediate_notes, '-Candidate is not sure about international opportunities at the moment')
where avail_immediate = 'Unsure';

