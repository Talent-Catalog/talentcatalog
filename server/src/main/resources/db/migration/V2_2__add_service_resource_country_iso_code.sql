alter table service_resource
    add column country_iso_code varchar(2);

create index if not exists sr_provider_service_country_available_idx
    on service_resource (provider, service_code, country_iso_code)
    where status = 'AVAILABLE';
