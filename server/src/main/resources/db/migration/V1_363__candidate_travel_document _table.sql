CREATE TABLE candidate_travel_document
(
    id                           bigserial PRIMARY KEY,
    candidate_id                 bigint NOT NULL REFERENCES candidate (id),
    attachment_id                bigint REFERENCES candidate_attachment (id),
    place_of_birth               varchar(255),
    travel_doc_type              varchar(255),
    travel_doc_number            varchar(255),
    travel_doc_issuing_authority varchar(255),
    travel_doc_issue_date        date,
    travel_doc_expiry_date       date,
    refugee_status               varchar(50),
    created_by                   bigint REFERENCES users (id),
    created_date                 timestamp with time zone,
    updated_by                   bigint REFERENCES users (id),
    updated_date                 timestamp with time zone
);