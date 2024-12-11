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

import com.google.common.net.InternetDomainName;
import org.springframework.lang.Nullable;

/**
 * Util for turning a partner subdomain into a standard url with a partner query param.
 * <p/>
 * eg crs.tctalent.org --> tctalent.org?p=crs
 *
 * @author John Cameron
 */
public class SubdomainRedirectHelper {

    /**
     * Check whether given url is a subdomain. If it is, treat it as a partner domain and
     * redirect it to the equivalent query param formatted url.
     * @param url Url to be examined
     * @return null if not a subdomain, otherwise returns equivalent query based url
     */
    @Nullable
    public static String computeRedirectUrl(String url) {
        String redirectUrl = null;
        //See https://stackoverflow.com/questions/7217271/extract-main-domain-name-from-a-given-url/
        InternetDomainName internetDomainName;
        try {
            internetDomainName = InternetDomainName.from(url);
        } catch (IllegalArgumentException e) {
            //This will happen when we receive raw internet addresses.
            internetDomainName = null;
        }
        //This is a way of checking whether there is a subdomain. If the domain name is not the top
        //private domain then it will be a subdomain.
        if (internetDomainName != null && !internetDomainName.isTopPrivateDomain()) {
            //The top private domain will be our basic domain name - eg tctalent.org
            String ourDomain = InternetDomainName.from(url).topPrivateDomain().toString();
            String suffix = "." + ourDomain;
            String subdomain = url.substring(0, url.indexOf(suffix));
            if (subdomain.length() > 0) {
                redirectUrl = "https://" + ourDomain + "?p=" + subdomain;
            }
        }
        return redirectUrl;
    }

}
