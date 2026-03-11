
alter table candidate add column shareable_notes text;
alter table candidate add column shareable_cv_attachment_id bigint references candidate_attachment;
alter table candidate add column shareable_doc_attachment_id bigint references candidate_attachment;
