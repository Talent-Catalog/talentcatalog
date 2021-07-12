/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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

package org.tbbtalent.server.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.tbbtalent.server.model.db.Role;
import org.tbbtalent.server.model.db.User;
import org.tbbtalent.server.request.list.UpdateSavedListInfoRequest;
import org.tbbtalent.server.request.user.CreateUserRequest;
import org.tbbtalent.server.service.db.SavedListService;
import org.tbbtalent.server.service.db.UserService;

/**
 * Component which listens for a Spring start up event and creates a system admin user if
 * none is present.
 *
 * @author John Cameron
 */
@Component
public class SystemAdminConfiguration {

  private final static String SYSTEM_ADMIN_NAME = "SystemAdmin";
  public final static String[] GLOBAL_LIST_NAMES = new String[] {
      "TestCandidates"
  }; 
  
  private final SavedListService savedListService;
  private final UserService userService;

  @Value("${email.user}")
  private String sysAdminEmail;
  
  @Autowired
  public SystemAdminConfiguration(SavedListService savedListService,
      UserService userService) {
    this.savedListService = savedListService;
    this.userService = userService;
  }

  /**
   * Run at startup to check whether we have a system admin user, creating one if necessary
   */
  @EventListener(ApplicationReadyEvent.class)
  public void autoCreateSystemAdmin() {
    User systemAdmin = userService.findByUsernameAndRole(SYSTEM_ADMIN_NAME, Role.admin);
    if (systemAdmin == null) {
      CreateUserRequest req = new CreateUserRequest();
      req.setUsername(SYSTEM_ADMIN_NAME);
      req.setFirstName("System");
      req.setLastName("Admin");
      req.setEmail(sysAdminEmail);
      req.setRole(Role.admin);
      req.setReadOnly(false);
      req.setUsingMfa(true);
      req.setPassword("password");
      systemAdmin = userService.createUser(req);
    }

    //Create global lists
    for (String listName : GLOBAL_LIST_NAMES) {
      //Don't create if already exists.
      if (savedListService.get(systemAdmin, listName) == null) {
        //Create the global list
        UpdateSavedListInfoRequest req = new UpdateSavedListInfoRequest();
        req.setGlobal(true);
        req.setFixed(true);
        req.setName(listName);
        savedListService.createSavedList(systemAdmin, req);
      }
    }
  }
  
}
