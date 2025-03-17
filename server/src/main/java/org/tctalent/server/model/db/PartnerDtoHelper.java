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

package org.tctalent.server.model.db;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.tctalent.server.model.db.partner.Partner;
import org.tctalent.server.util.dto.DtoBuilder;
import org.tctalent.server.util.dto.DtoPropertyFilter;

/**
 *  DTOs for Partners
 *
 * @author John Cameron
 */
public class PartnerDtoHelper {
    /**
     * Filters out properties in the DtoBuilder not appropriate to the type of partner
     */
    static private class PartnerDtoPropertyFilter implements DtoPropertyFilter {

        //These properties should only be extracted for source partner's
        private final Set<String> sourcePartnerOnlyProperties =
            new HashSet<>(Arrays.asList(
                "registrationLandingPage", "sourceCountries",
                "autoAssignable", "defaultPartnerRef"));

        public boolean ignoreProperty(Object o, String property) {
            Partner partner = (Partner) o;

            boolean ignore = sourcePartnerOnlyProperties.contains(property) && !partner.isSourcePartner();

            return ignore;
        }
    }

    public static DtoBuilder getPartnerDto() {
        return new DtoBuilder(new PartnerDtoPropertyFilter())
            .add("abbreviation")
            .add("autoAssignable")
            .add("defaultContact", userDto())
            .add("defaultJobCreator")
            .add("defaultSourcePartner")
            .add("defaultPartnerRef")
            .add("employer", employerDto())
            .add("id")
            .add("jobContact", userDto())
            .add("jobCreator")
            .add("logo")
            .add("name")
            .add("notificationEmail")
            .add("publicApiAccess")
            .add("publicApiAuthorities")
            .add("publicApiKey")
            .add("publicId")
            .add("sourcePartner")
            .add("status")
            .add("websiteUrl")
            .add("registrationLandingPage")
            .add("sflink")
            .add("sourceCountries", countryDto())
            .add("redirectPartner", shortPartnerDto())
            ;
    }

    private static DtoBuilder publicApiAuthorityDto() {
        return new DtoBuilder()
            .add("name")
            ;
    }

    private static DtoBuilder countryDto() {
        return new DtoBuilder()
            .add("id")
            .add("name")
            .add("isoCode")
            .add("status")
            ;
    }

    public static DtoBuilder employerDto() {
        return new DtoBuilder()
            .add("id")
            .add("name")
            .add("description")
            .add("hasHiredInternationally")
            .add("sfId")
            .add("website")
            ;
    }
    private static DtoBuilder userDto() {
        return new DtoBuilder()
            .add("firstName")
            .add("lastName")
            .add("email")
            ;
    }

    private static DtoBuilder shortPartnerDto() {
        return new DtoBuilder()
            .add("id")
            .add("name")
            .add("abbreviation")
            .add("websiteUrl")
            ;
    }

}
