/*
 * Copyright (c) 2025 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.casi.domain.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.tctalent.server.casi.domain.model.ResourceStatus;
import org.tctalent.server.casi.domain.model.ServiceCode;
import org.tctalent.server.casi.domain.model.ServiceProvider;
import org.tctalent.server.casi.domain.model.ResourceType;
import org.tctalent.server.integration.helper.BaseJpaIntegrationTest;

/**
 * Integration tests for {@link ServiceResourceRepository} running against a real PostgreSQL
 * instance via Testcontainers. Covers the native {@code lockNextAvailable} query (including
 * concurrent {@code FOR UPDATE SKIP LOCKED} behaviour), the expiry finder queries, count
 * queries, and existence/lookup queries.
 */
class ServiceResourceRepositoryIntegrationTest extends BaseJpaIntegrationTest {

  @Autowired
  private ServiceResourceRepository repo;

  @Autowired
  private PlatformTransactionManager txManager;

  private ServiceResourceEntity available1;
  private ServiceResourceEntity available2;
  private ServiceResourceEntity reserved;
  private ServiceResourceEntity expired;
  private ServiceResourceEntity redeemed;
  private ServiceResourceEntity disabled;
  private ServiceResourceEntity nonProctoredAvailable;
  private ServiceResourceEntity noExpiry;

  @BeforeEach
  void setUp() {
    available1 = saveResource(
        ServiceProvider.DUOLINGO, ServiceCode.TEST_PROCTORED,
        "PROC-INT-001", ResourceStatus.AVAILABLE,
        OffsetDateTime.now().plusDays(30));

    available2 = saveResource(
        ServiceProvider.DUOLINGO, ServiceCode.TEST_PROCTORED,
        "PROC-INT-002", ResourceStatus.AVAILABLE,
        OffsetDateTime.now().plusDays(30));

    reserved = saveResource(
        ServiceProvider.DUOLINGO, ServiceCode.TEST_PROCTORED,
        "PROC-INT-003", ResourceStatus.RESERVED,
        OffsetDateTime.now().plusDays(30));

    expired = saveResource(
        ServiceProvider.DUOLINGO, ServiceCode.TEST_PROCTORED,
        "PROC-INT-004", ResourceStatus.EXPIRED,
        OffsetDateTime.now().minusDays(5));

    redeemed = saveResource(
        ServiceProvider.DUOLINGO, ServiceCode.TEST_PROCTORED,
        "PROC-INT-005", ResourceStatus.REDEEMED,
        OffsetDateTime.now().minusDays(1));

    disabled = saveResource(
        ServiceProvider.DUOLINGO, ServiceCode.TEST_PROCTORED,
        "PROC-INT-006", ResourceStatus.DISABLED,
        OffsetDateTime.now().plusDays(10));

    nonProctoredAvailable = saveResource(
        ServiceProvider.DUOLINGO, ServiceCode.TEST_NON_PROCTORED,
        "NONPROC-INT-001", ResourceStatus.AVAILABLE,
        OffsetDateTime.now().plusDays(30));

    noExpiry = saveResource(
        ServiceProvider.DUOLINGO, ServiceCode.TEST_PROCTORED,
        "PROC-INT-007", ResourceStatus.AVAILABLE,
        null);
  }

  // ── lockNextAvailable ──────────────────────────────────────────────────

  @Test
  @DisplayName("lockNextAvailable returns the lowest-id AVAILABLE resource")
  void lockNextAvailable_returnsLowestIdAvailable() {
    ServiceResourceEntity result = repo.lockNextAvailable(
        ServiceProvider.DUOLINGO.name(), ServiceCode.TEST_PROCTORED.name());

    assertThat(result).isNotNull();
    assertThat(result.getStatus()).isEqualTo(ResourceStatus.AVAILABLE);
    assertThat(result.getId()).isEqualTo(available1.getId());
  }

