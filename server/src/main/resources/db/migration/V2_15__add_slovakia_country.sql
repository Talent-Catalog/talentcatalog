DO $$
    BEGIN
        IF EXISTS (
            SELECT 1
            FROM public.country
            WHERE name = 'Slovakia'
               OR iso_code = 'SK'
        ) THEN
            UPDATE public.country
            SET name = 'Slovakia',
                status = 'active',
                iso_code = 'SK'
            WHERE id = (
                SELECT id
                FROM public.country
                WHERE name = 'Slovakia'
                   OR iso_code = 'SK'
                ORDER BY
                    CASE WHEN iso_code = 'SK' THEN 0 ELSE 1 END,
                    id
                LIMIT 1
            );
        ELSE
            INSERT INTO public.country (name, status, iso_code)
            VALUES ('Slovakia', 'active', 'SK');
        END IF;
    END $$;
