UPDATE candidate_property_definition
SET type = NULL
WHERE type IN ('ENUM', 'TEXT', 'DATE');