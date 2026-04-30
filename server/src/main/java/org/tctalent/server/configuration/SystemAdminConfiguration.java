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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.db.Role;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.model.db.User;
import org.tctalent.server.model.db.partner.Partner;
import org.tctalent.server.request.list.UpdateSavedListInfoRequest;
import org.tctalent.server.request.partner.UpdatePartnerRequest;
import org.tctalent.server.request.user.UpdateUserRequest;
import org.tctalent.server.service.db.PartnerService;
import org.tctalent.server.service.db.SavedListService;
import org.tctalent.server.service.db.ShutdownService;
import org.tctalent.server.service.db.UserService;
import org.tctalent.server.service.db.impl.TcInstanceService;

/**
 * Component which listens for a Spring start up event and auto creates objects if needed.
 *
 * @author John Cameron
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class SystemAdminConfiguration {

    public final static String TEST_CANDIDATE_LIST_NAME = "TestCandidates";
    public static long TEST_CANDIDATE_LIST_ID;
    public final static String PENDING_TERMS_ACCEPTANCE_LIST_NAME = "PendingTermsAcceptance";
    public static long PENDING_TERMS_ACCEPTANCE_LIST_ID;

    public final static String SYSTEM_PARTNER_ABBREVIATION = "OPC";
    public final static String SYSTEM_PARTNER_NAME = "OPC";
    public final static String SYSTEM_ADMIN_NAME = "SystemAdmin";
    public final static String[] GLOBAL_LIST_NAMES = new String[]{

        //Tags candidates as test candidates
        TEST_CANDIDATE_LIST_NAME,

        //Tags candidates who have been asked to accept our latest terms but who have not yet done so
        PENDING_TERMS_ACCEPTANCE_LIST_NAME
    };

    private final PartnerService partnerService;
    private final SavedListService savedListService;
    private final ShutdownService shutdownService;
    private final TcInstanceService tcInstanceService;
    private final UserService userService;

    @Value("${tc.init.boot-admin-password}")
    private String systemAdminPassword;

    @Value("${email.user}")
    private String sysAdminEmail;

    /**
     * Run at startup to check whether we have necessary objects, creating them if necessary
     */
    @EventListener(ApplicationReadyEvent.class)
    @Order(1)
    public void autoCreates() {

        try {
            doAutoCreates();
        } catch (Exception ex) {
            LogBuilder.builder(log)
                .action("SystemAdminConfiguration: Fatal startup error")
                .logError(ex);

            shutdownService.shutdown();
        }
    }

    /**
     * Auto creates various system objects necessary for the application to function.
     * <p>
     * If they do not already exist, they are created.
     */
    private void doAutoCreates() {
        //Auto create system partner. They will be partner associated with the system admin user.
        Partner systemPartner = partnerService.getPartnerFromAbbreviation(SYSTEM_PARTNER_ABBREVIATION);
        if (systemPartner == null) {
            UpdatePartnerRequest req = new UpdatePartnerRequest();
            req.setName(SYSTEM_PARTNER_NAME);
            req.setAbbreviation(SYSTEM_PARTNER_ABBREVIATION);
            req.setStatus(Status.active);

            req.setJobCreator(false);
            req.setSourcePartner(false);

            //Create system partner
            systemPartner = partnerService.create(req);
        }

        //Auto create system admin user
        User systemAdmin = userService.findByUsernameAndRole(SYSTEM_ADMIN_NAME, Role.systemadmin);
        if (systemAdmin == null) {
            if (!StringUtils.hasText(systemAdminPassword)) {
                throw new InvalidRequestException(
                    "No password provided for creating " + SYSTEM_ADMIN_NAME +  " user. "
                        + "See boot-admin-password in the application.yml.");
            }

            UpdateUserRequest req = new UpdateUserRequest();
            req.setUsername(SYSTEM_ADMIN_NAME);
            req.setStatus(Status.active);
            req.setFirstName("System");
            req.setLastName("Admin");
            req.setEmail(sysAdminEmail);
            req.setRole(Role.systemadmin);
            req.setJobCreator(false);
            req.setReadOnly(false);
            req.setUsingMfa(false);
            req.setPartnerId(systemPartner.getId());
            req.setPassword(systemAdminPassword);

            //Self create system admin
            systemAdmin = userService.createUser(req, null);
        }

        //Auto create default source partner.
        Partner defaultSourcePartner
            = partnerService.findDefaultSourcePartner().orElse(null);
        if (defaultSourcePartner == null) {
            UpdatePartnerRequest req = new UpdatePartnerRequest();

            String defaultSourcePartnerName = tcInstanceService.getDefaultSourcePartnerName();
            String defaultSourcePartnerAbbreviation
                = tcInstanceService.getDefaultSourcePartnerAbbreviation();
            req.setName(defaultSourcePartnerName);
            req.setAbbreviation(defaultSourcePartnerAbbreviation);
            req.setStatus(Status.active);

            req.setJobCreator(false);
            req.setSourcePartner(true);
            req.setDefaultSourcePartner(true);

            //Create the default source partner
            partnerService.create(req);
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
