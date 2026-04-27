
alter table candidate_shortlist_item rename to candidate_review_item;
alter table candidate_review_item rename column shortlist_status to review_status;
alter sequence candidate_shortlist_item_id_seq rename to candidate_review_item_id_seq;

