create unique index if not exists saved_list_unique_selection_idx
    on saved_list (saved_search_id, created_by)
    where saved_search_id is not null;