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

import java.time.OffsetDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import jakarta.validation.constraints.NotNull;
import org.springframework.lang.Nullable;

/**
 * Some useful Salesforce utilities.
 *
 * @author John Cameron
 */
public class SalesforceHelper {
    private final static String SF_OPPORTUNITY_LINK_MIDDLE
            = "/lightning/r/Opportunity/";
    private final static String SF_OPPORTUNITY_LINK_SUFFIX
        = "/view/";

    /**
     * Converts a Salesforce record id to the opportunity link (url) for that record.
     * @param sfId Salesforce record id.
     * @return Url (ie link) to opportunity record with that id - null if sfId is null
     */
    @Nullable
    public static String sfOppIdToLink(@Nullable String sfId, String baseLightningUrl) {
        return sfId == null ? null : baseLightningUrl + SF_OPPORTUNITY_LINK_MIDDLE + sfId + SF_OPPORTUNITY_LINK_SUFFIX;
    }

    /**
     * Converts a Salesforce Offset Date Time string to an {@link OffsetDateTime}.
     * @param sfOffsetDateTime Salesforce offset date time string
     * @return OffsetDateTime or null if input string is null
     */
    @Nullable
    public static OffsetDateTime parseSalesforceOffsetDateTime(@Nullable String sfOffsetDateTime) {
        //Salesforce strings have the offset as hhmm instead of hh:mm. We just need to insert the :
        //For example "2023-06-01T00:21:58.000+0000" -> "2023-06-01T00:21:58.000+00:00"
        OffsetDateTime offsetDateTime = null;
        if (sfOffsetDateTime != null) {
            final int sfLen = sfOffsetDateTime.length();
            if (sfLen > 2) {
                String isoOffsetDateTime =
                    sfOffsetDateTime.substring(0, sfLen - 2) + ":" + sfOffsetDateTime.substring(
                        sfLen - 2);
                offsetDateTime = OffsetDateTime.parse(isoOffsetDateTime);
            }
        }
        return offsetDateTime;
    }


    /**
     * Extracts the Salesforce record id from the Salesforce url of a record.
     *
     * @param url Url of a Salesforce record
     * @return Salesforce id or null if the url was null or wasn't a valid record url
     */
    public static @Nullable
    String extractIdFromSfUrl(@Nullable String url) {
        return extractFieldFromSfUrl(url, 2);
    }

    /**
     * Extracts the Salesforce record ids from Salesforce record urls.
     * <p/>
     * List version go {@link #extractIdFromSfUrl(String)}
     */
    public static @NotNull
    List<String> extractIdFromSfUrl(@NotNull List<String> urls) {
        return urls.stream()
            .map(SalesforceHelper::extractIdFromSfUrl)
            .collect(Collectors.toList());
    }

    /**
     * Extracts the Salesforce object type (eg Account, Contact, Opportunity) from the Salesforce
     * url of a record.
     *
     * @param url Url of a Salesforce record
     * @return Salesforce object type or null if the url was null or wasn't a valid record url
     */
    public static @Nullable
    String extractObjectTypeFromSfUrl(@Nullable String url) {
        return extractFieldFromSfUrl(url, 1);
    }

    /**
     * Extracts the given field from the Salesforce url of a record.
     * <p/>
     * <ul>
     *     <li>1 - Salesforce object type (eg Account, Contact, Opportunity) </li>
     *     <li>2 - Salesforce record id</li>
     * </ul>
     *
     * @param url Url of a Salesforce record
     * @param fieldNum Indicates what you want to extract from the url.
     * @return Salesforce id or null if the url was null or wasn't a valid record url
     */
    private static @Nullable
    String extractFieldFromSfUrl(@Nullable String url, int fieldNum) {
        if (url == null) {
            return null;
        }

        //https://salesforce.stackexchange.com/questions/1653/what-are-salesforce-ids-composed-of
        String pattern =
            //This is the standard prefix for our Salesforce, allowing for both sandbox and prod versions.
            "https://talentbeyondboundaries\\S*\\.lightning\\.force\\.com" + "/" +

                //This part just checks for 15 or more "word" characters with
                //no "punctuation" - eg . or /.
                //That will be the Salesforce id.
                //It should be preceeded by the record type surrounded
                //by "/".
                ".*/([\\w]+)/([\\w]{15,})[^\\w]?.*";

        Pattern r = Pattern.compile(pattern);

        Matcher m = r.matcher(url);

        final int groupCount = m.groupCount();
        if (m.find() && groupCount == 2) {
            return m.group(fieldNum);
        } else {
            return null;
        }
    }

}