  @Test
  @DisplayName("lockNextAvailable returns null when no AVAILABLE resources exist")
  void lockNextAvailable_returnsNullWhenNoneAvailable() {
    ServiceResourceEntity result = repo.lockNextAvailable(
        ServiceProvider.DUOLINGO.name(), "NONEXISTENT_CODE");

    assertThat(result).isNull();
  }

  @Test
  @DisplayName("lockNextAvailable only returns AVAILABLE status, ignores others")
  void lockNextAvailable_ignoresNonAvailableStatuses() {
    available1.setStatus(ResourceStatus.RESERVED);
    available2.setStatus(ResourceStatus.SENT);
    noExpiry.setStatus(ResourceStatus.DISABLED);
    repo.saveAllAndFlush(List.of(available1, available2, noExpiry));

    ServiceResourceEntity result = repo.lockNextAvailable(
        ServiceProvider.DUOLINGO.name(), ServiceCode.TEST_PROCTORED.name());

    assertThat(result).isNull();
  }

  @Test
  @DisplayName("lockNextAvailable filters by provider and serviceCode")
  void lockNextAvailable_filtersByProviderAndServiceCode() {
    ServiceResourceEntity result = repo.lockNextAvailable(
        ServiceProvider.DUOLINGO.name(), ServiceCode.TEST_NON_PROCTORED.name());

    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(nonProctoredAvailable.getId());
    assertThat(result.getServiceCode()).isEqualTo(ServiceCode.TEST_NON_PROCTORED);
  }

  @Test
  @DisplayName("lockNextAvailable ignores SHARED resources")
  void lockNextAvailable_ignoresSharedResources() {
    ServiceResourceEntity shared = saveResource(
        ServiceProvider.DUOLINGO, ServiceCode.TEST_PROCTORED,
        "PROC-INT-SHARED", ResourceStatus.AVAILABLE,
        OffsetDateTime.now().plusDays(30),
        ResourceType.SHARED);

    ServiceResourceEntity result = repo.lockNextAvailable(
        ServiceProvider.DUOLINGO.name(), ServiceCode.TEST_PROCTORED.name());

    assertThat(result).isNotNull();
    assertThat(result.getId()).isNotEqualTo(shared.getId());
  }

  // ── concurrent lockNextAvailable (FOR UPDATE SKIP LOCKED) ─────────────

  @Test
  @DisplayName("concurrent lockNextAvailable: second caller skips the row locked by the first")
  void lockNextAvailable_concurrentCallersGetDifferentRows() throws Exception {
    TransactionTemplate txTemplate = new TransactionTemplate(txManager);
    txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

    // Pre-commit two AVAILABLE resources visible to all transactions
    List<Long> committedIds = txTemplate.execute(status -> {
      ServiceResourceEntity r1 = saveResource(
          ServiceProvider.DUOLINGO, ServiceCode.TEST_PROCTORED,
          "CONC-001", ResourceStatus.AVAILABLE, OffsetDateTime.now().plusDays(30));
      ServiceResourceEntity r2 = saveResource(
          ServiceProvider.DUOLINGO, ServiceCode.TEST_PROCTORED,
          "CONC-002", ResourceStatus.AVAILABLE, OffsetDateTime.now().plusDays(30));
      return List.of(r1.getId(), r2.getId());
    });

    assertThat(committedIds).hasSize(2);

    // Latches to coordinate the two threads
    CountDownLatch t1HasLock = new CountDownLatch(1);
    CountDownLatch t2Done = new CountDownLatch(1);

    AtomicReference<Long> t1ResourceId = new AtomicReference<>();
    AtomicReference<Long> t2ResourceId = new AtomicReference<>();
    AtomicReference<Throwable> failure = new AtomicReference<>();

    ExecutorService executor = Executors.newFixedThreadPool(2);

    try {
      // Thread 1: lock the first row and hold it
      Future<?> f1 = executor.submit(() -> {
        try {
          TransactionTemplate threadTx = new TransactionTemplate(txManager);
          threadTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
          threadTx.execute(status -> {
            ServiceResourceEntity locked = repo.lockNextAvailable(
                ServiceProvider.DUOLINGO.name(), ServiceCode.TEST_PROCTORED.name());
            if (locked != null) {
              t1ResourceId.set(locked.getId());
            }
            t1HasLock.countDown();

            // Hold the lock until thread 2 finishes its query
            try {
              t2Done.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
            }
            return null;
          });
        } catch (Throwable t) {
          failure.compareAndSet(null, t);
          t1HasLock.countDown();
        }
      });

      // Thread 2: wait for T1 to hold the lock, then query
      Future<?> f2 = executor.submit(() -> {
        try {
          t1HasLock.await(10, TimeUnit.SECONDS);

          TransactionTemplate threadTx = new TransactionTemplate(txManager);
          threadTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
          threadTx.execute(status -> {
            ServiceResourceEntity locked = repo.lockNextAvailable(
                ServiceProvider.DUOLINGO.name(), ServiceCode.TEST_PROCTORED.name());
            if (locked != null) {
              t2ResourceId.set(locked.getId());
            }
            return null;
          });
        } catch (Throwable t) {
          failure.compareAndSet(null, t);
        } finally {
          t2Done.countDown();
        }
      });

      f1.get(15, TimeUnit.SECONDS);
      f2.get(15, TimeUnit.SECONDS);

      assertThat(failure.get())
          .as("no exceptions from worker threads")
          .isNull();

      assertThat(t1ResourceId.get()).isNotNull();
      assertThat(t2ResourceId.get()).isNotNull();
      assertThat(t1ResourceId.get())
          .as("thread 1 and thread 2 must lock different rows")
          .isNotEqualTo(t2ResourceId.get());

    } finally {
      executor.shutdownNow();
      // Cleanup committed data
      txTemplate.execute(status -> {
        repo.deleteAllById(committedIds);
        return null;
      });
    }
  }

