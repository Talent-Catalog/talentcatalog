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

package org.tctalent.server.util.help;

import java.util.ArrayList;
import java.util.List;
import org.tctalent.server.model.db.HelpFocus;
import org.tctalent.server.request.helplink.SearchHelpLinkRequest;

/**
 * Some utilities for managing Help Links
 *
 * @author John Cameron
 */
public class HelpLinkHelper {

    /**
     * Generates a sequence of requests from the given parent request.
     * <p/>
     * We start by looking for links related to all terms in the original request. But if there
     * are no links matching all terms, we generate another request using fewer terms. If that
     * request also does not return any links, then we reduce the number of terms again - and so on.
     * <p/>
     * The idea is that we will eventually return at least one link - even if that link is
     * just a link to a "catch all" general help document.
     * @param request Original "parent" request
     * @return an ordered list of requests which should be made in order until at least one link is
     * returned.
     */
    public static List<SearchHelpLinkRequest> generateRequestSequence(SearchHelpLinkRequest request) {
        List<SearchHelpLinkRequest> requests = new ArrayList<>();

        //We always start with the original request
        requests.add(request);

        HelpFocus focus = request.getFocus();

        final boolean focussedHelp = focus != null;
        final boolean countrySpecificHelp = request.getCountryId() != null;

        //This is used to create child requests
        SearchHelpLinkRequest req;

        if (focussedHelp && countrySpecificHelp) {
            //Drop out the country
            req = new SearchHelpLinkRequest(request);
            req.setCountryId(null);
            requests.add(req);

            //Drop out the focus
            req = new SearchHelpLinkRequest(request);
            req.setFocus(null);
            requests.add(req);

            //Drop out focus and country
            req = new SearchHelpLinkRequest(request);
            req.setFocus(null);
            req.setCountryId(null);
            requests.add(req);
        } else if (focussedHelp) {
            //Drop out the focus
            req = new SearchHelpLinkRequest(request);
            req.setFocus(null);
            requests.add(req);
        } else if (countrySpecificHelp) {
            //Drop out the country
            req = new SearchHelpLinkRequest(request);
            req.setCountryId(null);
            requests.add(req);
        }

        return  requests;
    }
}
