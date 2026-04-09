
create table root_request
(
    id                   bigserial not null primary key,
    ip_address           text,
    partner_abbreviation text,
    query_string         text,
    request_url          text,
    timestamp            timestamp,
    utm_campaign         text,
    utm_content          text,
    utm_medium           text,
    utm_source           text,
    utm_term             text
);

-- To speed up look ups by ip address
create index ip_address_idx on root_request(ip_address);
