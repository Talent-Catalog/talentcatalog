-- Country rows in V2__init are inserted with explicit ids, which does not
-- advance country_id_seq. Inserts that omit id call nextval and can collide
-- with the country primary key when the sequence is still behind MAX(id).
SELECT setval(
    'public.country_id_seq',
    (SELECT MAX(id) FROM public.country)
);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM public.country
        WHERE name = 'Moldova'
           OR iso_code = 'MD'
    ) THEN
        INSERT INTO public.country (name, status, iso_code)
        VALUES ('Moldova', 'active', 'MD');
    END IF;
END $$;
