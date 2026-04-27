alter table partner
    add column accepted_data_processing_agreement_id varchar(255),
    add column accepted_data_processing_agreement_date timestamptz,
    add column first_dpa_seen_date timestamptz;