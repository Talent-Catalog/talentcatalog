-- Update single-language entries
update candidate_language
set language_id = 10121 -- Oromo
where migration_language in ('Afan Oromo', 'Oromo');

update candidate_language
set language_id = 343 -- Arabic
where migration_language in ('Arabic', 'العربية', 'عربي', 'العربيه', 'العربي', 'العربية الفصحى', 'العربية فقط', 'عربية فقط');

update candidate_language
set language_id = 347 -- Armenian (assuming "Aramic" is a typo or related)
where migration_language in ('Aramic', 'الارامية', 'New Aramaic', 'اللغة الآرامية لغة السيد المسيح');

update candidate_language
set language_id = 10007 -- Assyrian
where migration_language in ('Assyrian', 'assyrian', 'Assyrian (Mother tongue)', 'Assyrian (New-Aramic)', 'الاشورية', 'الاشورية( السريانية)');

update candidate_language
set language_id = 10101 -- Malay
where migration_language in ('Bahasa', 'Bahasa malay', 'Malay', 'الماليزية', 'المالاوية');

update candidate_language
set language_id = 10014 -- Bengali
where migration_language in ('Bangalie', 'Bengali');

update candidate_language
set language_id = 10024 -- Chaldean (assuming "Chaldian" is a typo or related)
where migration_language in ('Chaldian', 'الكلدانية', 'كلدانية');

update candidate_language
set language_id = 10104 -- Mandarin (default for "Chinese")
where migration_language in ('Chinese', 'Chinese language', 'الصينية', 'الصينيه');

update candidate_language
set language_id = 10031 -- Croatian
where migration_language in ('Croatian', 'الصربية الكرواتية');

update candidate_language
set language_id = 10034 -- Danish
where migration_language in ('Danish', 'Dansk', 'الدنماركية');

update candidate_language
set language_id = 9431 -- Dari
where migration_language = 'Dari';

update candidate_language
set language_id = 10038 -- Dutch
where migration_language in ('Dutch', 'Nederlands', 'الهولندية');

update candidate_language
set language_id = 342 -- English
where migration_language in ('English', 'الانجليزية', 'الأنكليزية', 'الإنجليزية وباللغة الام');

update candidate_language
set language_id = 9429 -- Farsi
where migration_language in ('Farsi', 'Persian', 'الفارسية');

update candidate_language
set language_id = 9419 -- Greek
where migration_language = 'Greek';

update candidate_language
set language_id = 10063 -- Hebrew
where migration_language in ('Hebrew', 'العبرية');

update candidate_language
set language_id = 10064 -- Hindi
where migration_language in ('Hindi', 'الهندية', 'الهنديه');

update candidate_language
set language_id = 10072 -- Indonesian
where migration_language = 'Indonesian';

update candidate_language
set language_id = 10075 -- Japanese
where migration_language in ('Japanese', 'اليابانية', 'اليابانيه');

update candidate_language
set language_id = 10080 -- Kashmiri
where migration_language = 'Kashmiri';

update candidate_language
set language_id = 10082 -- Kikuyu
where migration_language in ('Kikuyu', 'Kiembu'); -- "Kiembu" assumed related

update candidate_language
set language_id = 10083 -- Kinyarwanda
where migration_language = 'Kinyarwanda';

update candidate_language
set language_id = 10084 -- Kirundi
where migration_language = 'Kirundi';

update candidate_language
set language_id = 10146 -- Swahili
where migration_language in ('Kiswahili', 'Swahili', 'Swahilie');

update candidate_language
set language_id = 10085 -- Korean
where migration_language in ('Korena', 'الكوريه'); -- "Korena" assumed typo

update candidate_language
set language_id = 10089 -- Kurdish (general)
where migration_language in ('Kurdish', 'الكردية', 'الكوردية', 'الكوردية');

update candidate_language
set language_id = 10097 -- Luganda
where migration_language = 'LUGANDA';

update candidate_language
set language_id = 10102 -- Malayalam
where migration_language = 'Malayalam';

update candidate_language
set language_id = 10120 -- Norwegian
where migration_language in ('Norwegian', 'النرويجية', 'النرويجية - بوكمال');

update candidate_language
set language_id = 10131 -- Punjabi
where migration_language in ('Panjabi', 'Punjabi');

update candidate_language
set language_id = 10124 -- Papiamento
where migration_language = 'Papiamento';

update candidate_language
set language_id = 9432 -- Pashto
where migration_language = 'Pashto';

update candidate_language
set language_id = 10127 -- Polish
where migration_language in ('Polish', 'البولندية');

update candidate_language
set language_id = 10141 -- Sinhalese
where migration_language = 'Sinhala';

update candidate_language
set language_id = 10142 -- Slovak
where migration_language in ('Slovak', 'التشيكية'); -- Assuming "التشيكية" meant Slovak

update candidate_language
set language_id = 8789 -- Somali
where migration_language in ('Somalia', 'Somalian');

update candidate_language
set language_id = 10147 -- Swedish
where migration_language in ('Swedish', 'Sweedish', 'السويدية');

update candidate_language
set language_id = 10015 -- Berber (Tamazight is a Berber language)
where migration_language in ('Tamazight', 'الأمازيغية', 'الامازيغية');

