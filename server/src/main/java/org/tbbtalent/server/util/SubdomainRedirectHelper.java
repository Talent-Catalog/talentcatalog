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

package org.tbbtalent.server.util;

import org.springframework.lang.Nullable;

/**
 * Util for turning a partner subdomain into a standard url with a partner query param.
 * <p/>
 * eg crs.tctalent.org --> tctalent.org?p=crs
 *
 * @author John Cameron
 */
public class SubdomainRedirectHelper {
    @Nullable
    public static String computeRedirectUrl(String host) {
        String redirectUrl = null;
        String rootDomain = "tctalent.org";
        String suffix = "." + rootDomain;
        if (host != null && host.endsWith(suffix)) {
            String subdomain = host.substring(0, host.indexOf(suffix));
            if (subdomain.length() > 0) {
                redirectUrl = "https://" + rootDomain + "?p=" + subdomain;
            }
        }
        return redirectUrl;
    }

}
