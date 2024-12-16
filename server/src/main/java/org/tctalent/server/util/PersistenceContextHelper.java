/*
 * Copyright (c) 2024 Talent Catalog.
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

package org.tctalent.server.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;

/**
 * Helper component for managing the persistence context in JPA.
 * <p>
 * This component provides a utility method to flush and clear the {@link EntityManager} to write
 * any pending changes to the database and to reset the in-memory persistence context, which
 * is useful for managing memory.
 * </p>
 *
 * <p>
 * The {@code entityManager} is injected with {@code @PersistenceContext}, allowing it to be
 * container-managed and properly cleaned up after use.
 * </p>
 */
@Component
public class PersistenceContextHelper {

  @PersistenceContext
  private EntityManager entityManager;

  /**
   * Flushes and clears the persistence context.
   * <p>
   * - Ensures all pending changes in the persistence context are written to the database.
   * - Clears the persistence context, detaching all managed entities.
   * <p>
   * Use this method when you need to explicitly synchronize changes with the database
   * and reset the persistence context to prevent memory overhead or side effects from managed entities.
   * <p>
   * Note: Requires an active transaction to execute successfully.
   */
  public void flushAndClearEntityManager() {
    entityManager.flush();  // Flush changes to DB
    entityManager.clear();  // Clear in-memory persistence context
  }

  /**
   * Clears the persistence context without flushing.
   * <p>
   * - Detaches all managed entities from the persistence context.
   * - No changes are synchronized with the database.
   * <p>
   * Use this method to reset the persistence context when you want to avoid potential memory overhead
   * or stale data issues, without committing changes to the database.
   * <p>
   * Note: Does not require an active transaction since no database operation is performed.
   */
  public void clearEntityManager() {
    entityManager.clear();
  }
}
