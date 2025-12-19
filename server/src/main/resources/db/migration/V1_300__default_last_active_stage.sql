
alter table candidate_opportunity
    alter column last_active_stage set default 'prospect';

update candidate_opportunity set last_active_stage = 'prospect' where last_active_stage is null;

