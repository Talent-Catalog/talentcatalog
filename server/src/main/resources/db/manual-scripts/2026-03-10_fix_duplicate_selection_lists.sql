-- Manual DB script
-- Ticket: #3135
-- Purpose:
--   1. Clean duplicate selection lists from saved_list
-- Execution:
--   Run manually at DB layer before release
-- Not part of Flyway runtime migrations

delete from saved_list
where id in (
    select id
    from (
             select id,
                    ROW_NUMBER() over (
                        partition by saved_search_id, created_by
                        order by created_date desc
                        ) as rn
             from saved_list
             where saved_search_id is not null
         ) t
    where t.rn > 1
);