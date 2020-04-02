create table survey_type
(
id                      bigserial not null primary key,
name                    text not null,
status                  text not null default 'active'
);


alter table candidate add column survey_type_id bigint references survey_type;
alter table candidate add column survey_comment text;


insert into survey_type (name) values ('Information Session');
insert into survey_type (name) values ('Community centre posting - flyers');
insert into survey_type (name) values ('From a friend');
insert into survey_type (name) values ('Facebook');
insert into survey_type (name) values ('Facebook - through an organisation');
insert into survey_type (name) values ('Outreach worker');
insert into survey_type (name) values ('NGO');
insert into survey_type (name) values ('Other');

INSERT INTO translation(object_id,object_type,value,language) VALUES (1,'survey_type','جلسة معلومات','ar');
INSERT INTO translation(object_id,object_type,value,language) VALUES (2,'survey_type','منشورات من مراكز خدمات اجتماعية','ar');
INSERT INTO translation(object_id,object_type,value,language) VALUES (3,'survey_type','من خلال صديق','ar');
INSERT INTO translation(object_id,object_type,value,language) VALUES (4,'survey_type','فيسبوك- من خلال صديق','ar');
INSERT INTO translation(object_id,object_type,value,language) VALUES (5,'survey_type','فيسبوك- من خلال منظمة','ar');
INSERT INTO translation(object_id,object_type,value,language) VALUES (6,'survey_type','مستشار أو متطوع خدمات اجتماعية','ar');
INSERT INTO translation(object_id,object_type,value,language) VALUES (7,'survey_type','منظمات و جمعيات غير الحكومية','ar');
INSERT INTO translation(object_id,object_type,value,language) VALUES (8,'survey_type','آخر','ar');
