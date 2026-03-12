-- Candidate coupon code
drop trigger if exists candidate_coupon_code_bump_version on candidate_coupon_code;
create trigger candidate_coupon_code_bump_version
    after insert or update or delete on candidate_coupon_code
    for each row execute function bump_candidate_ref_version();

-- Candidate exam
drop trigger if exists candidate_exam_bump_version on candidate_exam;
create trigger candidate_exam_bump_version
    after insert or update or delete on candidate_exam
    for each row execute function bump_candidate_ref_version();

-- Candidate property
drop trigger if exists candidate_property_bump_version on candidate_property;
create trigger candidate_property_bump_version
    after insert or update or delete on candidate_property
    for each row execute function bump_candidate_ref_version();

-- Candidate review item
drop trigger if exists candidate_review_item_bump_version on candidate_review_item;
create trigger candidate_review_item_bump_version
    after insert or update or delete on candidate_review_item
    for each row execute function bump_candidate_ref_version();

-- Candidate review item
drop trigger if exists task_assignment_bump_version on task_assignment;
create trigger task_assignment_bump_version
    after insert or update or delete on task_assignment
    for each row execute function bump_candidate_ref_version();
