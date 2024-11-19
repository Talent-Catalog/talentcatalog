/*
 * Copyright (c) 2024 Talent Beyond Boundaries.
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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
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

  public void flushAndClearEntityManager() {
    entityManager.flush();  // Flush changes to DB
    entityManager.clear();  // Clear in-memory persistence context
  }
}
