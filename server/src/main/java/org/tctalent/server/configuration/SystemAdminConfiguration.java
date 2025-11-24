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

package org.tctalent.server.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.tctalent.server.model.db.Role;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.model.db.User;
import org.tctalent.server.request.list.UpdateSavedListInfoRequest;
import org.tctalent.server.request.user.UpdateUserRequest;
import org.tctalent.server.service.db.SavedListService;
import org.tctalent.server.service.db.UserService;

/**
 * Component which listens for a Spring start up event and auto creates objects if needed.
 *
 * @author John Cameron
 */
@Component
public class SystemAdminConfiguration {
  public final static String TEST_CANDIDATE_LIST_NAME = "TestCandidates";
  public static long TEST_CANDIDATE_LIST_ID;
  public final static String PENDING_TERMS_ACCEPTANCE_LIST_NAME = "PendingTermsAcceptance";
  public static long PENDING_TERMS_ACCEPTANCE_LIST_ID;

  public final static String SYSTEM_ADMIN_NAME = "SystemAdmin";
  public final static String[] GLOBAL_LIST_NAMES = new String[] {

      //Tags candidates as test candidates
      TEST_CANDIDATE_LIST_NAME,

      //Tags candidates who have been asked to accept our latest terms but who have not yet done so
      PENDING_TERMS_ACCEPTANCE_LIST_NAME
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
   * Run at startup to check whether we have necessary objects, creating them if necessary
   */
  @EventListener(ApplicationReadyEvent.class)
  public void autoCreates() {

    User systemAdmin = userService.findByUsernameAndRole(SYSTEM_ADMIN_NAME, Role.systemadmin);
    if (systemAdmin == null) {
      UpdateUserRequest req = new UpdateUserRequest();
      req.setUsername(SYSTEM_ADMIN_NAME);
      req.setStatus(Status.active);
      req.setFirstName("System");
      req.setLastName("Admin");
      req.setEmail(sysAdminEmail);
      req.setRole(Role.systemadmin);
      req.setReadOnly(false);
      req.setUsingMfa(true);

      //Self create system admin
      systemAdmin = userService.createUser(req, null);
    }

    //Create global lists
    for (String listName : GLOBAL_LIST_NAMES) {
      SavedList savedList = savedListService.get(systemAdmin, listName);
      //Don't create if already exists.
      if (savedList == null) {
        //Create the global list
        UpdateSavedListInfoRequest req = new UpdateSavedListInfoRequest();
        req.setGlobal(true);
        req.setFixed(true);
        req.setName(listName);
        savedList = savedListService.createSavedList(systemAdmin, req);
      }

      //For some global lists we store their ids for convenience.

      if (listName.equals(TEST_CANDIDATE_LIST_NAME)) {
        TEST_CANDIDATE_LIST_ID = savedList.getId();
      }

      if (listName.equals(PENDING_TERMS_ACCEPTANCE_LIST_NAME)) {
        PENDING_TERMS_ACCEPTANCE_LIST_ID = savedList.getId();
      }
    }
  }

}