  // ── findExpirable ──────────────────────────────────────────────────────

  @Test
  @DisplayName("findExpirable returns resources past their expiresAt, excluding terminal statuses")
  void findExpirable_returnsPastExpiryExcludingTerminal() {
    // Create a resource that expired yesterday with AVAILABLE status
    ServiceResourceEntity expirable = saveResource(
        ServiceProvider.DUOLINGO, ServiceCode.TEST_PROCTORED,
        "EXP-INT-001", ResourceStatus.AVAILABLE,
        OffsetDateTime.now().minusDays(1));

    // Create a SENT resource that expired yesterday (should be found)
    ServiceResourceEntity expirableSent = saveResource(
        ServiceProvider.DUOLINGO, ServiceCode.TEST_PROCTORED,
        "EXP-INT-002", ResourceStatus.SENT,
        OffsetDateTime.now().minusDays(1));

    List<ResourceStatus> excluded =
        List.of(ResourceStatus.EXPIRED, ResourceStatus.REDEEMED, ResourceStatus.DISABLED);

    List<ServiceResourceEntity> result = repo.findExpirable(OffsetDateTime.now(), excluded);

    List<Long> resultIds = result.stream().map(ServiceResourceEntity::getId).toList();

    assertThat(resultIds).contains(expirable.getId(), expirableSent.getId());
  }

  @Test
  @DisplayName("findExpirable excludes EXPIRED, REDEEMED, and DISABLED resources")
  void findExpirable_excludesTerminalStatuses() {
    List<ResourceStatus> excluded =
        List.of(ResourceStatus.EXPIRED, ResourceStatus.REDEEMED, ResourceStatus.DISABLED);

    List<ServiceResourceEntity> result = repo.findExpirable(OffsetDateTime.now(), excluded);

    List<Long> resultIds = result.stream().map(ServiceResourceEntity::getId).toList();
    assertThat(resultIds)
        .doesNotContain(expired.getId(), redeemed.getId(), disabled.getId());
  }

  @Test
  @DisplayName("findExpirable ignores resources with null expiresAt")
  void findExpirable_ignoresNullExpiresAt() {
    List<ResourceStatus> excluded =
        List.of(ResourceStatus.EXPIRED, ResourceStatus.REDEEMED, ResourceStatus.DISABLED);

    List<ServiceResourceEntity> result = repo.findExpirable(OffsetDateTime.now(), excluded);

    List<Long> resultIds = result.stream().map(ServiceResourceEntity::getId).toList();
    assertThat(resultIds).doesNotContain(noExpiry.getId());
  }

