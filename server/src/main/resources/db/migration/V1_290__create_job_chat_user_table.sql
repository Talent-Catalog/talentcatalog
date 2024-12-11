
create table job_chat_user
(
    job_chat_id bigint not null references job_chat,
    user_id bigint not null references users,
    last_read_post_id bigint references chat_post,
    primary key (job_chat_id, user_id)
);
