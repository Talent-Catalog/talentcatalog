/*
 * Update duolingo_coupon table to include references and type changes.
 */
alter table duolingo_coupon
    alter column expiration_date type timestamptz,
    alter column date_sent type timestamptz,
    add constraint fk_candidate foreign key (candidate_id) references candidate(id);
