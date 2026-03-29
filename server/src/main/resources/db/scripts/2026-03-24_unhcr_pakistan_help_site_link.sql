-- Seed UNHCR help-site link for candidates in Pakistan.
-- Intended for environment-specific application by admins (not Flyway-managed).
-- This insert is idempotent for the same provider/service/resource/country combination.

insert into service_resource (
    provider,
    service_code,
    resource_code,
    status,
    country_iso_code,
    created_at
)
select
    'UNHCR',
    'HELP_SITE_LINK',
    'https://help.unhcr.org/pakistan/',
    'AVAILABLE',
    'PK',
    now()
where not EXISTS (
    select 1
    from service_resource
    where provider = 'UNHCR'
      and service_code = 'HELP_SITE_LINK'
      and resource_code = 'https://help.unhcr.org/pakistan/'
      and country_iso_code = 'PK'
);