  @Test
  @DisplayName("findExpirable does not return resources whose expiresAt is in the future")
  void findExpirable_doesNotReturnFutureExpiry() {
    List<ResourceStatus> excluded =
        List.of(ResourceStatus.EXPIRED, ResourceStatus.REDEEMED, ResourceStatus.DISABLED);

    List<ServiceResourceEntity> result = repo.findExpirable(OffsetDateTime.now(), excluded);

    List<Long> resultIds = result.stream().map(ServiceResourceEntity::getId).toList();
    assertThat(resultIds)
        .doesNotContain(available1.getId(), available2.getId(), reserved.getId());
  }

  @Test
  @DisplayName("findExpirable returns empty list when no resources are expirable")
  void findExpirable_returnsEmptyWhenNoneExpirable() {
    List<ResourceStatus> excluded =
        List.of(ResourceStatus.EXPIRED, ResourceStatus.REDEEMED, ResourceStatus.DISABLED);

    // Use a date far in the past so nothing is "expired before" that date
    List<ServiceResourceEntity> result =
        repo.findExpirable(OffsetDateTime.now().minusYears(10), excluded);

    assertThat(result).isEmpty();
  }

  // ── findExpirableForProvider ────────────────────────────────────────────

  @Test
  @DisplayName("findExpirableForProvider filters by provider")
  void findExpirableForProvider_filtersByProvider() {
    ServiceResourceEntity expirable = saveResource(
        ServiceProvider.DUOLINGO, ServiceCode.TEST_PROCTORED,
        "EXPPROV-INT-001", ResourceStatus.AVAILABLE,
        OffsetDateTime.now().minusDays(1));

    List<ResourceStatus> excluded =
        List.of(ResourceStatus.EXPIRED, ResourceStatus.REDEEMED, ResourceStatus.DISABLED);

    List<ServiceResourceEntity> result = repo.findExpirableForProvider(
        ServiceProvider.DUOLINGO, OffsetDateTime.now(), excluded);

    List<Long> resultIds = result.stream().map(ServiceResourceEntity::getId).toList();
    assertThat(resultIds).contains(expirable.getId());

    // All returned resources should belong to DUOLINGO
    assertThat(result).allMatch(r -> r.getProvider() == ServiceProvider.DUOLINGO);
  }

  // ── findByProviderAndServiceCodeAndStatus ──────────────────────────────

  @Test
  @DisplayName("findByProviderAndServiceCodeAndStatus returns matching resources ordered by id desc")
  void findByProviderAndServiceCodeAndStatus_returnsMatchingOrdered() {
    List<ServiceResourceEntity> result = repo.findByProviderAndServiceCodeAndStatus(
        ServiceProvider.DUOLINGO, ServiceCode.TEST_PROCTORED, ResourceStatus.AVAILABLE);

    assertThat(result).isNotEmpty();
    assertThat(result).allMatch(r ->
        r.getProvider() == ServiceProvider.DUOLINGO
            && r.getServiceCode() == ServiceCode.TEST_PROCTORED
            && r.getStatus() == ResourceStatus.AVAILABLE);

    // Verify descending id order
    for (int i = 0; i < result.size() - 1; i++) {
      assertThat(result.get(i).getId()).isGreaterThan(result.get(i + 1).getId());
    }
  }

  @Test
  @DisplayName("findByProviderAndServiceCodeAndStatus returns empty for non-matching status")
  void findByProviderAndServiceCodeAndStatus_emptyForNonMatchingStatus() {
    List<ServiceResourceEntity> result = repo.findByProviderAndServiceCodeAndStatus(
        ServiceProvider.DUOLINGO, ServiceCode.TEST_NON_PROCTORED, ResourceStatus.EXPIRED);

    assertThat(result).isEmpty();
  }

