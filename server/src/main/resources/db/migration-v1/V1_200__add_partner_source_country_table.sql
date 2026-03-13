

create table partner
(
    default_source_partner    boolean not null default false,
    id                        bigserial not null primary key,
    logo                      text,
    name                      text not null,
    partner_type              text,
    status                    text not null,
    registration_landing_page text,
    registration_url          text,
    website_url               text
);

create table partner_source_country
(
    partner_id bigint not null references partner,
    country_id bigint references country,
    primary key (partner_id, country_id)
);

insert into partner (name, default_source_partner, logo, partner_type, status,
                     registration_landing_page, registration_url, website_url)
values ('Talent Beyond Boundaries', true, 'assets/images/tbbLogo.png', 'SourcePartner', 'active',
        'https://www.talentbeyondboundaries.org/talentcatalog/', 'tbbtalent.org',
        'https://talentbeyondboundaries.org');

alter table users add column partner_id bigint references partner;

update users set partner_id = (select id from partner where default_source_partner = true);

alter table users alter column partner_id set not null;
