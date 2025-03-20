-- Update single-language entries
update candidate_language
set language_id = 10121 -- Oromo
where trim(migration_language) in ('Afan Oromo', 'Oromo');

update candidate_language
set language_id = 343 -- Arabic
where trim(migration_language) in ('Arabic', 'العربية', 'عربي', 'العربيه', 'العربي', 'العربية الفصحى', 'العربية فقط', 'عربية فقط');

update candidate_language
set language_id = 347 -- Armenian (assuming "Aramic" is a typo or related)
where trim(migration_language) in ('Aramic', 'الارامية', 'New Aramaic', 'اللغة الآرامية لغة السيد المسيح');

update candidate_language
set language_id = 10007 -- Assyrian
where trim(migration_language) in ('Assyrian', 'assyrian', 'Assyrian (Mother tongue)', 'Assyrian (New-Aramic)', 'الاشورية', 'الاشورية( السريانية)');

update candidate_language
set language_id = 10101 -- Malay
where trim(migration_language) in ('Bahasa', 'Bahasa malay', 'Malay', 'الماليزية', 'المالاوية');

update candidate_language
set language_id = 10014 -- Bengali
where trim(migration_language) in ('Bangalie', 'Bengali');

update candidate_language
set language_id = 10024 -- Chaldean (assuming "Chaldian" is a typo or related)
where trim(migration_language) in ('Chaldian', 'الكلدانية', 'كلدانية');

update candidate_language
set language_id = 10104 -- Mandarin (default for "Chinese")
where trim(migration_language) in ('Chinese', 'Chinese language', 'الصينية', 'الصينيه');

update candidate_language
set language_id = 10031 -- Croatian
where trim(migration_language) in ('Croatian', 'الصربية الكرواتية');

update candidate_language
set language_id = 10034 -- Danish
where trim(migration_language) in ('Danish', 'Dansk', 'الدنماركية');

update candidate_language
set language_id = 9431 -- Dari
where trim(migration_language) = 'Dari';

update candidate_language
set language_id = 10038 -- Dutch
where trim(migration_language) in ('Dutch', 'Nederlands', 'الهولندية');

update candidate_language
set language_id = 342 -- English
where trim(migration_language) in ('English', 'الانجليزية', 'الأنكليزية', 'الإنجليزية وباللغة الام');

update candidate_language
set language_id = 9429 -- Farsi
where trim(migration_language) in ('Farsi', 'Persian', 'الفارسية');

update candidate_language
set language_id = 9419 -- Greek
where trim(migration_language) = 'Greek';

update candidate_language
set language_id = 10063 -- Hebrew
where trim(migration_language) in ('Hebrew', 'العبرية');

update candidate_language
set language_id = 10064 -- Hindi
where trim(migration_language) in ('Hindi', 'الهندية', 'الهنديه');

update candidate_language
set language_id = 10072 -- Indonesian
where trim(migration_language) = 'Indonesian';

update candidate_language
set language_id = 10075 -- Japanese
where trim(migration_language) in ('Japanese', 'اليابانية', 'اليابانيه');

update candidate_language
set language_id = 10080 -- Kashmiri
where trim(migration_language) = 'Kashmiri';

update candidate_language
set language_id = 10082 -- Kikuyu
where trim(migration_language) in ('Kikuyu', 'Kiembu'); -- "Kiembu" assumed related

update candidate_language
set language_id = 10083 -- Kinyarwanda
where trim(migration_language) = 'Kinyarwanda';

update candidate_language
set language_id = 10084 -- Kirundi
where trim(migration_language) = 'Kirundi';

update candidate_language
set language_id = 10146 -- Swahili
where trim(migration_language) in ('Kiswahili', 'Swahili', 'Swahilie');

update candidate_language
set language_id = 10085 -- Korean
where trim(migration_language) in ('Korena', 'الكوريه'); -- "Korena" assumed typo

update candidate_language
set language_id = 10089 -- Kurdish (general)
where trim(migration_language) in ('Kurdish', 'الكردية', 'الكوردية', 'الكوردية');

update candidate_language
set language_id = 10097 -- Luganda
where trim(migration_language) = 'LUGANDA';

update candidate_language
set language_id = 10102 -- Malayalam
where trim(migration_language) = 'Malayalam';

update candidate_language
set language_id = 10120 -- Norwegian
where trim(migration_language) in ('Norwegian', 'النرويجية', 'النرويجية - بوكمال');

update candidate_language
set language_id = 10131 -- Punjabi
where trim(migration_language) in ('Panjabi', 'Punjabi');

update candidate_language
set language_id = 10124 -- Papiamento
where trim(migration_language) = 'Papiamento';

update candidate_language
set language_id = 9432 -- Pashto
where trim(migration_language) = 'Pashto';

update candidate_language
set language_id = 10127 -- Polish
where trim(migration_language) in ('Polish', 'البولندية');

update candidate_language
set language_id = 10141 -- Sinhalese
where trim(migration_language) = 'Sinhala';

update candidate_language
set language_id = 10142 -- Slovak
where trim(migration_language) in ('Slovak', 'التشيكية'); -- Assuming "التشيكية" meant Slovak

update candidate_language
set language_id = 8789 -- Somali
where trim(migration_language) in ('Somalia', 'Somalian');

update candidate_language
set language_id = 10147 -- Swedish
where trim(migration_language) in ('Swedish', 'Sweedish', 'السويدية');

update candidate_language
set language_id = 10015 -- Berber (Tamazight is a Berber language)
where trim(migration_language) in ('Tamazight', 'الأمازيغية', 'الامازيغية');

