alter table candidate_attachment add column active boolean default true not null;
alter table candidate_attachment add column bucket text;
alter table candidate_attachment add column content_length bigint;
alter table candidate_attachment add column publicId text;
alter table candidate_attachment add column sha256_hex text;
alter table candidate_attachment add column storage_key text;


