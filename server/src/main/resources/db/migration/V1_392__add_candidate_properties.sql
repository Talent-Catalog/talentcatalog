insert into candidate_property_definition (name, label, definition, type)
values
    ('TTH_IT$TRAVEL_INFO_COMMENT', 'TTH IT: Travel info comment',
        'Optional comment related to travel information (Italian Train to Hire).',NULL),
    ('TTH_IT$REFUGEE_STATUS_COMMENT', 'TTH IT: Refugee status comment',
        'Optional comment related to refugee status information (Italian Train to Hire).',NULL);

update candidate_property_definition
set name = 'TTH_IT$TRAVEL_DOC_TYPE',
    label = 'TTH IT: Travel document type',
    definition = 'Type of travel document, e.g., Passport, Refugee Travel Doc, National ID (Italian Train to Hire).'
where name = 'TRAVEL_DOC_TYPE';

update candidate_property_definition
set name = 'TTH_IT$TRAVEL_DOC_NUMBER',
    label = 'TTH IT: Travel document number',
    definition = 'Unique number printed on the travel document (Italian Train to Hire).'
where name = 'TRAVEL_DOC_NUMBER';

update candidate_property_definition
set name = 'TTH_IT$TRAVEL_DOC_ISSUED_BY',
    label = 'TTH IT: Travel document issued by',
    definition = 'Authority or country that issued the travel document (Italian Train to Hire).'
where name = 'TRAVEL_DOC_ISSUED_BY';

update candidate_property_definition
set name = 'TTH_IT$TRAVEL_DOC_ISSUE_DATE',
    label = 'TTH IT: Travel document issue date',
    definition = 'Date when the travel document was issued (Italian Train to Hire).'
where name = 'TRAVEL_DOC_ISSUE_DATE';

update candidate_property_definition
set name = 'TTH_IT$TRAVEL_DOC_EXPIRY_DATE',
    label = 'TTH IT: Travel document expiry date',
    definition = 'Date when the travel document expires (Italian Train to Hire).'
where name = 'TRAVEL_DOC_EXPIRY_DATE';

update candidate_property_definition
set name = 'TTH_IT$REFUGEE_STATUS',
    label = 'TTH IT: Refugee status',
    definition = 'Refugee status according to UNHCR or host country determination (Italian Train to Hire).'
where name = 'REFUGEE_STATUS';

update candidate_property_definition
set name = 'TTH_IT$REFUGEE_STATUS_EVIDENCE_DOCUMENT_TYPE',
    label = 'TTH IT: Refugee status evidence document type',
    definition = 'Type of Refugee Status evidence document (UNHCR Certificate, Host Country ID, etc.) (Italian Train to Hire).'
where name = 'REFUGEE_STATUS_EVIDENCE_DOCUMENT_TYPE';

update candidate_property_definition
set name = 'TTH_IT$REFUGEE_STATUS_EVIDENCE_DOCUMENT_NUMBER',
    label = 'TTH IT: Refugee status evidence document number',
    definition = 'Number printed on the Refugee Status evidence document (Italian Train to Hire).'
where name = 'REFUGEE_STATUS_EVIDENCE_DOCUMENT_NUMBER';

update candidate_property_definition
set name = 'TTH_IT$DEPENDANTS_INFO',
    label = 'TTH IT: Dependants info',
    definition = 'Information about dependants of a candidate (Italian Train to Hire).'
where name = 'DEPENDANTS_INFO';
