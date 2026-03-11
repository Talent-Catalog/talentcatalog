do $$
    begin
        -- Balochi (Southern Balochi)
        if not exists (select 1 from language where iso_code = 'bcc') then
            insert into language (id, name, iso_code, status)
            values (nextval('language_id_seq'), 'Balochi (Southern Balochi)', 'bcc', 'active');
        end if;

        -- Balochi (Western Balochi)
        if not exists (select 1 from language where iso_code = 'bgn') then
            insert into language (id, name, iso_code, status)
            values (nextval('language_id_seq'), 'Balochi (Western Balochi)', 'bgn', 'active');
        end if;

        -- Balochi (Eastern Balochi)
        if not exists (select 1 from language where iso_code = 'bgp') then
            insert into language (id, name, iso_code, status)
            values (nextval('language_id_seq'), 'Balochi (Eastern Balochi)', 'bgp', 'active');
        end if;

        -- brahui
        if not exists (select 1 from language where iso_code = 'brh') then
            insert into language (id, name, iso_code, status)
            values (nextval('language_id_seq'), 'brahui', 'brh', 'active');
        end if;

        -- circassian (adyghe)
        if not exists (select 1 from language where iso_code = 'ady') then
            insert into language (id, name, iso_code, status)
            values (nextval('language_id_seq'), 'circassian (adyghe)', 'ady', 'active');
        end if;

        -- circassian (kabardian)
        if not exists (select 1 from language where iso_code = 'kbd') then
            insert into language (id, name, iso_code, status)
            values (nextval('language_id_seq'), 'circassian (kabardian)', 'kbd', 'active');
        end if;

        -- fur
        if not exists (select 1 from language where iso_code = 'fvr') then
            insert into language (id, name, iso_code, status)
            values (nextval('language_id_seq'), 'fur', 'fvr', 'active');
        end if;

        -- latin
        if not exists (select 1 from language where iso_code = 'la') then
            insert into language (id, name, iso_code, status)
            values (nextval('language_id_seq'), 'latin', 'la', 'active');
        end if;

        -- nauruan
        if not exists (select 1 from language where iso_code = 'nau') then
            insert into language (id, name, iso_code, status)
            values (nextval('language_id_seq'), 'nauruan', 'nau', 'active');
        end if;

        -- rohingya
        if not exists (select 1 from language where iso_code = 'rhg') then
            insert into language (id, name, iso_code, status)
            values (nextval('language_id_seq'), 'rohingya', 'rhg', 'active');
        end if;

        -- sindhi
        if not exists (select 1 from language where iso_code = 'snd') then
            insert into language (id, name, iso_code, status)
            values (nextval('language_id_seq'), 'sindhi', 'snd', 'active');
        end if;

        -- syriac
        if not exists (select 1 from language where iso_code = 'syr') then
            insert into language (id, name, iso_code, status)
            values (nextval('language_id_seq'), 'syriac', 'syr', 'active');
        end if;

        -- zaghawa
        if not exists (select 1 from language where iso_code = 'zag') then
            insert into language (id, name, iso_code, status)
            values (nextval('language_id_seq'), 'zaghawa', 'zag', 'active');
        end if;

        -- international sign language
        if not exists (select 1 from language where iso_code = 'ils') then
            insert into language (id, name, iso_code, status)
            values (nextval('language_id_seq'), 'International Sign', 'ils', 'active');
        end if;
    end $$;

-- mapping for each remained language with new added language id
update candidate_language
set language_id = (select id from language where iso_code = 'bcc') -- balochi
where trim(migration_language) = 'Balochi';

update candidate_language
set language_id = (select id from language where iso_code = 'brh') -- brahui
where trim(migration_language) = 'Brahvi';

update candidate_language
set language_id = (select id from language where iso_code = 'ady') -- circassian (adyghe)
where trim(migration_language) = 'Circassian';

update candidate_language
set language_id = (select id from language where iso_code = 'fvr') -- fur
where trim(migration_language) = 'Fur language';

update candidate_language
set language_id = (select id from language where iso_code = 'kbd') -- kabardian
where trim(migration_language) = 'Kabardian';

update candidate_language
set language_id = (select id from language where iso_code = 'la') -- latin
where trim(migration_language) = 'Latina';

update candidate_language
set language_id = (select id from language where iso_code = 'nau') -- nauruan
where trim(migration_language) = 'Nauruan';

update candidate_language
set language_id = (select id from language where iso_code = 'rhg') -- rohingya
where trim(migration_language) = 'Rohingan';

update candidate_language
set language_id = (select id from language where iso_code = 'rhg') -- rohingya
where trim(migration_language) = 'Rohingya';

update candidate_language
set language_id = (select id from language where iso_code = 'rhg') -- rohingya
where trim(migration_language) = 'Rohingyan';

update candidate_language
set language_id = (select id from language where iso_code = 'snd') -- sindhi
where trim(migration_language) = 'Sindhi';

update candidate_language
set language_id = (select id from language where iso_code = 'syr') -- syriac
where trim(migration_language) = 'Syriac';

update candidate_language
set language_id = (select id from language where iso_code = 'zag') -- zaghawa
where trim(migration_language) = 'Zaghawa language and English language and Arabic language';

update candidate_language
set language_id = (select id from language where iso_code = 'zag') -- zaghawa
where trim(migration_language) = 'لغة زغاوية';

update candidate_language
set language_id = (select id from language where iso_code = 'ara') -- arabic
where trim(migration_language) = 'الاردنيه';

update candidate_language
set language_id = (select id from language where iso_code = 'syr') -- syriac
where trim(migration_language) = 'السريانية';

update candidate_language
set language_id = (select id from language where iso_code = 'ils') -- international sign language
where trim(migration_language) = 'اصم وابكم';

-- staging should have 0 instances of values for this field where the corresponding candidate_language.language_id is 0
alter table candidate_language drop column migration_language;
