DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1
            FROM public.country
            WHERE name = 'Slovakia'
              AND iso_code = 'SK'
        ) THEN
            INSERT INTO public.country (name, status, iso_code)
            VALUES ('Slovakia', 'active', 'SK');
        END IF;
    END $$;