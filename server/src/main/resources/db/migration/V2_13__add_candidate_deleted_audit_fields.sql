ALTER TABLE public.candidate
    ADD COLUMN deleted_date timestamp with time zone,
    ADD COLUMN deleted_by bigint;

ALTER TABLE public.candidate
    ADD CONSTRAINT candidate_deleted_by_fkey
        FOREIGN KEY (deleted_by)
            REFERENCES public.users(id);

CREATE INDEX idx_candidate_deleted_by
    ON public.candidate(deleted_by);

CREATE INDEX idx_candidate_deleted_date
    ON public.candidate(deleted_date);