update candidate_language
set language_id = 10152 -- Tamil
where trim(migration_language) = 'Tamil';

update candidate_language
set language_id = 10154 -- Thai
where trim(migration_language) = 'Thai';

update candidate_language
set language_id = 10157 -- Tigrinya
where trim(migration_language) in ('Tiginya', 'Tigrinya');

update candidate_language
set language_id = 8791 -- Turkish
where trim(migration_language) in ('Turkey', 'تركيه', 'اللغة التركية');

update candidate_language
set language_id = 10164 -- Ukrainian
where trim(migration_language) = 'Ukrainian';

update candidate_language
set language_id = 10165 -- Urdu
where trim(migration_language) in ('Urdu', 'Urdo', 'Ordo', 'الاوردو', 'أردو');

update candidate_language
set language_id = 10173 -- Yoruba
where trim(migration_language) = 'Yorba'; -- "Yorba" assumed typo

update candidate_language
set language_id = 344 -- French
where trim(migration_language) = 'French';

-- Handle multi-language entries by splitting into separate rows

-- 1. Assyrian -armenian-syriac-kurdish-languages -> Assyrian (10007), Armenian (347), Kurdish (10089)
-- Update original row to Assyrian
update candidate_language
set language_id = 10007 -- Assyrian
where trim(migration_language) = 'Assyrian -armenian-syriac-kurdish-languages';
-- Insert Armenian
insert into candidate_language (candidate_id, language_id, written_level_id, spoken_level_id)
select candidate_id, 347, written_level_id, spoken_level_id -- Armenian
from candidate_language
where trim(migration_language) = 'Assyrian -armenian-syriac-kurdish-languages';
-- Insert Kurdish
insert into candidate_language (candidate_id, language_id, written_level_id, spoken_level_id)
select candidate_id, 10089, written_level_id, spoken_level_id -- Kurdish
from candidate_language
where trim(migration_language) = 'Assyrian -armenian-syriac-kurdish-languages';

-- 2. Burmese and Rohingya -> Burmese (10019), Rohingya (0)
-- Update original row to Burmese
update candidate_language
set language_id = 10019 -- Burmese
where trim(migration_language) = 'Burmese and Rohingya';
-- Insert Rohingya (using 0 since not in language table)
insert into candidate_language (candidate_id, language_id, written_level_id, spoken_level_id)
select candidate_id, 0, written_level_id, spoken_level_id -- Rohingya
from candidate_language
where trim(migration_language) = 'Burmese and Rohingya';

-- 3. Zaghawa language and English language and Arabic language -> Zaghawa (0), English (342), Arabic (343)
-- Update original row to Zaghawa
update candidate_language
set language_id = 0 -- Zaghawa (not in table)
where trim(migration_language) = 'Zaghawa language and English language and Arabic language';
-- Insert English
insert into candidate_language (candidate_id, language_id, written_level_id, spoken_level_id)
select candidate_id, 342, written_level_id, spoken_level_id -- English
from candidate_language
where trim(migration_language) = 'Zaghawa language and English language and Arabic language';
-- Insert Arabic
insert into candidate_language (candidate_id, language_id, written_level_id, spoken_level_id)
select candidate_id, 343, written_level_id, spoken_level_id -- Arabic
from candidate_language
where trim(migration_language) = 'Zaghawa language and English language and Arabic language';

-- 4. العربية والانكليزية, العربية والأنكليزية -> Arabic (343), English (342)
-- Update original row to Arabic
update candidate_language
set language_id = 343 -- Arabic
where trim(migration_language) in ('العربية والانكليزية', 'العربية والأنكليزية');
-- Insert English
insert into candidate_language (candidate_id, language_id, written_level_id, spoken_level_id)
select candidate_id, 342, written_level_id, spoken_level_id -- English
from candidate_language
where trim(migration_language) in ('العربية والانكليزية', 'العربية والأنكليزية');

-- 5. العربيه و الفرنسيه والانكليزيه -> Arabic (343), French (344), English (342)
-- Update original row to Arabic
update candidate_language
set language_id = 343 -- Arabic
where trim(migration_language) = 'العربيه و الفرنسيه والانكليزيه';
-- Insert French
insert into candidate_language (candidate_id, language_id, written_level_id, spoken_level_id)
select candidate_id, 344, written_level_id, spoken_level_id -- French
from candidate_language
where trim(migration_language) = 'العربيه و الفرنسيه والانكليزيه';
-- Insert English
insert into candidate_language (candidate_id, language_id, written_level_id, spoken_level_id)
select candidate_id, 342, written_level_id, spoken_level_id -- English
from candidate_language
where trim(migration_language) = 'العربيه و الفرنسيه والانكليزيه';

-- 6. اللغة العربية .اللغة الإنكليزية -> Arabic (343), English (342)
-- Update original row to Arabic
update candidate_language
set language_id = 343 -- Arabic
where trim(migration_language) = 'اللغة العربية .اللغة الإنكليزية';
-- Insert English
insert into candidate_language (candidate_id, language_id, written_level_id, spoken_level_id)
select candidate_id, 342, written_level_id, spoken_level_id -- English
from candidate_language
where trim(migration_language) = 'اللغة العربية .اللغة الإنكليزية';

-- Other multi-language entries (using primary language)
update candidate_language
set language_id = 343 -- Arabic (first language listed)
where trim(migration_language) in ('الانجليزية،  الفرنسية. الروسية ،العربية', 'الكردية و العربية و التركية', 'اللغة الأنجليزية واللغة العربيه', 'العربيه والانكليزيه');