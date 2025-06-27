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

package org.tctalent.server.data;

import static java.util.Collections.emptySet;
import static org.tctalent.server.data.CountryTestData.getSourceCountrySet;
import static org.tctalent.server.data.SalesforceJobOppTestData.getEmployer;
import static org.tctalent.server.data.UserTestData.getAuditUser;

import java.util.List;
import org.tctalent.server.model.db.PartnerImpl;
import org.tctalent.server.model.db.Status;

public class PartnerImplTestData {

    public static PartnerImpl getSourcePartner() {
        PartnerImpl partner = new PartnerImpl();
        partner.setId(123L);
        partner.setName("TC Partner");
        partner.setAbbreviation("TCP");
        partner.setSourcePartner(true);
        partner.setLogo("www.logo.com");
        partner.setWebsiteUrl("www.website.com");
        partner.setRegistrationLandingPage("www.registration.com");
        partner.setNotificationEmail("notification@email.address");
        partner.setStatus(Status.active);
        partner.setSourceCountries(getSourceCountrySet());
        partner.setDefaultContact(getAuditUser());
        return partner;
    }

    public static PartnerImpl getDefaultPartner() {
        PartnerImpl p = new PartnerImpl();
        p.setDefaultSourcePartner(true);
        p.setDefaultJobCreator(true);
        p.setJobCreator(true);
        p.setId(999L);
        p.setName("TC Default Partner");
        p.setAbbreviation("TCDP");
        p.setSourcePartner(true);
        p.setLogo("www.logo.com");
        p.setWebsiteUrl("www.website.com");
        p.setRegistrationLandingPage("www.registration.com");
        p.setNotificationEmail("notification@email.address");
        p.setStatus(Status.active);
        p.setSourceCountries(emptySet()); // Unrestricted
        p.setDefaultContact(getAuditUser());
        return p;
    }

    public static PartnerImpl getDestinationPartner() {
        PartnerImpl p = new PartnerImpl();
        p.setName("TC Partner");
        p.setAbbreviation("TCP");
        p.setJobCreator(true);
        p.setSourcePartner(true);
        p.setLogo("logo_url");
        p.setWebsiteUrl("website_url");
        p.setRegistrationLandingPage("registration_landing_page");
        p.setNotificationEmail("notification@email.address");
        p.setStatus(Status.active);
        p.setId(99L);
        return p;
    }

    public static List<PartnerImpl> getListOfDestinationPartners() {
        PartnerImpl partner1 = getDestinationPartner();
        PartnerImpl partner2 = getDestinationPartner();
        partner2.setName("TC Partner 2");
        PartnerImpl partner3 = getDestinationPartner();
        partner3.setName("TC Partner 3");
        return List.of(
            partner1, partner2, partner3
        );
    }

    public static PartnerImpl getEmployerPartner() {
        PartnerImpl p = new PartnerImpl();
        p.setName("Employer");
        p.setAbbreviation("E");
        p.setJobCreator(true);
        p.setEmployer(getEmployer());
        p.setStatus(Status.active);
        return p;
    }

}
