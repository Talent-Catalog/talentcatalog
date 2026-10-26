/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.integration.cache;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.tctalent.server.casi.core.scheduler.ResourceExpiryScheduler;
import org.tctalent.server.casi.domain.model.ResourceStatus;
import org.tctalent.server.casi.domain.model.ResourceType;
import org.tctalent.server.casi.domain.model.ServiceCode;
import org.tctalent.server.casi.domain.model.ServiceProvider;
import org.tctalent.server.casi.domain.persistence.ServiceResourceEntity;
import org.tctalent.server.casi.domain.persistence.ServiceResourceRepository;
import org.tctalent.server.integration.helper.BaseDBIntegrationTest;
import org.tctalent.server.repository.db.read.cache.CandidateRedisCache;

/**
 * Checks that ShedLock protects our scheduled jobs, using the database lock table: the first run
 * takes the lock for 23 hours, a second run while it is locked does nothing, and the job runs
 * again once the lock is released.
 * <p>
 * Added in TC-1189 (ShedLock 5 to 7, Spring Framework 7).
 * </p>
 */
@SpringBootTest
class ShedLockSchedulerIntegrationTest extends BaseDBIntegrationTest {

    private static final String LOCK_NAME = "ServiceResource_Expire";

    @MockitoBean
    private CandidateRedisCache candidateRedisCache;

    @Autowired private ResourceExpiryScheduler resourceExpiryScheduler;
    @Autowired private ServiceResourceRepository serviceResourceRepository;
    @Autowired private JdbcTemplate jdbc;

    private Long resourceId;

    @BeforeEach
    void setUp() {
        releaseLock();
    }

    @AfterEach
    void tearDown() {
        releaseLock();
        if (resourceId != null) {
            serviceResourceRepository.deleteById(resourceId);
        }
    }

    @Test
    @DisplayName("a scheduled job runs once while it holds its ShedLock lock")
    void scheduledJobIsLocked() {
        resourceExpiryScheduler.markResourcesAsExpired();

        //The lock is held for lockAtLeastFor (23 hours)
        assertThat(jdbc.queryForObject(
            "select lock_until > now() at time zone 'utc' + interval '22 hours' from shedlock"
                + " where name = ?", Boolean.class, LOCK_NAME)).isTrue();

        //While locked, the job does not run
        resourceId = expiredResource().getId();
        resourceExpiryScheduler.markResourcesAsExpired();
        assertThat(status(resourceId)).isEqualTo(ResourceStatus.AVAILABLE);

        //Once the lock is released (eg the next day), it runs again
        releaseLock();
        resourceExpiryScheduler.markResourcesAsExpired();
        assertThat(status(resourceId)).isEqualTo(ResourceStatus.EXPIRED);
    }

    private ServiceResourceEntity expiredResource() {
        ServiceResourceEntity resource = new ServiceResourceEntity();
        resource.setProvider(ServiceProvider.DUOLINGO);
        resource.setServiceCode(ServiceCode.TEST_PROCTORED);
        resource.setResourceCode("TC1189-" + UUID.randomUUID());
        resource.setStatus(ResourceStatus.AVAILABLE);
        resource.setExpiresAt(OffsetDateTime.now().minusDays(1));
        resource.setResourceType(ResourceType.UNIQUE);
        return serviceResourceRepository.saveAndFlush(resource);
    }

    private ResourceStatus status(Long id) {
        return serviceResourceRepository.findById(id).orElseThrow().getStatus();
    }

    /** Releases the lock as ShedLock does, keeping the row (which ShedLock remembers exists). */
    private void releaseLock() {
        jdbc.update("update shedlock set lock_until = '2000-01-01' where name = ?", LOCK_NAME);
    }
}
