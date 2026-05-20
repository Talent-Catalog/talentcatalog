ALTER TABLE public.task
  ADD COLUMN notify_on_assignment boolean NOT NULL DEFAULT false;
