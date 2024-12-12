
update candidate set unhcr_status =
                         case
                             when unhcr_registered = 'No' then 'NotRegistered'
                             when unhcr_registered = 'Yes' then 'RegisteredStatusUnknown'
                             when unhcr_registered = 'NoResponse' then 'NoResponse'
                             when unhcr_registered = 'Unsure' then 'Unsure'
                             end
where unhcr_registered is not null and
    (unhcr_status is null or unhcr_status in ('NoResponse', 'Unsure'));