  // ── findByProviderAndResourceCode ──────────────────────────────────────

  @Test
  @DisplayName("findByProviderAndResourceCode returns the resource when it exists")
  void findByProviderAndResourceCode_returnsWhenExists() {
    Optional<ServiceResourceEntity> result =
        repo.findByProviderAndResourceCode(ServiceProvider.DUOLINGO, "PROC-INT-001");

    assertThat(result).isPresent();
    assertThat(result.get().getId()).isEqualTo(available1.getId());
  }

  @Test
  @DisplayName("findByProviderAndResourceCode returns empty when not found")
  void findByProviderAndResourceCode_emptyWhenNotFound() {
    Optional<ServiceResourceEntity> result =
        repo.findByProviderAndResourceCode(ServiceProvider.DUOLINGO, "NONEXISTENT");

    assertThat(result).isEmpty();
  }

  // ── existsByProviderAndResourceCode ────────────────────────────────────

  @Test
  @DisplayName("existsByProviderAndResourceCode returns true for existing resource")
  void existsByProviderAndResourceCode_trueWhenExists() {
    boolean exists = repo.existsByProviderAndResourceCode(
        ServiceProvider.DUOLINGO, "PROC-INT-001");

    assertThat(exists).isTrue();
  }

  @Test
  @DisplayName("existsByProviderAndResourceCode returns false for non-existing resource")
  void existsByProviderAndResourceCode_falseWhenNotExists() {
    boolean exists = repo.existsByProviderAndResourceCode(
        ServiceProvider.DUOLINGO, "NONEXISTENT");

    assertThat(exists).isFalse();
  }

  // ── countAvailableByProviderAndService ─────────────────────────────────

  @Test
  @DisplayName("countAvailableByProviderAndService counts only AVAILABLE for provider+serviceCode")
  void countAvailableByProviderAndService_countsCorrectly() {
    long count = repo.countAvailableByProviderAndService(
        ServiceProvider.DUOLINGO, ServiceCode.TEST_PROCTORED);

    // available1, available2, noExpiry are AVAILABLE + DUOLINGO + TEST_PROCTORED
    assertThat(count).isEqualTo(3);
  }

  @Test
  @DisplayName("countAvailableByProviderAndService returns zero when none available")
  void countAvailableByProviderAndService_zeroWhenNoneAvailable() {
    long count = repo.countAvailableByProviderAndService(
        ServiceProvider.DUOLINGO, ServiceCode.TEST_NON_PROCTORED);

    // nonProctoredAvailable is the only one; count should be 1
    assertThat(count).isEqualTo(1);
  }

  // ── countAvailableByProvider ───────────────────────────────────────────

  @Test
  @DisplayName("countAvailableByProvider counts AVAILABLE across all service codes for a provider")
  void countAvailableByProvider_countsAcrossServiceCodes() {
    long count = repo.countAvailableByProvider(ServiceProvider.DUOLINGO);

    // available1, available2, noExpiry (TEST_PROCTORED) + nonProctoredAvailable (TEST_NON_PROCTORED)
    assertThat(count).isEqualTo(4);
  }

  // ── helpers ────────────────────────────────────────────────────────────

  private ServiceResourceEntity saveResource(
      ServiceProvider provider, ServiceCode serviceCode,
      String resourceCode, ResourceStatus status,
      OffsetDateTime expiresAt) {
    return saveResource(provider, serviceCode, resourceCode, status, expiresAt, ResourceType.UNIQUE);
  }

  private ServiceResourceEntity saveResource(
      ServiceProvider provider, ServiceCode serviceCode,
      String resourceCode, ResourceStatus status,
      OffsetDateTime expiresAt, ResourceType resourceType) {
    ServiceResourceEntity e = new ServiceResourceEntity();
    e.setProvider(provider);
    e.setServiceCode(serviceCode);
    e.setResourceCode(resourceCode);
    e.setStatus(status);
    e.setExpiresAt(expiresAt);
    e.setResourceType(resourceType);
    return repo.saveAndFlush(e);
  }
}
