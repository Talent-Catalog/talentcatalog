
-- To speed up look ups of lowercase usernames
create index user_lower_username_idx on users (lower(username));

