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

package org.tctalent.server.logging;

import lombok.extern.slf4j.Slf4j;
import org.ehcache.event.CacheEvent;
import org.ehcache.event.CacheEventListener;

/**
 * Listens for cache events and logs them. The event types that will be logged are defined in
 * ehcache.xml in /resources.
 *
 * @author sadatmalik
 */
@Slf4j
public class CacheLogger implements CacheEventListener<Object, Object> {

  @Override
  public void onEvent(CacheEvent<?, ?> cacheEvent) {
    log.info("Key: {} | EventType: {} | Old value: {} | New value: {}",
        cacheEvent.getKey(), cacheEvent.getType(), cacheEvent.getOldValue(),
        cacheEvent.getNewValue());
  }

}
