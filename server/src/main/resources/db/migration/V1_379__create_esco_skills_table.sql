create table if not exists skills_esco_en
(
    concepttype    text,
    concepturi     text primary key,
    skilltype      text,
    reuselevel     text,
    preferredlabel text,
    altlabels      text,
    hiddenlabels   text,
    status         text,
    modifieddate   text,
    scopenote      text,
    definition     text,
    inscheme       text,
    description    text
);
