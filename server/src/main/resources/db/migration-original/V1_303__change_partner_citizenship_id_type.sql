
alter table candidate drop constraint candidate_partner_citizenship_id_fkey;
alter table candidate alter column partner_citizenship_id type text;
alter table candidate rename column partner_citizenship_id to partner_citizenship;