update candidate_language
set language_id = 10152 -- Tamil
where migration_language = 'Tamil';

update candidate_language
set language_id = 10154 -- Thai
where migration_language = 'Thai';

update candidate_language
set language_id = 10157 -- Tigrinya
where migration_language in ('Tiginya', 'Tigrinya');

update candidate_language
set language_id = 8791 -- Turkish
where migration_language in ('Turkey', 'تركيه', 'اللغة التركية');

update candidate_language
set language_id = 10164 -- Ukrainian
where migration_language = 'Ukrainian';

update candidate_language
set language_id = 10165 -- Urdu
where migration_language in ('Urdu', 'Urdo', 'الاوردو', 'أردو');

update candidate_language
set language_id = 10173 -- Yoruba
where migration_language = 'Yorba'; -- "Yorba" assumed typo

-- Handle multi-language entries by splitting into separate rows

-- 1. Assyrian -armenian-syriac-kurdish-languages -> Assyrian (10007), Armenian (347), Kurdish (10089)
-- Update original row to Assyrian
update candidate_language
set language_id = 10007 -- Assyrian
where migration_language = 'Assyrian -armenian-syriac-kurdish-languages';
-- Insert Armenian
insert into candidate_language (candidate_id, language_id, written_level_id, spoken_level_id)
select candidate_id, 347, written_level_id, spoken_level_id -- Armenian
from candidate_language
where migration_language = 'Assyrian -armenian-syriac-kurdish-languages';
-- Insert Kurdish
insert into candidate_language (candidate_id, language_id, written_level_id, spoken_level_id)
select candidate_id, 10089, written_level_id, spoken_level_id -- Kurdish
from candidate_language
where migration_language = 'Assyrian -armenian-syriac-kurdish-languages';

-- 2. Burmese and Rohingya -> Burmese (10019), Rohingya (0)
-- Update original row to Burmese
update candidate_language
set language_id = 10019 -- Burmese
where migration_language = 'Burmese and Rohingya';
-- Insert Rohingya (in this flyway I am using 0 since not in language table later then we can update it)
insert into candidate_language (candidate_id, language_id, written_level_id, spoken_level_id)
select candidate_id, 0, written_level_id, spoken_level_id -- Rohingya
from candidate_language
where migration_language = 'Burmese and Rohingya';

-- 3. Zaghawa language and English language and Arabic language -> Zaghawa (0), English (342), Arabic (343)
-- Update original row to Zaghawa
update candidate_language
set language_id = 0 -- Zaghawa (not in table the same like Rohingya)
where migration_language = 'Zaghawa language and English language and Arabic language';
-- Insert English
insert into candidate_language (candidate_id, language_id, written_level_id, spoken_level_id)
select candidate_id, 342, written_level_id, spoken_level_id -- English
from candidate_language
where migration_language = 'Zaghawa language and English language and Arabic language';
-- Insert Arabic
insert into candidate_language (candidate_id, language_id, written_level_id, spoken_level_id)
select candidate_id, 343, written_level_id, spoken_level_id -- Arabic
from candidate_language
where migration_language = 'Zaghawa language and English language and Arabic language';

-- 4. العربية والانكليزية, العربية والأنكليزية -> Arabic (343), English (342)
-- Update original row to Arabic
update candidate_language
set language_id = 343 -- Arabic
where migration_language in ('العربية والانكليزية', 'العربية والأنكليزية');
-- Insert English
insert into candidate_language (candidate_id, language_id, written_level_id, spoken_level_id)
select candidate_id, 342, written_level_id, spoken_level_id -- English
from candidate_language
where migration_language in ('العربية والانكليزية', 'العربية والأنكليزية');

-- 5. العربيه و الفرنسيه والانكليزيه -> Arabic (343), French (344), English (342)
-- Update original row to Arabic
update candidate_language
set language_id = 343 -- Arabic
where migration_language = 'العربيه و الفرنسيه والانكليزيه';
-- Insert French
insert into candidate_language (candidate_id, language_id, written_level_id, spoken_level_id)
select candidate_id, 344, written_level_id, spoken_level_id -- French
from candidate_language
where migration_language = 'العربيه و الفرنسيه والانكليزيه';
-- Insert English
insert into candidate_language (candidate_id, language_id, written_level_id, spoken_level_id)
select candidate_id, 342, written_level_id, spoken_level_id -- English
from candidate_language
where migration_language = 'العربيه و الفرنسيه والانكليزيه';

-- 6. اللغة العربية .اللغة الإنكليزية -> Arabic (343), English (342)
-- Update original row to Arabic
update candidate_language
set language_id = 343 -- Arabic
where migration_language = 'اللغة العربية .اللغة الإنكليزية';
-- Insert English
insert into candidate_language (candidate_id, language_id, written_level_id, spoken_level_id)
select candidate_id, 342, written_level_id, spoken_level_id -- English
from candidate_language
where migration_language = 'اللغة العربية .اللغة الإنكليزية';

-- Other multi-language entries (using primary language)
update candidate_language
set language_id = 343 -- Arabic (first language listed)
where migration_language in ('الانجليزية،  الفرنسية. الروسية ،العربية', 'الكردية و العربية و التركية', 'اللغة الأنجليزية واللغة العربيه', 'العربيه والانكليزيه');

