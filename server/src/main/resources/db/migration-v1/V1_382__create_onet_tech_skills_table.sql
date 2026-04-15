create table if not exists skills_tech_onet_en
(
    id                  bigserial not null primary key,
    "O*NET-SOC CODE"    text,
    example             text,
    "Commodity Code"    text,
    "Commodity Title"   text,
    "Hot Technology"    text,
    "In Demand"         text
);
