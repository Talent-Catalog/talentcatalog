alter table service_resource
    add column resource_type varchar(50) not null default 'UNIQUE';

update service_resource
set resource_type = 'SHARED'
where provider = 'UNHCR';
