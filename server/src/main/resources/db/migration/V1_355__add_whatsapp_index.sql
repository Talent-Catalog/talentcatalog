create index if not exists idx_candidate_whatsapp_lower
    on candidate (LOWER(whatsapp))
    where status <> 'deleted';
