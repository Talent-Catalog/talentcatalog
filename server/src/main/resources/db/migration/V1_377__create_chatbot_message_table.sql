-- Create chatbot_message table
create table chatbot_message
(
    id          UUID                        not null primary key,
    session_id  UUID                        not null,
    question_id UUID                        not null,
    sender      varchar(255)                not null,
    message     text                        not null,
    timestamp   timestamp with time zone    not null
);

-- Create enum constraint for sender field
alter table chatbot_message
    add constraint chatbot_message_sender_check
        check (sender in ('user', 'bot'));

-- Index for efficient session queries
create index chatbot_message_session_id_idx on chatbot_message (session_id);

-- Index for efficient question_id lookups (to link questions with answers)
create index chatbot_message_question_id_idx on chatbot_message (question_id);

