-- Update country name for Democratic Republic of the Congo
UPDATE country
SET name = 'Congo (the Democratic Republic of the)'
WHERE iso_code = 'CD';

-- Update country name for Republic of the Congo
UPDATE country
SET name = 'Congo (the)'
WHERE iso_code = 'CG';
