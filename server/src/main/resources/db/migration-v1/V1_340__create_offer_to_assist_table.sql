create table offer_to_assist
(
    id bigserial not null primary key,

    created_by   bigint not null references users,
    created_date timestamptz not null,
    updated_by   bigint references users,
    updated_date timestamptz,

    additional_notes text,
    partner_id bigint not null references partner,
    public_id varchar(22),
    reason text
);

create table candidate_coupon_code
(
    id bigserial not null primary key,
    offer_to_assist_id bigint references offer_to_assist,
    candidate_id bigint not null references candidate,
    coupon_code text
);

-- Public ID Indexes
create index candidate_public_id_idx on candidate(public_id);
create index offer_to_assist_public_id_idx on offer_to_assist(public_id);
create index saved_list_public_id_idx on saved_list(public_id);
create index saved_search_public_id_idx on saved_search(public_id);

