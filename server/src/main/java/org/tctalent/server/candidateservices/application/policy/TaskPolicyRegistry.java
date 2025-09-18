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

package org.tctalent.server.candidateservices.application.policy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class TaskPolicyRegistry {

  private final Map<String, TaskPolicy> policies; // provider -> policy

  public TaskPolicyRegistry(List<TaskPolicy> policies) {
    Map<String, TaskPolicy> map = new HashMap<>();
    for (TaskPolicy p : policies) {
      String key = p.provider();
      if (map.putIfAbsent(key, p) != null) {
        throw new IllegalStateException("Duplicate TaskPolicy for provider=" + key);
      }
    }
    this.policies = Map.copyOf(map);
  }

  public TaskPolicy forProvider(String provider) { // provider descriptors
    var p = policies.get(provider);
    if (p == null) {
      throw new IllegalStateException("No policy for " + provider);
    }
    return p;
  }

}
