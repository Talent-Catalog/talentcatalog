
-- Creates indexes to improve the performance of quick searching by name/number, email/phone, and external ID

-- for queries that search for users by lowercase username and exclude deleted users
create index users_username_active_status_idx on users (lower(username))
    where status <> 'deleted';

-- for queries that search for users by lowercase first name and exclude deleted users
create index users_lower_first_name_idx on users (lower(first_name))
    where status <> 'deleted';

-- for queries that search for users by lowercase last name and exclude deleted users
create index users_lower_last_name_idx on users (lower(last_name))
    where status <> 'deleted';

-- for queries that search for candidates by user ID, status, and country ID, excluding deleted candidates
create index candidate_user_status_country_active_idx on candidate(user_id, status, country_id)
    where status <> 'deleted';

-- for queries that search for candidates by candidate number, status, and country ID, excluding deleted candidates.
create index candidate_number_status_country_active_idx on candidate (candidate_number, status, country_id)
    where status <> 'deleted';

-- for queries that search for candidates by phone number, ignoring case and excluding deleted candidates
create index idx_candidate_phone_lower on candidate (LOWER(phone))
    where status <> 'deleted';

-- for queries that search for users based on email, ignoring case
create index idx_users_email_lower on users (LOWER(email));

-- for queries that filter candidates by status and country, excluding deleted candidates
create index idx_candidate_status_country on candidate (status, country_id)
    where status <> 'deleted';

-- for queries that search for candidates by external ID and country, ignoring case, including deletions
create index idx_candidate_external_id_country on candidate (LOWER(external_id), country_id);
