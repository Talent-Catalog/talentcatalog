package org.tbbtalent.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.tbbtalent.server.model.AuditLog;
import org.tbbtalent.server.service.audit.AuditType;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {

    @Query("select a from AuditLog a where a.type = ?1 and a.objectRef = ?2")
    AuditLog findByTypeAndObjectRef(AuditType type, String objectRef);

}
