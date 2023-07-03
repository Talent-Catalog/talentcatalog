/*
 * Copyright (c) 2022 Talent Beyond Boundaries.
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

package org.tbbtalent.server.model.db;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.tbbtalent.server.model.db.partner.SourcePartner;
import org.tbbtalent.server.util.dto.DtoBuilder;
import org.tbbtalent.server.util.dto.DtoPropertyFilter;

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
            //Ignore properties which do not exist on type of partner
            boolean ignore =
                sourcePartnerOnlyProperties.contains(property) && ! (o instanceof SourcePartner);

            return ignore;
        }
    }

    public static DtoBuilder getPartnerDto() {
        return new DtoBuilder(new PartnerDtoPropertyFilter())
            .add("abbreviation")
            .add("autoAssignable")
            .add("defaultContact", userDto())
            .add("defaultDestinationPartner")
            .add("defaultSourcePartner")
            .add("defaultPartnerRef")
            .add("id")
            .add("jobContact", userDto())
            .add("logo")
            .add("name")
            .add("notificationEmail")
            .add("partnerType")
            .add("status")
            .add("websiteUrl")
            .add("registrationLandingPage")
            .add("sflink")
            .add("sourceCountries", countryDto())
            ;
    }

    private static DtoBuilder countryDto() {
        return new DtoBuilder()
            .add("id")
            .add("name")
            ;
    }
    private static DtoBuilder userDto() {
        return new DtoBuilder()
            .add("firstName")
            .add("lastName")
            .add("email")
            ;
    }

}
