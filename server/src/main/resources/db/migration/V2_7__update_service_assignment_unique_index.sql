DROP INDEX IF EXISTS sa_assigned_per_resource_uq_idx;

CREATE UNIQUE INDEX IF NOT EXISTS sa_assigned_per_resource_candidate_uq_idx
    ON service_assignment(resource_id, candidate_id)
    WHERE status = 'ASSIGNED' AND resource_id IS NOT NULL;
