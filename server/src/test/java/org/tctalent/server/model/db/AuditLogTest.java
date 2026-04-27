package org.tctalent.server.model.db;

import org.junit.jupiter.api.Test;
import org.tctalent.server.service.db.audit.AuditAction;
import org.tctalent.server.service.db.audit.AuditType;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class AuditLogTest {

  @Test
  void testConstructorAndGetters() {
    // Given
    OffsetDateTime now = OffsetDateTime.now();
    Long userId = 101L;
    AuditType type = AuditType.CANDIDATE_OCCUPATION;
    AuditAction action = AuditAction.UPDATE;
    String objectRef = "CANDIDATE#123";
    String description = "Updated occupation details";

    // When
    AuditLog log = new AuditLog(now, userId, type, action, objectRef, description);

    // Then
    assertThat(log.getEventDate()).isEqualTo(now);
    assertThat(log.getUserId()).isEqualTo(userId);
    assertThat(log.getType()).isEqualTo(type);
    assertThat(log.getAction()).isEqualTo(action);
    assertThat(log.getObjectRef()).isEqualTo(objectRef);
    assertThat(log.getDescription()).isEqualTo(description);
  }

  @Test
  void testSetters() {
    // Given
    AuditLog log = new AuditLog();
    OffsetDateTime now = OffsetDateTime.now();

    // When
    log.setEventDate(now);
    log.setUserId(202L);
    log.setType(AuditType.CANDIDATE_OCCUPATION);
    log.setAction(AuditAction.ADD);
    log.setObjectRef("OBJ#456");
    log.setDescription("Created something new");

    // Then
    assertThat(log.getEventDate()).isEqualTo(now);
    assertThat(log.getUserId()).isEqualTo(202L);
    assertThat(log.getType()).isEqualTo(AuditType.CANDIDATE_OCCUPATION);
    assertThat(log.getAction()).isEqualTo(AuditAction.ADD);
    assertThat(log.getObjectRef()).isEqualTo("OBJ#456");
    assertThat(log.getDescription()).isEqualTo("Created something new");
  }
}
