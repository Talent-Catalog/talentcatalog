
update candidate set status = 'active' where status = 'approved';
update candidate set status = 'inactive' where status = 'rejected';
