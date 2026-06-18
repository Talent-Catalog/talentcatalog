ALTER TABLE public.candidate_certification
    ADD COLUMN created_by bigint REFERENCES public.users (id),
    ADD COLUMN created_date timestamp with time zone,
    ADD COLUMN updated_by bigint REFERENCES public.users (id),
    ADD COLUMN updated_date timestamp with time zone;

ALTER TABLE public.candidate_destination
    ADD COLUMN created_by bigint REFERENCES public.users (id),
    ADD COLUMN created_date timestamp with time zone,
    ADD COLUMN updated_by bigint REFERENCES public.users (id),
    ADD COLUMN updated_date timestamp with time zone;

ALTER TABLE public.candidate_education
    ADD COLUMN created_by bigint REFERENCES public.users (id),
    ADD COLUMN created_date timestamp with time zone,
    ADD COLUMN updated_by bigint REFERENCES public.users (id),
    ADD COLUMN updated_date timestamp with time zone;

ALTER TABLE public.candidate_exam
    ADD COLUMN created_by bigint REFERENCES public.users (id),
    ADD COLUMN created_date timestamp with time zone,
    ADD COLUMN updated_by bigint REFERENCES public.users (id),
    ADD COLUMN updated_date timestamp with time zone;

ALTER TABLE public.candidate_job_experience
    ADD COLUMN created_by bigint REFERENCES public.users (id),
    ADD COLUMN created_date timestamp with time zone,
    ADD COLUMN updated_by bigint REFERENCES public.users (id),
    ADD COLUMN updated_date timestamp with time zone;

ALTER TABLE public.candidate_language
    ADD COLUMN created_by bigint REFERENCES public.users (id),
    ADD COLUMN created_date timestamp with time zone,
    ADD COLUMN updated_by bigint REFERENCES public.users (id),
    ADD COLUMN updated_date timestamp with time zone;

ALTER TABLE public.candidate_dependant
    ADD COLUMN created_by bigint REFERENCES public.users (id),
    ADD COLUMN created_date timestamp with time zone,
    ADD COLUMN updated_by bigint REFERENCES public.users (id),
    ADD COLUMN updated_date timestamp with time zone;

ALTER TABLE public.candidate_citizenship
    ADD COLUMN created_by bigint REFERENCES public.users (id),
    ADD COLUMN created_date timestamp with time zone,
    ADD COLUMN updated_by bigint REFERENCES public.users (id),
    ADD COLUMN updated_date timestamp with time zone;

ALTER TABLE public.candidate_visa_job_check
    ADD COLUMN created_by bigint REFERENCES public.users (id),
    ADD COLUMN created_date timestamp with time zone,
    ADD COLUMN updated_by bigint REFERENCES public.users (id),
    ADD COLUMN updated_date timestamp with time zone;
